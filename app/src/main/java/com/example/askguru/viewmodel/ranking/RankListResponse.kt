package com.example.askguru.viewmodel.ranking

class RankListResponse : ArrayList<RankListResponseItem>()

data class RankListResponseItem(
    val biography: String,
    val created_on: String,
    val email: String,
    val email_verified: Boolean,
    val is_active: Boolean,
    val is_trending: Boolean,
    val profile_pic: String,
    val total_listens: Int,
    val updated_on: String,
    val user_id: String,
    val username: String
)