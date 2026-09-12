package com.rnlkav.storyapp.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.rnlkav.storyapp.data.repository.UserRepository
import com.rnlkav.storyapp.data.response.RegisterResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RegisterViewModel(private val repository: UserRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _registerResponse = MutableLiveData<RegisterResponse>()
    val registerResponse: LiveData<RegisterResponse> = _registerResponse

    fun register(name: String, email: String, pass: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.register(name, email, pass)
                _registerResponse.value = response
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, RegisterResponse::class.java)
                _registerResponse.value = errorResponse
            } finally {
                _isLoading.value = false
            }
        }
    }
}
