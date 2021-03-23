package com.rafo.pixabay.data.repository

import com.rafo.pixabay.api.PixabayService
import com.rafo.pixabay.api.data.SearchResponse
import javax.inject.Inject

class PixabyRepository @Inject constructor(private val pixabayService: PixabayService) {

    suspend fun search(query: String, page: Int): SearchResponse =
        pixabayService.search(query = query, page = page)
}