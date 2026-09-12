package com.rnlkav.storyapp.data.repository

import com.rnlkav.storyapp.data.api.ApiService
import com.rnlkav.storyapp.data.pref.UserModel
import com.rnlkav.storyapp.data.pref.UserPreference
import com.rnlkav.storyapp.data.response.FileUploadResponse
import com.rnlkav.storyapp.data.response.LoginResponse
import com.rnlkav.storyapp.data.response.RegisterResponse
import com.rnlkav.storyapp.data.response.StoryResponse
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

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
        return apiService.register(name, email, pass)
    }

    suspend fun login(email: String, pass: String): LoginResponse {
        return apiService.login(email, pass)
    }

    suspend fun getStories(): StoryResponse {
        return apiService.getStories()
    }

    suspend fun uploadImage(
        file: MultipartBody.Part,
        description: RequestBody,
    ): FileUploadResponse {
        return apiService.uploadImage(file, description)
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
