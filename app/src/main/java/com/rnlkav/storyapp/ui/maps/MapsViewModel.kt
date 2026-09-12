package com.rnlkav.storyapp.ui.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.rnlkav.storyapp.data.repository.StoryRepository
import com.rnlkav.storyapp.data.response.StoryResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MapsViewModel(private val repository: StoryRepository) : ViewModel() {

    private val _stories = MutableLiveData<StoryResponse>()
    val stories: LiveData<StoryResponse> = _stories

    private val _isLoading = MutableLiveData<Boolean>()

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun getStoriesWithLocation() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.getStoriesWithLocation()
                _stories.value = response
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, StoryResponse::class.java)
                _stories.value = errorResponse
            } catch (e: Exception) {
                _message.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
