package com.rnlkav.storyapp.ui.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.rnlkav.storyapp.data.repository.UserRepository
import com.rnlkav.storyapp.data.response.FileUploadResponse
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class AddStoryViewModel(private val repository: UserRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _uploadResponse = MutableLiveData<FileUploadResponse>()
    val uploadResponse: LiveData<FileUploadResponse> = _uploadResponse

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _currentImageUri = MutableLiveData<Uri?>()
    val currentImageUri: LiveData<Uri?> = _currentImageUri

    fun setCurrentImageUri(uri: Uri?) {
        _currentImageUri.value = uri
    }

    fun uploadImage(file: MultipartBody.Part, description: RequestBody) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.uploadImage(file, description)
                _uploadResponse.value = response
            } catch (e: HttpException) {
                _message.value = e.message()
            } catch (e: Exception) {
                _message.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
