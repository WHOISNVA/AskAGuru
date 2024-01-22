package com.example.askguru.repository

import com.example.askguru.network.ApiHelper
import com.example.askguru.viewmodel.add_song.AddSuggestionRequest
import com.example.askguru.viewmodel.create_list.CreatePlatListRequest
import com.example.askguru.viewmodel.profile.UpdateProfileRequest
import com.example.askguru.viewmodel.signup.SignUpRequest
import okhttp3.MultipartBody


class MainRepository(private val apiHelper: ApiHelper) {

    suspend fun login(email:String,password:String,grant_type:String,scope:String,client_id:String,client_secret:String) = apiHelper.login(email,password,grant_type,scope,client_id,client_secret)
    suspend fun signUp(request: SignUpRequest) = apiHelper.signUp(request)
    suspend fun getAllPlayList() = apiHelper.getAllPlayList()
    suspend fun getPlayListById(playListId: String) = apiHelper.getPlayListById(playListId)

    suspend fun getRankList() = apiHelper.getRankList()
    suspend fun getRankByFollowerList() = apiHelper.getRankByFollowerList()
    suspend fun getRankByRecommendedList() = apiHelper.getRankByRecommendedList()
    suspend fun getRankingProfileData(userId: String) = apiHelper.getRankingProfileData(userId)
    suspend fun getProfileData(token: String) = apiHelper.getProfileData(token)
    suspend fun like(token: String,id: String) = apiHelper.like(token,id)
    suspend fun disLike(token: String,id: String) = apiHelper.disLike(token,id)

    suspend fun getSearchList(token: String,text: String) = apiHelper.getSearchList(token,text)

    suspend fun createPlatList(token:String,request: CreatePlatListRequest) = apiHelper.createPlatList(token,request)

    suspend fun getUserData(token: String) = apiHelper.getUserData(token)

    suspend fun follow(token: String,userId:String) = apiHelper.follow(token,userId)
    suspend fun unFollow(token: String,userId:String) = apiHelper.unFollow(token,userId)

    suspend fun addRecommendation(token: String, playListId:String, request: AddSuggestionRequest) = apiHelper.addRecommendation(token,playListId,request)

    suspend fun acceptedRequest(token: String, recommendationId:String) = apiHelper.acceptedRequest(token,recommendationId)

    suspend fun rejectRequest(token: String, recommendationId:String) = apiHelper.rejectRequest(token,recommendationId)


    suspend fun removePlayList(token: String, playListId:String) = apiHelper.removePlayList(token,playListId)

    suspend fun uploadProfile(token: String, images: List<MultipartBody.Part>) = apiHelper.uploadProfile(token,images)
    suspend fun updateBio(token: String,  request: UpdateProfileRequest) = apiHelper.updateBio(token,request)
}