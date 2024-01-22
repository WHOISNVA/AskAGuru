package com.example.askguru.network


import com.example.askguru.ui.profile.ProfileUserDataResponse
import com.example.askguru.ui.qr.PlayListByIdResponse
import com.example.askguru.viewmodel.LoginResponse
import com.example.askguru.viewmodel.add_song.AddSuggestionRequest
import com.example.askguru.viewmodel.add_song.SearchResponse
import com.example.askguru.viewmodel.create_list.CreateListResponse
import com.example.askguru.viewmodel.create_list.CreatePlatListRequest
import com.example.askguru.viewmodel.home.LikeResponse
import com.example.askguru.viewmodel.home.PlayListResponse
import com.example.askguru.viewmodel.login.UserResponse
import com.example.askguru.viewmodel.notification.AcceptedResponse
import com.example.askguru.viewmodel.profile.ProfileDataResponse
import com.example.askguru.viewmodel.profile.UpdateProfileRequest
import com.example.askguru.viewmodel.profile.UploadResponse
import com.example.askguru.viewmodel.ranking.RankListResponse
import com.example.askguru.viewmodel.ranking.RankingByFollowerCountResponse
import com.example.askguru.viewmodel.ranking.RankingByRecommendedResponse
import com.example.askguru.viewmodel.signup.SignUpRequest
import com.example.askguru.viewmodel.signup.SignupResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("login/token")
    suspend fun login(
        @Field("username") email: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String,
        @Field("scope") scope: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): LoginResponse

    @GET("me")
    suspend fun getUserData(@Header("Authorization") token: String): UserResponse

    @POST("users")
    suspend fun signUp(@Body dataModal: SignUpRequest): SignupResponse

    @GET("playlists")
    suspend fun getAllPlayList(): PlayListResponse

    @GET("playlists/{Id}/")
    suspend fun getPlayListById(@Path("Id") playListId: String): PlayListByIdResponse

    @GET("rankings")
    suspend fun getRankList(): RankListResponse

    @GET("rankings_by_follower_count")
    suspend fun getRankByFollowerList(): RankingByFollowerCountResponse

    @GET("rankings_by_accepted_recommendations")
    suspend fun getRankByRecommendedList(): RankingByRecommendedResponse


    @POST("playlists/{Id}/like")
    suspend fun like(@Header("Authorization") token: String, @Path("Id") userId: String): LikeResponse

    @DELETE("playlists/{Id}/like")
    suspend fun disLike(@Header("Authorization") token: String, @Path("Id") userId: String): LikeResponse

    @POST("search")
    suspend fun getSearchList(@Header("Authorization") token: String,@Query("request") searchText: String): SearchResponse


    @POST("playlists")
    suspend fun createPlatList(
        @Header("Authorization") token: String,
        @Body request: CreatePlatListRequest
    ): CreateListResponse


    @POST("{Id}/follow")
    suspend fun follow(@Header("Authorization") token: String, @Path("Id") userId: String): String

    @DELETE("users/{Id}/follow")
    suspend fun unFollow(@Header("Authorization") token: String, @Path("Id") userId: String): String

    @POST("playlists/{Id}") //application/json
    suspend fun addRecommendation(
        @Header("Authorization") token: String,
        @Header("Content-Type") application: String,
        @Path("Id") playListId: String,
        @Body request: AddSuggestionRequest
    ): LikeResponse


    @PUT("recommendations/{Id}")
    suspend fun acceptedRequest(
        @Header("Authorization") application: String,
        @Path("Id") recommendationId: String
    ): AcceptedResponse

    @DELETE("recommendations/{Id}/")
    suspend fun rejectRequest(
        @Header("Authorization") application: String,
        @Path("Id") recommendationId: String
    ): String


    @DELETE("playlists/{Id}/")
    suspend fun removePlayList(
        @Header("Authorization") application: String,
        @Path("Id") recommendationId: String
    ): String



    @GET("playlists/me")
    suspend fun getProfileData(@Header("Authorization") token: String): ProfileUserDataResponse

    @GET("users/{Id}")
    suspend fun getRankingProfileData(@Path("Id") userId: String): ProfileDataResponse

    @Multipart
    @POST("uploadfile/profile")
    suspend fun uploadProfile(
        @Header("Authorization") token: String,
        @Part images: List<MultipartBody.Part> = emptyList(),
    ): Response<UploadResponse>

    @PUT("me")
    suspend fun updateBio(
        @Header("Authorization") application: String,
        @Body request: UpdateProfileRequest
    ): String
}