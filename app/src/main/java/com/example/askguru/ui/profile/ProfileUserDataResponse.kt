package com.example.askguru.ui.profile
import android.os.Parcelable
import com.example.askguru.viewmodel.home.Recommendation

import kotlinx.parcelize.Parcelize


@Parcelize
data class ProfileUserDataResponse(
    val playlists: Playlists
) : Parcelable

@Parcelize
data class Playlists(
    val contributions: List<Contribution>,
    val liked_playlists: List<LikedPlaylists>,
    val my_playlists: List<MyPlaylists>
) : Parcelable

@Parcelize
data class Contribution(
    val playlist: Playlist
) : Parcelable

@Parcelize
data class LikedPlaylists(
    val playlist: Playlist
) : Parcelable

@Parcelize
data class MyPlaylists(
    val playlist: UserPlaylist
) : Parcelable

@Parcelize
data class Playlist(
    val artist_name: String,
    val artwork: String,
    val created_on: String,
    val genre: String,
    val is_trending: Boolean,
    val listens: Int,
    val owner: String,
    val playlist_id: String,
    val playlist_like_count: Int,
    val playlist_name: String,
    val recommendations: List<Recommendation>,
    val song_duration: Int,
    val song_id: String,
    val song_title: String,
    val song_url: String,
    val updated_on: String,
    val username: String,
    val spotipy_id: String,
    ) : Parcelable

@Parcelize
data class UserPlaylist(
    val artist_name: String,
    val artwork: String,
    val created_on: String,
    val genre: String,
    val is_trending: Boolean,
    val listens: Int,
    val owner: String,
    val playlist_id: String,
    val playlist_like_count: Int,
    val playlist_name: String,
    val recommendations: ArrayList<Recommendation>,
    val song_duration: Int,
    val song_id: String,
    val song_title: String,
    val song_url: String,
    val updated_on: String,
    val username: String,
    val spotipy_id: String
) : Parcelable

//@Parcelize
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
//    val song_like_count: Int,
//    val song_title: String,
//    val song_url: String,
//    val updated_on: String,
//    val username: String
//) : Parcelable