package com.example.newsapp.repository

import androidx.room.Query
import com.example.newsapp.api.RetrofitInstance
import com.example.newsapp.db.ArticleDatabase
import com.example.newsapp.models.Article
import retrofit2.Retrofit

class NewsRepository(val db: ArticleDatabase) {
    suspend fun getHeadlines(contryCode: String,pageNumber: Int)=
        RetrofitInstance.api.getHeadLines(contryCode,pageNumber)

    suspend fun searchNews(searchQuery: String,pageNumber: Int)=
        RetrofitInstance.api.searchForNews(searchQuery,pageNumber)

    suspend fun upsert(article: Article) = db.getArticleDao().upsert(article)

     fun getFavoriteNews() = db.getArticleDao().getAllActicle()

    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteActicle(article)
}