package com.rnlkav.storyapp.data.repository

import com.rnlkav.storyapp.data.api.ApiService
import com.rnlkav.storyapp.data.pref.UserModel
import com.rnlkav.storyapp.data.pref.UserPreference
import com.rnlkav.storyapp.data.response.LoginResponse
import com.rnlkav.storyapp.data.response.RegisterResponse
import kotlinx.coroutines.flow.Flow
import com.rnlkav.storyapp.utils.EspressoIdlingResource

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService,
) {

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    suspend fun register(name: String, email: String, pass: String): RegisterResponse {
        EspressoIdlingResource.increment()
        return try {
            apiService.register(name, email, pass)
        } finally {
            EspressoIdlingResource.decrement()
        }
    }

    suspend fun login(email: String, pass: String): LoginResponse {
        EspressoIdlingResource.increment()
        return try {
            apiService.login(email, pass)
        } finally {
            EspressoIdlingResource.decrement()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService,
        ): UserRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserRepository(userPreference, apiService)
            }.also { INSTANCE = it }
    }
}
