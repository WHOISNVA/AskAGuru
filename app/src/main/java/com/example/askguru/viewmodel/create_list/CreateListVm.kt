package com.example.askguru.viewmodel.create_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.askguru.network.Resource
import com.example.askguru.repository.MainRepository
import com.example.askguru.viewmodel.add_song.SearchList
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateListVm (private val mainRepository: MainRepository): ViewModel()  {

    fun createPlatList(token:String,genre:String,songId:String) = liveData(Dispatchers.IO) {

        val request =  CreatePlatListRequest(NewPlaylist(genre,songId))
        var gson = Gson()
        var jsonString = gson.toJson(request)
        Timber.e("jsonString ==>> $jsonString")

        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.createPlatList(token,request)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }








}