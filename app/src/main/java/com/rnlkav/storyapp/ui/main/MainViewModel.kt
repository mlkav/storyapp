package com.rnlkav.storyapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rnlkav.storyapp.data.database.StoryEntity
import com.rnlkav.storyapp.data.pref.UserModel
import com.rnlkav.storyapp.data.repository.StoryRepository
import com.rnlkav.storyapp.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(
    private val userRepository: UserRepository,
    storyRepository: StoryRepository
) : ViewModel() {

    fun getSession(): LiveData<UserModel> {
        return userRepository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }

    val stories: Flow<PagingData<StoryEntity>> =
        storyRepository.getStories().cachedIn(viewModelScope)
}
