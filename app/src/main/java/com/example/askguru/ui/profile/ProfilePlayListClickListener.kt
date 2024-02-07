package com.example.askguru.ui.profile

import com.example.askguru.viewmodel.profile.MyPlaylists
import com.example.askguru.viewmodel.profile.Playlist

interface ProfilePlayListClickListener {
    fun onPlayListClicked(genre: Playlist, position: Int)

    fun onDeleteClicked(genre: Playlist, position: Int, myPlaylists: MyPlaylists)

    fun onShareClicked(list: Playlist, position: Int)
}