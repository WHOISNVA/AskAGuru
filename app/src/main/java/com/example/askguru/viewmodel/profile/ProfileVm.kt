package com.example.askguru.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.askguru.network.Resource
import com.example.askguru.repository.MainRepository
import com.example.askguru.viewmodel.add_song.AddSuggestionRequest
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody

class ProfileVm (private val mainRepository: MainRepository): ViewModel() {

    fun getRankingProfileData(userId: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.getRankingProfileData(userId)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


    fun getProfileData(token: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.getProfileData(token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }




    fun followUnFollow(token:String, userId: String,isFollowing : Boolean) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            if(isFollowing){
                emit(Resource.success(data = mainRepository.unFollow(token,userId)))
            }else{
                emit(Resource.success(data = mainRepository.follow(token,userId)))
            }
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


    fun removePlayList(token:String,userId: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.removePlayList(token,userId)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


    fun uploadProfilePic(token: String,images: List<MultipartBody.Part>) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.uploadProfile(token,images)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun updateBio(token: String,request: UpdateProfileRequest) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.updateBio(token,request)))
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