package com.example.askguru.viewmodel.add_song

data class SearchResponse(
    val results: List<SearchList>
)

data class SearchList(
    val attributes: Attributes,
    val href: String,
    val id: String,
    val type: String,
    var isSelected: Boolean = false
)

data class Attributes(
    val albumName: String,
    val artistName: String,
    val artwork: Artwork,
    val composerName: String,
    val contentRating: String,
    val discNumber: Int,
    val durationInMillis: Int,
    val genreNames: List<String>,
    val hasLyrics: Boolean,
    val isAppleDigitalMaster: Boolean,
    val isrc: String,
    val name: String,
    val playParams: PlayParams,
    val previews: List<Preview>,
    val releaseDate: String,
    val trackNumber: Int,
    val url: String
)

data class Artwork(
    val bgColor: String,
    val height: Int,
    val textColor1: String,
    val textColor2: String,
    val textColor3: String,
    val textColor4: String,
    val url: String,
    val width: Int
)

data class PlayParams(
    val id: String,
    val kind: String
)

data class Preview(
    val url: String
)