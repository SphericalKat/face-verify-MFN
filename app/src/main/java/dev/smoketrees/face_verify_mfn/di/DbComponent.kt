package dev.smoketrees.face_verify_mfn.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.smoketrees.face_verify_mfn.db.Db
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object DbComponent {
    @Provides
    @Singleton
    fun providesDb(@ApplicationContext context: Context) = Room.databaseBuilder(context, Db::class.java, "face-db")
        .fallbackToDestructiveMigration()
        .build()
}

@Module
@InstallIn(ApplicationComponent::class)
object DaoComponent {
    @Provides
    @Singleton
    fun providesUserDao(db: Db) = db.userDao()
}