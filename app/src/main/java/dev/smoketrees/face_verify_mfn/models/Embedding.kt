package dev.smoketrees.face_verify_mfn.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Embedding(
    @PrimaryKey(autoGenerate = true) val embeddingId: Int = 0,
    var userId: Int = 0,
    val embedding: String
)
