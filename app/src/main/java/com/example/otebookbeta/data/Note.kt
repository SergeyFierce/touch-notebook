package com.example.otebookbeta.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = Contact::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index(value = ["contactId"])]
)
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactId: Int,
    val content: String,
    val dateAdded: String
)
