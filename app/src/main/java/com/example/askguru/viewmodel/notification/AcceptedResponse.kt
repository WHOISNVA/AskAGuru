package com.example.askguru.viewmodel.notification
import android.os.Parcelable

import kotlinx.parcelize.Parcelize


class AcceptedResponse : ArrayList<AcceptedResponseItem>()

@Parcelize
data class AcceptedResponseItem(
    val accepted: Boolean,
    val accepted_by: String,
    val artist_name: String,
    val artwork: String,
    val created_on: String,
    val playlist_id: String,
    val recommendation_id: String,
    val recommender_id: String,
    val song_duration: Int,
    val song_id: String,
    val song_like_count: Int,
    val song_title: String,
    val song_url: String,
    val updated_on: String
) : Parcelable