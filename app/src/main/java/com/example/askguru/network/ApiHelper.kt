package com.example.askguru.network

import com.example.askguru.viewmodel.add_song.AddSuggestionRequest
import com.example.askguru.viewmodel.create_list.CreatePlatListRequest
import com.example.askguru.viewmodel.profile.UpdateProfileRequest
import com.example.askguru.viewmodel.signup.SignUpRequest
import okhttp3.MultipartBody
import retrofit2.http.Part


class ApiHelper(private val apiService: ApiService) {

    suspend fun login(
        email: String,
        password: String,
        grant_type: String,
        scope: String,
        client_id: String,
        client_secret: String
    ) = apiService.login(email, password, grant_type, scope, client_id, client_secret)

    suspend fun signUp(request: SignUpRequest) = apiService.signUp(request)

    suspend fun getAllPlayList() = apiService.getAllPlayList()
    suspend fun getPlayListById(playListId: String) = apiService.getPlayListById(playListId)
    suspend fun getRankList() = apiService.getRankList()
    suspend fun getRankByFollowerList() = apiService.getRankByFollowerList()
    suspend fun getRankByRecommendedList() = apiService.getRankByRecommendedList()

    suspend fun getRankingProfileData(userId: String) = apiService.getRankingProfileData(userId)
    suspend fun getProfileData(token: String) = apiService.getProfileData(token)

    suspend fun like(token: String, id: String) = apiService.like(token, id)
    suspend fun disLike(token: String, id: String) = apiService.disLike(token, id)

    suspend fun getSearchList(token: String, text: String) = apiService.getSearchList(token, text)


    suspend fun createPlatList(token: String, request: CreatePlatListRequest) =
        apiService.createPlatList(token, request)

    suspend fun getUserData(token: String) = apiService.getUserData(token)

    suspend fun follow(token: String,userId:String) = apiService.follow(token,userId)
    suspend fun unFollow(token: String,userId:String) = apiService.unFollow(token,userId)


    suspend fun addRecommendation(token: String, playListId:String, request: AddSuggestionRequest) = apiService.addRecommendation(token,"application/json",playListId,request)

    suspend fun acceptedRequest(token: String, recommendationId:String) = apiService.acceptedRequest(token,recommendationId)

    suspend fun rejectRequest(token: String, recommendationId:String) = apiService.rejectRequest(token,recommendationId)

    suspend fun removePlayList(token: String, playListId:String) = apiService.removePlayList(token,playListId)

    suspend fun uploadProfile(token: String, images: List<MultipartBody.Part>) = apiService.uploadProfile(token,images)
    suspend fun updateBio(token: String, request: UpdateProfileRequest) = apiService.updateBio(token,request)

}