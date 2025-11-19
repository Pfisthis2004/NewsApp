package com.example.newsapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "articles"
)
data class Article(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val description: String,
    val publishedAt: String,
    val source: Source,
    val url: String,
    val image: String
): Serializable