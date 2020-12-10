package dev.smoketrees.face_verify_mfn.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Embedding(
    @PrimaryKey val embeddingId: Int,
    val embedding: String
)
