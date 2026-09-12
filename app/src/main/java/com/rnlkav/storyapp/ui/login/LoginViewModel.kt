package com.rnlkav.storyapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.rnlkav.storyapp.data.pref.UserModel
import com.rnlkav.storyapp.data.repository.UserRepository
import com.rnlkav.storyapp.data.response.LoginResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginViewModel(private val repository: UserRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loginResponse = MutableLiveData<LoginResponse>()
    val loginResponse: LiveData<LoginResponse> = _loginResponse

    fun login(email: String, pass: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.login(email, pass)
                _loginResponse.value = response
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, LoginResponse::class.java)
                _loginResponse.value = errorResponse
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }
}
