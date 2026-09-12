package com.rnlkav.storyapp.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.rnlkav.storyapp.data.StoryRemoteMediator
import com.rnlkav.storyapp.data.api.ApiService
import com.rnlkav.storyapp.data.database.StoryDatabase
import com.rnlkav.storyapp.data.database.StoryEntity
import com.rnlkav.storyapp.data.response.FileUploadResponse
import com.rnlkav.storyapp.data.response.StoryResponse
import com.rnlkav.storyapp.utils.EspressoIdlingResource
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

class StoryRepository private constructor(
    private val database: StoryDatabase,
    private val apiService: ApiService,
) {
    fun getStories(): Flow<PagingData<StoryEntity>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 20
            ),
            remoteMediator = StoryRemoteMediator(database, apiService),
            pagingSourceFactory = {
                database.storyDao().getAllStory()
            }
        ).flow
    }

    suspend fun getStoriesRaw(): StoryResponse {
        return apiService.getStories()
    }

    suspend fun getStoriesWithLocation(): StoryResponse {
        EspressoIdlingResource.increment()
        return try {
            apiService.getStoriesWithLocation()
        } finally {
            EspressoIdlingResource.decrement()
        }
    }

    suspend fun uploadImage(
        file: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody? = null,
        lon: RequestBody? = null,
    ): FileUploadResponse {
        EspressoIdlingResource.increment()
        return try {
            apiService.uploadImage(file, description, lat, lon)
        } finally {
            EspressoIdlingResource.decrement()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: StoryRepository? = null
        fun getInstance(
            database: StoryDatabase,
            apiService: ApiService,
        ): StoryRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: StoryRepository(database, apiService)
            }.also { INSTANCE = it }
    }
}
