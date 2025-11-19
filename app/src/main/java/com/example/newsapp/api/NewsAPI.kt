package com.example.newsapp.api

import com.example.newsapp.models.NewsReponse
import com.example.newsapp.util.Constants.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsAPI {
    @GET("api/v4/top-headlines") // nạp điểm cuối của api
    suspend fun getHeadLines(
        @Query("country") countryCode: String = "vn",
        @Query("lang") lang: String = "vi",
        @Query("page") page: Int,
        @Query("max") maxResults: Int = 10,
        @Query("apikey") apiKey: String = API_KEY
    ): Response<NewsReponse>

    @GET("api/v4/search")
    suspend fun searchForNews(
        @Query("q") searchQuery: String,
        @Query("lang") lang: String = "vi",
        @Query("country") countryCode: String = "vn",
        @Query("max") maxResults: Int = 10,
        @Query("apikey") apiKey: String = API_KEY
    ): Response<NewsReponse>
}