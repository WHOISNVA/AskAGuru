package com.example.askguru.viewmodel.add_song

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.askguru.network.Resource
import com.example.askguru.repository.MainRepository
import kotlinx.coroutines.Dispatchers

class AddSongVm(private val mainRepository: MainRepository): ViewModel()  {

    fun getSearchList(token: String,text:String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.getSearchList(token,text)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun addRecommendation(token: String, playListId:String, request: AddSuggestionRequest) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.addRecommendation(token,playListId,request)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

}