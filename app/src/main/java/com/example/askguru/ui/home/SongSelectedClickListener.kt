package com.example.askguru.ui.home

import com.example.askguru.viewmodel.home.PlayListResponseItem

interface SongSelectedClickListener {

    fun onLikeClicked(playlistId: String, list: PlayListResponseItem, position: Int)



    fun onPlaylistClick(model: PlayListResponseItem)

    fun onPlayClick(list: PlayListResponseItem)
}