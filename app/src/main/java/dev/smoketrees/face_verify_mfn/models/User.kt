package dev.smoketrees.face_verify_mfn.models

import androidx.room.*

@Entity
data class User(
    @PrimaryKey val userId: Int,

)

data class UserWithEmbeddings(
    @Embedded val user: User,
    @Relation(
        parentColumn = "userId",
        entityColumn = "embeddingId"
    )
    val embeddings: List<Embedding>
)
