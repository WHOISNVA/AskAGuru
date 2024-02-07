package com.example.askguru.viewmodel.create_list

data class CreatePlatListRequest(
    val new_playlist: NewPlaylist
)

data class NewPlaylist(
    val genre: String,
    val song_id: String
)