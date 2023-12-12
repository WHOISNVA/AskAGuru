package com.example.askguru.ui.playlist

import com.example.askguru.viewmodel.home.Recommendation

interface AddSongClickListener {

    fun onAddSongClick(model: Recommendation?, position: Int)
}