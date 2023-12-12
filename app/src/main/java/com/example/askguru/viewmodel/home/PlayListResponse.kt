package com.example.askguru.viewmodel.home

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class PlayListResponse : ArrayList<PlayListResponseItem>(), Serializable

data class PlayListResponseItem(
    @SerializedName("playlist")
    var playlist: Playlist = Playlist()
) : Serializable

data class Playlist(
    @SerializedName("playlist_id")
    var playlistId: String = "",
    @SerializedName("owner")
    var owner: String = "",
    @SerializedName("playlist_name")
    var playlistName: Any? = Any(),
    @SerializedName("song_title")
    var songTitle: String = "",
    @SerializedName("artist_name")
    var artistName: String = "",
    @SerializedName("song_url")
    var songUrl: String = "",
    @SerializedName("created_on")
    var createdOn: String = "",
    @SerializedName("updated_on")
    var updatedOn: String = "",
    @SerializedName("artwork")
    var artwork: String = "",
    @SerializedName("song_id")
    var songId: String = "",
    @SerializedName("genre")
    var genre: String = "",
    @SerializedName("listens")
    var listens: Int = 0,
    @SerializedName("is_trending")
    var isTrending: Boolean = false,
    @SerializedName("song_duration")
    var songDuration: Int = 0,
    @SerializedName("username")
    var username: String = "",
    @SerializedName("recommendations")
    var recommendations: List<Recommendation> = listOf(),
    @SerializedName("playlist_like_count")
    var playlistLikeCount: Int = 0,
    @SerializedName("spotipy_id")
    var spotipyId: String? = null
) : Serializable

data class Recommendation(
    @SerializedName("playlist_id")
    var playlistId: String = "",
    @SerializedName("song_title")
    var songTitle: String = "",
    @SerializedName("artist_name")
    var artistName: String = "",
    @SerializedName("song_url")
    var songUrl: String = "",
    @SerializedName("created_on")
    var createdOn: String = "",
    @SerializedName("updated_on")
    var updatedOn: String = "",
    @SerializedName("artwork")
    var artwork: String = "",
    @SerializedName("accepted_by")
    var acceptedBy: String? = "",
    @SerializedName("song_id")
    var songId: String = "",
    @SerializedName("song_duration")
    var songDuration: Int = 0,
    @SerializedName("recommendation_id")
    var recommendationId: String = "",
    @SerializedName("recommender_id")
    var recommenderId: String = "",
    @SerializedName("accepted")
    var accepted: Boolean? = false,
    @SerializedName("song_like_count")
    var songLikeCount:  Int = 0,
    @SerializedName("owner")
    var owner: String? = "",
    @SerializedName("username")
    var username: String? = "",
    @SerializedName("spotipy_id")
    var spotipyId: String? = null
) : Serializable

//class PlayListResponse : ArrayList<PlayListResponseItem>()
//
//data class PlayListResponseItem(
//    val playlist: Playlist
//)
//
//data class Playlist(
//    val artist_name: String,
//    val artwork: String,
//    val created_on: String,
//    val genre: String,
//    val is_trending: Boolean,
//    val listens: Int,
//    val owner: String,
//    val playlist_id: String,
//    var playlist_like_count: Int,
//    val playlist_name: Any,
//    val recommendations: List<Recommendation>,
//    val song_duration: Int,
//    val song_id: String,
//    val song_title: String,
//    val song_url: String,
//    val updated_on: String,
//    val username: String
//)
//
//data class Recommendation(
//    val accepted: Boolean,
//    val accepted_by: String,
//    val artist_name: String,
//    val artwork: String,
//    val created_on: String,
//    val owner: String,
//    val playlist_id: String,
//    val recommendation_id: String,
//    val recommender_id: String,
//    val song_duration: Int,
//    val song_id: String,
//    val song_like_count: Any,
//    val song_title: String,
//    val song_url: String,
//    val updated_on: String,
//    val username: String
//)