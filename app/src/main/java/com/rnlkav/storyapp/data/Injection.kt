package com.rnlkav.storyapp.data

import android.content.Context
import com.rnlkav.storyapp.data.api.ApiConfig
import com.rnlkav.storyapp.data.pref.UserPreference
import com.rnlkav.storyapp.data.pref.dataStore
import com.rnlkav.storyapp.data.repository.UserRepository

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService(pref)
        return UserRepository.getInstance(pref, apiService)
    }
}
