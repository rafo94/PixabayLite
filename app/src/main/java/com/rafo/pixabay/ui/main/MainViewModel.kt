package com.rafo.pixabay.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafo.pixabay.api.data.SearchResponse
import com.rafo.pixabay.data.repository.PixabyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val pixabyRepository: PixabyRepository) :
    ViewModel() {

    private val _imagesResponseData: MutableStateFlow<SearchResponse?> = MutableStateFlow(null)
    val imagesResponseLiveData: StateFlow<SearchResponse?> = _imagesResponseData.asStateFlow()

    fun search(query: String = "", page: Int = 1) {
        viewModelScope.launch {
            _imagesResponseData.value = pixabyRepository.search(query, page)
        }
    }
}