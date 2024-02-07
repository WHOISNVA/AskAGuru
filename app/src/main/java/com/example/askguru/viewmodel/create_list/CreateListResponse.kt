package com.example.askguru.viewmodel.create_list

data class CreateListResponse(
    val artist_name: String,
    val artwork: String,
    val created_on: String,
    val genre: String,
    val is_trending: Boolean,
    val listens: Int,
    val playlist_like_count: Int,
    val playlist_name: Any,
    val song_duration: Int,
    val song_id: String,
    val song_title: String,
    val song_url: String,
    val updated_on: String
)