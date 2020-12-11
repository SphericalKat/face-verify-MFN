package dev.smoketrees.face_verify_mfn.models

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import dev.smoketrees.face_verify_mfn.R

@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val name: String
)

data class UserWithEmbeddings(
    @Embedded val user: User,
    @Relation(
        parentColumn = "userId",
        entityColumn = "embeddingId"
    )
    val embeddings: List<Embedding>
)

class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val id: TextView = itemView.findViewById(R.id.idText)
    val name: TextView = itemView.findViewById(R.id.nameText)
}