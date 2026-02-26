package com.edwindiaz.votaya_tilinesup.features.polls.data.repositories

import com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.local.dao.PollDao
import com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.local.entities.PollEntity
import com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.remote.mapper.toDomain
import com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.remote.models.PollDto
import com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.remote.models.PollOptionDto
import com.edwindiaz.votaya_tilinesup.features.polls.domain.entities.Poll
import com.edwindiaz.votaya_tilinesup.features.polls.domain.entities.PollOption
import com.edwindiaz.votaya_tilinesup.features.polls.domain.entities.PollStatus
import com.edwindiaz.votaya_tilinesup.features.polls.domain.repositories.PollRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

class PollRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val pollDao: PollDao
) : PollRepository {

    override fun observePolls(): Flow<List<Poll>> {
        // Escucha Firestore en tiempo real y guarda en Room (SSOT)
        val firestoreFlow = callbackFlow {
            val listener = firestore.collection("polls")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(PollDto::class.java)?.copy(id = doc.id)
                    }?.let { trySend(it) }
                }
            awaitClose { listener.remove() }
        }

        return pollDao.observeAll().map { entities ->
            entities.map { it.toPollDomain() }
        }
    }

    override suspend fun createPoll(question: String, options: List<String>): Result<Poll> = try {
        val currentUser = firebaseAuth.currentUser!!
        val pollId = UUID.randomUUID().toString()
        val optionsMap = options.mapIndexed { index, text ->
            "option$index" to PollOptionDto(id = "option$index", text = text, votes = 0)
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

        // UX Optimista: guarda en Room con estado PENDING antes de ir a Firestore
        pollDao.upsert(pollDto.toEntity(PollStatus.PENDING))

        try {
            firestore.collection("polls").document(pollId).set(pollDto).await()
            // Confirma en Room como PUBLISHED
            pollDao.upsert(pollDto.toEntity(PollStatus.PUBLISHED))
            Result.success(pollDto.toDomain())
        } catch (e: Exception) {
            // Rollback: elimina de Room si Firestore falla
            pollDao.delete(pollDto.toEntity(PollStatus.PENDING))
            Result.failure(e)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun vote(pollId: String, optionId: String): Result<Unit> = try {
        val currentUser = firebaseAuth.currentUser!!
        val pollRef = firestore.collection("polls").document(pollId)
        val voteRef = firestore.collection("votes").document(pollId)
            .collection("users").document(currentUser.uid)

        firestore.runTransaction { transaction ->
            val pollSnap = transaction.get(pollRef)
            val voteSnap = transaction.get(voteRef)
            if (voteSnap.exists()) throw Exception("Ya votaste en esta encuesta")
            val currentVotes = pollSnap.getLong("options.$optionId.votes") ?: 0
            val currentTotal = pollSnap.getLong("totalVotes") ?: 0
            transaction.update(pollRef, "options.$optionId.votes", currentVotes + 1)
            transaction.update(pollRef, "totalVotes", currentTotal + 1)
            transaction.set(voteRef, mapOf("optionId" to optionId))
        }.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getPollById(pollId: String): Result<Poll> = try {
        val doc = firestore.collection("polls").document(pollId).get().await()
        val pollDto = doc.toObject(PollDto::class.java)?.copy(id = doc.id)
            ?: throw Exception("Encuesta no encontrada")
        Result.success(pollDto.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Helpers de conversión Entity ↔ Domain
    private fun PollDto.toEntity(status: PollStatus) = PollEntity(
        id = id,
        question = question,
        authorId = authorId,
        authorName = authorName,
        optionsJson = optionsToJson(options.values.map {
            PollOption(it.id, it.text, it.votes)
        }),
        totalVotes = totalVotes,
        createdAt = createdAt,
        status = status.name
    )

    private fun PollEntity.toPollDomain() = Poll(
        id = id,
        question = question,
        authorId = authorId,
        authorName = authorName,
        options = optionsFromJson(optionsJson),
        totalVotes = totalVotes,
        createdAt = createdAt,
        status = PollStatus.valueOf(status)
    )

    private fun optionsToJson(options: List<PollOption>): String {
        val array = JSONArray()
        options.forEach {
            array.put(JSONObject().apply {
                put("id", it.id)
                put("text", it.text)
                put("votes", it.votes)
            })
        }
        return array.toString()
    }

    private fun optionsFromJson(json: String): List<PollOption> {
        val array = JSONArray(json)
        return (0 until array.length()).map {
            val obj = array.getJSONObject(it)
            PollOption(obj.getString("id"), obj.getString("text"), obj.getInt("votes"))
        }
    }
}
