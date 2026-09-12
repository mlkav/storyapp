package com.rnlkav.storyapp.ui.add

import android.location.Location
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.rnlkav.storyapp.data.repository.StoryRepository
import com.rnlkav.storyapp.data.response.FileUploadResponse
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class AddStoryViewModel(private val repository: StoryRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _uploadResponse = MutableLiveData<FileUploadResponse>()
    val uploadResponse: LiveData<FileUploadResponse> = _uploadResponse

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _currentImageUri = MutableLiveData<Uri?>()
    val currentImageUri: LiveData<Uri?> = _currentImageUri

    private val _location = MutableLiveData<Location?>()
    val location: LiveData<Location?> = _location

    var tempCameraUri: Uri? = null

    fun setCurrentImageUri(uri: Uri?) {
        _currentImageUri.value = uri
    }

    fun setLocation(location: Location?) {
        _location.value = location
    }

    fun uploadImage(
        file: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody? = null,
        lon: RequestBody? = null
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.uploadImage(file, description, lat, lon)
                _uploadResponse.value = response
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, FileUploadResponse::class.java)
                _uploadResponse.value = errorResponse
            } catch (e: Exception) {
                _message.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
