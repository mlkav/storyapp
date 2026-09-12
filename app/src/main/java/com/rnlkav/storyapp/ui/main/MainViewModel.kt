package com.rnlkav.storyapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rnlkav.storyapp.data.pref.UserModel
import com.rnlkav.storyapp.data.repository.UserRepository
import com.rnlkav.storyapp.data.response.ListStoryItem
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainViewModel(private val repository: UserRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _listStory = MutableLiveData<List<ListStoryItem>?>()
    val listStory: LiveData<List<ListStoryItem>?> = _listStory

    private val _message = MutableLiveData<String>()

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun getStories() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.getStories()
                _listStory.value = response.listStory
            } catch (e: HttpException) {
                _message.value = e.message()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
