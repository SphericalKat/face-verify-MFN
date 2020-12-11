package dev.smoketrees.viewmodels

import android.graphics.Bitmap
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dev.smoketrees.face_verify_mfn.db.UserDao
import dev.smoketrees.face_verify_mfn.models.Embedding
import dev.smoketrees.face_verify_mfn.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

class AppViewModel @ViewModelInject constructor(
    private val userDao: UserDao,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel( ){

    var name: String = ""

    @ExperimentalCoroutinesApi
    fun getAllUsers() = userDao.getAllUsers().asLiveData(viewModelScope.coroutineContext)

    fun addUser(user: User, embedding: Embedding) = viewModelScope.launch(Dispatchers.IO) {
        userDao.insertUserWithEmbedding(user, embedding)
    }
}