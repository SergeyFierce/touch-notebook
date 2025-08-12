package com.example.otebookbeta.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(
    tableName = "contacts",
    indices = [Index(value = ["category"])]
)
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val phone: String,
    val email: String? = null,
    val socialMedia: String? = null,
    val city: String? = null,
    val age: Int? = null,
    val profession: String? = null,
    val category: String,
    val status: String? = null,
    val tags: String? = null,
    val comment: String? = null,
    val dateAdded: String = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
)
