package dev.smoketrees.face_verify_mfn.db

import androidx.room.*
import dev.smoketrees.face_verify_mfn.models.Embedding
import dev.smoketrees.face_verify_mfn.models.User
import dev.smoketrees.face_verify_mfn.models.UserWithEmbeddings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
abstract class UserDao {
    @Transaction
    @Query("SELECT * FROM user")
    abstract fun getAllUsersFlow(): Flow<List<UserWithEmbeddings>>

    @Transaction
    @Query("SELECT * FROM user WHERE userId = :userId")
    abstract fun getUserFlowById(userId: Int): Flow<UserWithEmbeddings>

    suspend fun insertUserWithEmbedding(user: User, embedding: Embedding) {
        val userId = insertUser(user)
        embedding.userId = userId.toInt()
        insertEmbedding(embedding)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertUser(user: User): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertEmbedding(embedding: Embedding)

    @ExperimentalCoroutinesApi
    fun getAllUsers() = getAllUsersFlow().distinctUntilChanged()

    @ExperimentalCoroutinesApi
    fun getUserById(userId: Int) = getUserFlowById(userId).distinctUntilChanged()
}