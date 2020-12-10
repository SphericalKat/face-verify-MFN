package dev.smoketrees.face_verify_mfn.db

import androidx.room.*
import dev.smoketrees.face_verify_mfn.models.Embedding
import dev.smoketrees.face_verify_mfn.models.User
import dev.smoketrees.face_verify_mfn.models.UserWithEmbeddings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface UserDao {
    @Transaction
    @Query("SELECT * FROM user")
    fun getAllUsersFlow(): Flow<List<UserWithEmbeddings>>

    @Query("SELECT * FROM user WHERE userId = :userId")
    fun getUserFlowById(userId: Int): Flow<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEmbedding(embedding: Embedding)

    @ExperimentalCoroutinesApi
    fun getAllUsers() = getAllUsersFlow().distinctUntilChanged()

    @ExperimentalCoroutinesApi
    fun getUserById(userId: Int) = getUserFlowById(userId).distinctUntilChanged()
}