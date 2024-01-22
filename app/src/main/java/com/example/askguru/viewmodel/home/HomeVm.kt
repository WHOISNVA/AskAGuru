package com.example.askguru.viewmodel.home

import android.widget.Toast
import androidx.lifecycle.*
import com.example.askguru.network.Resource
import com.example.askguru.network.Status
import com.example.askguru.repository.MainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class HomeVm (private val mainRepository: MainRepository): ViewModel() {

    val playlistsLiveData = MutableLiveData<Resource<PlayListResponse>>()

    val randomPlaylist = MutableLiveData<Playlist>()

    fun getAllPlayList() = liveData(
        Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.getAllPlayList()))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun like(token: String,id: String,isLike : Boolean = true) = liveData(
        Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            if(isLike){
                emit(Resource.success(data = mainRepository.disLike(token,id)))
            }else{
                emit(Resource.success(data = mainRepository.like(token,id)))
            }
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


    fun getAllPlayListRandom() {
        playlistsLiveData.value?.let {
            if (it.status == Status.SUCCESS ) {
                getRandomSong()
            }else{
                callAPI()
            }
        } ?: kotlin.run {
            callAPI()
        }
    }

    private fun callAPI(){
        viewModelScope.launch(Dispatchers.IO) {
            playlistsLiveData.postValue(Resource.loading(data = null))
            try {
                val playlists = mainRepository.getAllPlayList()
                playlistsLiveData.postValue(Resource.success(data = playlists))
            } catch (exception: Exception) {
                playlistsLiveData.postValue(
                    Resource.error(data = null, message = exception.message ?: "Error Occurred!")
                )
            }
        }
    }

    fun getRandomSong(){
        playlistsLiveData?.let {resource ->
            resource.value?.data?.let {
                val randomIndex = (0 until it.size).random()
                if(randomIndex >= 0){
                    randomPlaylist.value = it[randomIndex].playlist
                }
            }
        }
    }
}