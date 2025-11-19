package com.example.newsapp.repository

import androidx.room.Query
import com.example.newsapp.api.RetrofitInstance
import com.example.newsapp.db.ArticleDatabase
import com.example.newsapp.models.Article
import retrofit2.Retrofit

class NewsRepository(val db: ArticleDatabase) {
    suspend fun getHeadlines(countryCode: String, lang: String ="vi", page: Int, maxResults: Int =10)=
        RetrofitInstance.api.getHeadLines(countryCode, lang, page,maxResults )

    suspend fun searchNews(searchQuery: String,lang: String ="vi",countryCode: String ="vn", maxResults: Int=10)=
        RetrofitInstance.api.searchForNews(searchQuery, lang,countryCode,maxResults)

    suspend fun upsert(article: Article) = db.getArticleDao().upsert(article)

     fun getFavoriteNews() = db.getArticleDao().getAllActicle()

    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteActicle(article)
}