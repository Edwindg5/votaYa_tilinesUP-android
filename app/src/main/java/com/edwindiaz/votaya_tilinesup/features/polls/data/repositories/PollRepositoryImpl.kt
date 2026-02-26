//PollRepositoryImpl.kt
package com.edwindiaz.votaya_tilinesup.features.polls.data.repositories

import com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.remote.mapper.toDomain
import com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.remote.models.PollDto
import com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.remote.models.PollOptionDto
import com.edwindiaz.votaya_tilinesup.features.polls.domain.entities.Poll
import com.edwindiaz.votaya_tilinesup.features.polls.domain.repositories.PollRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PollRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : PollRepository {

    // Versión para una sola carga
    override suspend fun getPolls(): List<Poll> = try {
        val snapshot = firestore.collection("polls")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()

        snapshot.documents.mapNotNull { doc ->
            val poll = doc.toObject(PollDto::class.java)?.copy(id = doc.id)
            poll?.toDomain()
        }
    } catch (e: Exception) {
        emptyList()
    }

    // Versión en tiempo real (Flow)
    override fun observePolls(): Flow<List<Poll>> {
        return firestore.collection("polls")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val poll = doc.toObject(PollDto::class.java)?.copy(id = doc.id)
                    poll?.toDomain()
                }
            }
    }

    override suspend fun createPoll(question: String, options: List<String>): Result<Poll> = try {
        val currentUser = auth.currentUser ?: throw Exception("Usuario no autenticado")
        val pollId = UUID.randomUUID().toString()

        val optionsMap = options.mapIndexed { index, text ->
            "option${index + 1}" to PollOptionDto(
                id = "option${index + 1}",
                text = text,
                votes = 0
            )
        }.toMap()

        val pollDto = PollDto(
            id = pollId,
            question = question,
            authorId = currentUser.uid,
            authorName = currentUser.displayName ?: "Usuario",
            options = optionsMap,
            totalVotes = 0,
            createdAt = System.currentTimeMillis()
        )

        firestore.collection("polls")
            .document(pollId)
            .set(pollDto)
            .await()

        Result.success(pollDto.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun vote(pollId: String, optionId: String): Result<Unit> = try {
        val currentUser = auth.currentUser ?: throw Exception("Usuario no autenticado")

        val pollRef = firestore.collection("polls").document(pollId)
        val userVoteRef = firestore.collection("polls")
            .document(pollId)
            .collection("votes")
            .document(currentUser.uid)

        val voteDoc = userVoteRef.get().await()
        if (voteDoc.exists()) {
            throw Exception("Ya has votado en esta encuesta")
        }

        firestore.runTransaction { transaction ->
            transaction.update(pollRef, "options.$optionId.votes", FieldValue.increment(1))
            transaction.update(pollRef, "totalVotes", FieldValue.increment(1))
            transaction.set(userVoteRef, mapOf(
                "optionId" to optionId,
                "votedAt" to System.currentTimeMillis()
            ))
        }.await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getPollById(pollId: String): Result<Poll> = try {
        val doc = firestore.collection("polls")
            .document(pollId)
            .get()
            .await()

        val pollDto = doc.toObject(PollDto::class.java)?.copy(id = doc.id)
            ?: throw Exception("Encuesta no encontrada")

        Result.success(pollDto.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }
}