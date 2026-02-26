// PollRepositoryImpl.kt
package com.edwindiaz.votaya_tilinesup.features.polls.data.repositories

import android.util.Log
import com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.remote.mapper.toDomain
import com.edwindiaz.votaya_tilinesup.features.polls.domain.entities.Poll
import com.edwindiaz.votaya_tilinesup.features.polls.domain.entities.PollOption
import com.edwindiaz.votaya_tilinesup.features.polls.domain.repositories.PollRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class PollRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) : PollRepository {

    private val pollsRef = database.getReference("polls")
    private val usersRef = database.getReference("users")

    override suspend fun getPolls(): List<Poll> = try {
        Log.d("PollRepository", "getPolls: iniciando carga")

        val snapshot = pollsRef
            .orderByChild("createdAt")
            .get()
            .await()

        val polls = mutableListOf<Poll>()

        for (pollSnapshot in snapshot.children) {
            val poll = pollSnapshotToPoll(pollSnapshot)
            if (poll != null) {
                polls.add(poll)
            }
        }

        Log.d("PollRepository", "getPolls: ${polls.size} encuestas encontradas")
        polls
    } catch (e: Exception) {
        Log.e("PollRepository", "Error en getPolls", e)
        emptyList()
    }

    override fun observePolls(): Flow<List<Poll>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val polls = mutableListOf<Poll>()

                    for (pollSnapshot in snapshot.children) {
                        val poll = pollSnapshotToPollSync(pollSnapshot)
                        if (poll != null) {
                            polls.add(poll)
                        }
                    }

                    Log.d("PollRepository", "observer: ${polls.size} encuestas")
                    trySend(polls).isSuccess
                } catch (e: Exception) {
                    Log.e("PollRepository", "Error procesando snapshot", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PollRepository", "Listener cancelado: ${error.message}")
                close(error.toException())
            }
        }

        pollsRef.orderByChild("createdAt").addValueEventListener(listener)

        awaitClose {
            pollsRef.removeEventListener(listener)
        }
    }

    override fun observePollById(pollId: String): Flow<Result<Poll>> = callbackFlow {
        val pollRef = pollsRef.child(pollId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    // No usamos viewModelScope aquí, procesamos directamente
                    val poll = getPollFromSnapshotSync(snapshot)
                    if (poll != null) {
                        trySend(Result.success(poll)).isSuccess
                    } else {
                        trySend(Result.failure(Exception("Encuesta no encontrada"))).isSuccess
                    }
                } catch (e: Exception) {
                    trySend(Result.failure(e)).isSuccess
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException())).isSuccess
                close(error.toException())
            }
        }

        pollRef.addValueEventListener(listener)

        awaitClose {
            pollRef.removeEventListener(listener)
        }
    }

    override suspend fun createPoll(question: String, options: List<String>): Result<Poll> = try {
        Log.d("PollRepository", "createPoll: question='$question', options=$options")

        val currentUser = auth.currentUser
            ?: throw Exception("Usuario no autenticado")

        val pollId = pollsRef.push().key ?: throw Exception("Error generando ID")
        val pollRef = pollsRef.child(pollId)

        // Crear opciones
        val optionsMap = HashMap<String, Any>()
        options.forEach { text ->
            val optionId = UUID.randomUUID().toString()
            val optionMap = HashMap<String, Any>()
            optionMap["text"] = text
            optionMap["votes"] = 0
            optionsMap[optionId] = optionMap
        }

        // Preparar el mapa principal de la encuesta
        val pollMap = HashMap<String, Any>()
        pollMap["title"] = question
        pollMap["ownerId"] = currentUser.uid
        pollMap["totalVotes"] = 0
        pollMap["createdAt"] = ServerValue.TIMESTAMP
        pollMap["options"] = optionsMap
        pollMap["userVotes"] = HashMap<String, Any>()

        pollRef.setValue(pollMap).await()
        Log.d("PollRepository", "✅ Encuesta creada con ID: $pollId")

        // Obtener el timestamp real después de guardar
        val savedSnapshot = pollRef.get().await()
        val createdAtValue = savedSnapshot.child("createdAt").value

        val createdAt = when (createdAtValue) {
            is Long -> createdAtValue
            is Int -> createdAtValue.toLong()
            is Double -> createdAtValue.toLong()
            else -> System.currentTimeMillis()
        }

        // Obtener nombre del autor
        val authorName = try {
            val userSnapshot = usersRef.child(currentUser.uid).get().await()
            userSnapshot.child("displayName").getValue(String::class.java) ?: currentUser.displayName ?: ""
        } catch (e: Exception) {
            currentUser.displayName ?: ""
        }

        // Crear las entidades de opciones
        val optionEntities = optionsMap.map { (optionId, optionData) ->
            val optionMap = optionData as Map<String, Any>
            val votesValue = optionMap["votes"]
            val votes = when (votesValue) {
                is Long -> votesValue.toInt()
                is Int -> votesValue
                is Double -> votesValue.toInt()
                else -> 0
            }

            PollOption(
                id = optionId,
                text = optionMap["text"] as String,
                votes = votes
            )
        }

        val poll = Poll(
            id = pollId,
            question = question,
            authorId = currentUser.uid,
            authorName = authorName,
            options = optionEntities,
            totalVotes = 0,
            createdAt = createdAt
        )

        Result.success(poll)

    } catch (e: Exception) {
        Log.e("PollRepository", "❌ Error en createPoll", e)
        Result.failure(e)
    }

    override suspend fun vote(pollId: String, optionId: String): Result<Unit> = try {
        Log.d("PollRepository", "vote: pollId=$pollId, optionId=$optionId")

        val currentUser = auth.currentUser
            ?: throw Exception("Usuario no autenticado")

        val pollRef = pollsRef.child(pollId)

        // Verificar si ya votó
        val userVoteRef = pollRef.child("userVotes").child(currentUser.uid)
        val voteSnapshot = userVoteRef.get().await()

        if (voteSnapshot.exists()) {
            throw Exception("Ya has votado en esta encuesta")
        }

        // Usar suspendCancellableCoroutine para manejar la transacción
        val result = suspendCancellableCoroutine<Boolean> { continuation ->
            pollRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    return try {
                        // Obtener valores con manejo de tipos
                        val totalVotesObj = mutableData.child("totalVotes").value
                        val totalVotes = when (totalVotesObj) {
                            is Long -> totalVotesObj.toInt()
                            is Int -> totalVotesObj
                            is Double -> totalVotesObj.toInt()
                            else -> 0
                        }

                        val optionData = mutableData.child("options").child(optionId)
                        val currentVotesObj = optionData.child("votes").value
                        val currentVotes = when (currentVotesObj) {
                            is Long -> currentVotesObj.toInt()
                            is Int -> currentVotesObj
                            is Double -> currentVotesObj.toInt()
                            else -> 0
                        }

                        optionData.child("votes").value = currentVotes + 1
                        mutableData.child("totalVotes").value = totalVotes + 1

                        val userVoteMap = HashMap<String, Any>()
                        userVoteMap["optionId"] = optionId
                        userVoteMap["votedAt"] = ServerValue.TIMESTAMP
                        mutableData.child("userVotes").child(currentUser.uid).value = userVoteMap

                        Transaction.success(mutableData)
                    } catch (e: Exception) {
                        Transaction.abort()
                    }
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                    if (error != null) {
                        Log.e("PollRepository", "Error en transacción: ${error.message}")
                        continuation.resume(false)
                    } else {
                        Log.d("PollRepository", "Transacción completada: committed=$committed")
                        continuation.resume(committed)
                    }
                }
            })

            continuation.invokeOnCancellation {
                Log.d("PollRepository", "Transacción cancelada")
            }
        }

        if (result) {
            Result.success(Unit)
        } else {
            throw Exception("Error en la transacción")
        }

    } catch (e: Exception) {
        Log.e("PollRepository", "Error en vote", e)
        Result.failure(e)
    }

    override suspend fun getPollById(pollId: String): Result<Poll> = try {
        Log.d("PollRepository", "getPollById: pollId=$pollId")

        val pollSnapshot = pollsRef.child(pollId).get().await()
        val poll = getPollFromSnapshotSuspend(pollSnapshot)
            ?: throw Exception("Encuesta no encontrada")

        Result.success(poll)

    } catch (e: Exception) {
        Log.e("PollRepository", "Error en getPollById", e)
        Result.failure(e)
    }

    private suspend fun getPollFromSnapshotSuspend(snapshot: DataSnapshot): Poll? {
        return try {
            val pollId = snapshot.key ?: return null
            val title = snapshot.child("title").getValue(String::class.java) ?: return null
            val ownerId = snapshot.child("ownerId").getValue(String::class.java) ?: return null

            // Manejo de tipos para totalVotes
            val totalVotesObj = snapshot.child("totalVotes").value
            val totalVotes = when (totalVotesObj) {
                is Long -> totalVotesObj.toInt()
                is Int -> totalVotesObj
                is Double -> totalVotesObj.toInt()
                else -> 0
            }

            // Manejo de tipos para createdAt
            val createdAtObj = snapshot.child("createdAt").value
            val createdAt = when (createdAtObj) {
                is Long -> createdAtObj
                is Int -> createdAtObj.toLong()
                is Double -> createdAtObj.toLong()
                else -> 0L
            }

            val options = mutableListOf<PollOption>()
            val optionsSnapshot = snapshot.child("options")

            for (optionSnapshot in optionsSnapshot.children) {
                val optionId = optionSnapshot.key ?: continue
                val text = optionSnapshot.child("text").getValue(String::class.java) ?: continue

                val votesObj = optionSnapshot.child("votes").value
                val votes = when (votesObj) {
                    is Long -> votesObj.toInt()
                    is Int -> votesObj
                    is Double -> votesObj.toInt()
                    else -> 0
                }

                options.add(
                    PollOption(
                        id = optionId,
                        text = text,
                        votes = votes
                    )
                )
            }

            val authorName = try {
                val userSnapshot = usersRef.child(ownerId).get().await()
                userSnapshot.child("displayName").getValue(String::class.java) ?: ""
            } catch (e: Exception) {
                ""
            }

            Poll(
                id = pollId,
                question = title,
                authorId = ownerId,
                authorName = authorName,
                options = options,
                totalVotes = totalVotes,
                createdAt = createdAt
            )
        } catch (e: Exception) {
            Log.e("PollRepository", "Error convirtiendo snapshot", e)
            null
        }
    }

    private fun getPollFromSnapshotSync(snapshot: DataSnapshot): Poll? {
        return try {
            val pollId = snapshot.key ?: return null
            val title = snapshot.child("title").getValue(String::class.java) ?: return null
            val ownerId = snapshot.child("ownerId").getValue(String::class.java) ?: return null

            val totalVotesObj = snapshot.child("totalVotes").value
            val totalVotes = when (totalVotesObj) {
                is Long -> totalVotesObj.toInt()
                is Int -> totalVotesObj
                is Double -> totalVotesObj.toInt()
                else -> 0
            }

            val createdAtObj = snapshot.child("createdAt").value
            val createdAt = when (createdAtObj) {
                is Long -> createdAtObj
                is Int -> createdAtObj.toLong()
                is Double -> createdAtObj.toLong()
                else -> 0L
            }

            val options = mutableListOf<PollOption>()
            val optionsSnapshot = snapshot.child("options")

            for (optionSnapshot in optionsSnapshot.children) {
                val optionId = optionSnapshot.key ?: continue
                val text = optionSnapshot.child("text").getValue(String::class.java) ?: continue

                val votesObj = optionSnapshot.child("votes").value
                val votes = when (votesObj) {
                    is Long -> votesObj.toInt()
                    is Int -> votesObj
                    is Double -> votesObj.toInt()
                    else -> 0
                }

                options.add(
                    PollOption(
                        id = optionId,
                        text = text,
                        votes = votes
                    )
                )
            }

            // Versión síncrona - no podemos obtener authorName aquí porque requiere llamada suspendida
            // El authorName se cargará después en el ViewModel si es necesario

            Poll(
                id = pollId,
                question = title,
                authorId = ownerId,
                authorName = "", // Se actualizará después
                options = options,
                totalVotes = totalVotes,
                createdAt = createdAt
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun pollSnapshotToPollSync(snapshot: DataSnapshot): Poll? {
        return getPollFromSnapshotSync(snapshot)
    }

    private suspend fun pollSnapshotToPoll(snapshot: DataSnapshot): Poll? {
        return getPollFromSnapshotSuspend(snapshot)
    }
}