package com.example.askguru.viewmodel.login

import androidx.lifecycle.ViewModel
import com.example.askguru.network.Resource

import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.liveData
import com.example.askguru.repository.MainRepository

class LoginViewModel(private val mainRepository: MainRepository): ViewModel() {


    fun getLogin(email:String,password:String,grant_type:String,scope:String,client_id:String,client_secret:String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.login(email,password,grant_type,scope,client_id,client_secret)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getUserData(token:String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.getUserData(token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

}