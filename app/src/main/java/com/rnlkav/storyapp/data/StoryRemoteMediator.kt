package com.rnlkav.storyapp.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.rnlkav.storyapp.data.api.ApiService
import com.rnlkav.storyapp.data.database.RemoteKeys
import com.rnlkav.storyapp.data.database.StoryDatabase
import com.rnlkav.storyapp.data.database.StoryEntity

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val database: StoryDatabase,
    private val apiService: ApiService
) : RemoteMediator<Int, StoryEntity>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, StoryEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyAtClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }

            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }

            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val responseData = apiService.getStories(page, state.config.pageSize)

            val endOfPaginationReached = responseData.listStory.isNullOrEmpty()

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeysDao().deleteRemoteKeys()
                    database.storyDao().deleteAll()
                }
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1

                val stories = responseData.listStory?.map {
                    StoryEntity(
                        id = it.id ?: "",
                        name = it.name,
                        description = it.description,
                        photoUrl = it.photoUrl,
                        createdAt = it.createdAt,
                        lat = it.lat,
                        lon = it.lon
                    )
                }

                val keys = stories?.map {
                    RemoteKeys(id = it.id, prevKey = prevKey, nextKey = nextKey)
                }

                if (keys != null) {
                    database.remoteKeysDao().insertAll(keys)
                }
                if (stories != null) {
                    database.storyDao().insertStory(stories)
                }
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: Exception) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            withContext(Dispatchers.IO) {
                database.remoteKeysDao().getRemoteKeysId(data.id)
            }
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            withContext(Dispatchers.IO) {
                database.remoteKeysDao().getRemoteKeysId(data.id)
            }
        }
    }

    private suspend fun getRemoteKeyAtClosestToCurrentPosition(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                withContext(Dispatchers.IO) {
                    database.remoteKeysDao().getRemoteKeysId(id)
                }
            }
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}
