package com.example.askguru.viewmodel.add_song
import android.os.Parcelable

import kotlinx.parcelize.Parcelize


@Parcelize
data class AddSuggestionRequest(
    val new_recommendations: List<NewRecommendation>
) : Parcelable

@Parcelize
data class NewRecommendation(
    val song_id: String
) : Parcelable