package com.example.askguru.ui.qr
import android.os.Parcelable

import kotlinx.parcelize.Parcelize


@Parcelize
data class PlayListByIdResponse(val playlist: Playlist) : Parcelable

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
    val recommendations: List<RecommendationData>,
    val song_duration: Int,
    val song_id: String,
    val song_title: String,
    val song_url: String,
    val updated_on: String,
    val username: String
) : Parcelable

@Parcelize
data class RecommendationData(
    val accepted: Boolean,
    val accepted_by: String,
    val artist_name: String,
    val artwork: String,
    val created_on: String,
    val owner: String,
    val playlist_id: String,
    val recommendation_id: String,
    val recommender_id: String,
    val song_duration: Int,
    val song_id: String,
    val song_like_count: Int,
    val song_title: String,
    val song_url: String,
    val updated_on: String,
    val username: String
) : Parcelable