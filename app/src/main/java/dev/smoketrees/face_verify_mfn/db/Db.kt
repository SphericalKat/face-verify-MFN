package dev.smoketrees.face_verify_mfn.db

import androidx.room.Database
import androidx.room.Entity
import androidx.room.RoomDatabase
import dev.smoketrees.face_verify_mfn.models.User

@Database(
    entities = [User::class, Entity::class],
    version = 1,
    exportSchema = false
)
abstract class Db : RoomDatabase() {
    abstract fun userDao(): UserDao
}