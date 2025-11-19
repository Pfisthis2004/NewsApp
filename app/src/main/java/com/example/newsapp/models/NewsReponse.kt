package com.example.newsapp.models

data class NewsReponse(
    val articles: MutableList<Article>,
    val totalResults: Int
)