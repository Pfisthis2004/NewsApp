package com.example.newsapp.api

import com.example.newsapp.models.NewsReponse
import com.example.newsapp.util.Constants.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsAPI {
    @GET("v2/top-headlines") // nạp điểm cuối của api
    suspend fun getHeadLines(
        @Query("country")
        contryCode: String ="us", // tham so ma quoc qia
        @Query("page")
        pageNumber: Int =1, // tham so trang
        @Query("apiKey")
        apiKey: String = API_KEY // tham so khóa api
    ): Response<NewsReponse>

    @GET("v2/everything")
    suspend fun searchForNews(
        @Query("q")
        searchQuery: String,
        @Query("page")
        pageNumber: Int =1, // tham so trang
        @Query("apiKey")
        apiKey: String = API_KEY // tham so khóa api
    ): Response<NewsReponse>
}