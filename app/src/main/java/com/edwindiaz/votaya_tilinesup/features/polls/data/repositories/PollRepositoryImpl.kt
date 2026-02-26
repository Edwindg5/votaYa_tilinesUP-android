//PollRepositoryImpl.kt
package com.edwindiaz.votaya_tilinesup.features.polls.data.repositories

import android.util.Log
import com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.remote.mapper.toDomain
import com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.remote.mapper.toDto
import com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.remote.models.PollDto
import com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.remote.models.PollOptionDto
import com.edwindiaz.votaya_tilinesup.features.polls.domain.entities.Poll
import com.edwindiaz.votaya_tilinesup.features.polls.domain.entities.PollOption
import com.edwindiaz.votaya_tilinesup.features.polls.domain.repositories.PollRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PollRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : PollRepository {

    override suspend fun getPolls(): List<Poll> = try {
        Log.d("PollRepository", "getPolls: iniciando carga")

        val pollsSnapshot = firestore.collection("polls")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()

        Log.d("PollRepository", "getPolls: ${pollsSnapshot.documents.size} encuestas encontradas")

        val polls = mutableListOf<Poll>()

        for (doc in pollsSnapshot.documents) {
            val pollDto = doc.toObject(PollDto::class.java)?.copy(id = doc.id)

            if (pollDto != null) {
                // Obtener las opciones de la subcolección
                val optionsSnapshot = doc.reference.collection("options").get().await()
                Log.d("PollRepository", "Encuesta ${doc.id}: ${optionsSnapshot.documents.size} opciones encontradas")

                val options = optionsSnapshot.documents.mapNotNull { optionDoc ->
                    optionDoc.toObject(PollOptionDto::class.java)?.copy(id = optionDoc.id)
                }.map { it.toDomain() }

                polls.add(pollDto.toDomain(options))
            }
        }

        polls
    } catch (e: Exception) {
        Log.e("PollRepository", "Error en getPolls", e)
        emptyList()
    }

    override fun observePolls(): Flow<List<Poll>> {
        return firestore.collection("polls")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                val polls = mutableListOf<Poll>()

                for (doc in snapshot.documents) {
                    val pollDto = doc.toObject(PollDto::class.java)?.copy(id = doc.id)

                    if (pollDto != null) {
                        // Nota: En un flujo en tiempo real, esto no es óptimo
                        val options = emptyList<PollOption>()
                        polls.add(pollDto.toDomain(options))
                    }
                }

                polls
            }
    }

    override suspend fun createPoll(question: String, options: List<String>): Result<Poll> = try {
        Log.d("PollRepository", "createPoll: question='$question', options=$options")

        val currentUser = auth.currentUser
            ?: throw Exception("Usuario no autenticado")

        val pollRef = firestore.collection("polls").document()

        val pollDto = PollDto(
            id = pollRef.id,
            title = question,
            ownerId = currentUser.uid,
            totalVotes = 0,
            createdAt = Timestamp.now()
        )

        pollRef.set(pollDto).await()
        Log.d("PollRepository", "Encuesta creada con ID: ${pollRef.id}")

        val optionEntities = options.mapIndexed { index, text ->
            val optionRef = pollRef.collection("options").document()
            val optionDto = PollOptionDto(
                id = optionRef.id,
                text = text,
                votes = 0
            )
            optionRef.set(optionDto).await()
            Log.d("PollRepository", "Opción creada: ${optionRef.id} - $text")

            optionDto.toDomain()
        }

        val poll = pollDto.toDomain(optionEntities)
        Result.success(poll)

    } catch (e: Exception) {
        Log.e("PollRepository", "Error en createPoll", e)
        Result.failure(e)
    }

    override suspend fun vote(pollId: String, optionId: String): Result<Unit> = try {
        Log.d("PollRepository", "vote: pollId=$pollId, optionId=$optionId")

        val currentUser = auth.currentUser
            ?: throw Exception("Usuario no autenticado")

        val pollRef = firestore.collection("polls").document(pollId)
        val optionRef = pollRef.collection("options").document(optionId)
        val userVoteRef = pollRef.collection("userVotes").document(currentUser.uid)

        val voteDoc = userVoteRef.get().await()
        if (voteDoc.exists()) {
            throw Exception("Ya has votado en esta encuesta")
        }

        firestore.runTransaction { transaction ->
            transaction.update(optionRef, "votes", FieldValue.increment(1))
            transaction.update(pollRef, "totalVotes", FieldValue.increment(1))
            transaction.set(userVoteRef, mapOf(
                "optionId" to optionId,
                "votedAt" to Timestamp.now()
            ))
        }.await()

        Log.d("PollRepository", "Voto registrado exitosamente")
        Result.success(Unit)

    } catch (e: Exception) {
        Log.e("PollRepository", "Error en vote", e)
        Result.failure(e)
    }

    override suspend fun getPollById(pollId: String): Result<Poll> = try {
        Log.d("PollRepository", "getPollById: pollId=$pollId")

        val pollDoc = firestore.collection("polls").document(pollId).get().await()
        val pollDto = pollDoc.toObject(PollDto::class.java)?.copy(id = pollDoc.id)
            ?: throw Exception("Encuesta no encontrada")

        val optionsSnapshot = pollDoc.reference.collection("options").get().await()
        val options = optionsSnapshot.documents.mapNotNull { optionDoc ->
            optionDoc.toObject(PollOptionDto::class.java)?.copy(id = optionDoc.id)
        }.map { it.toDomain() }

        Result.success(pollDto.toDomain(options))

    } catch (e: Exception) {
        Log.e("PollRepository", "Error en getPollById", e)
        Result.failure(e)
    }
}