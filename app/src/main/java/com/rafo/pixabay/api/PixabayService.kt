package com.rafo.pixabay.api

import com.rafo.pixabay.BuildConfig
import com.rafo.pixabay.api.data.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PixabayService {

    @GET("/api")
    suspend fun search(
        @Query("key") apiKey: String = BuildConfig.PIXABAY_API_KEY,
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("image_type") imageType: String = "photo"
    ): SearchResponse
}