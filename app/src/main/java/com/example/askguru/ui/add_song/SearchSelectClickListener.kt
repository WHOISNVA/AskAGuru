package com.example.askguru.ui.add_song

import com.example.askguru.viewmodel.add_song.SearchList

interface SearchSelectClickListener {

    fun onClickSong(position: Int, list: SearchList)
}