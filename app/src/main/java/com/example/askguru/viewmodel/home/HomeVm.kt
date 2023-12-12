package com.example.askguru.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.askguru.network.Resource
import com.example.askguru.repository.MainRepository
import kotlinx.coroutines.Dispatchers

class HomeVm (private val mainRepository: MainRepository): ViewModel() {

    fun getAllPlayList() = liveData(
        Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.getAllPlayList()))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun like(token: String,id: String) = liveData(
        Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.like(token,id)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getPlayListById(playListId:String) = liveData(
        Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.getPlayListById(playListId)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

}