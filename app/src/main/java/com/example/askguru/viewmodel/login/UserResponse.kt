package com.example.askguru.viewmodel.login

data class UserResponse(
    val accepted_recs_count: Any,
    val access_token: Any,
    val biography: String,
    val created_on: String,
    val email: String,
    val email_verified: Boolean,
    val follower_data: FollowerData,
    val is_active: Boolean,
    val is_trending: Boolean,
    val profile_pic: String,
    val total_listens: Int,
    val updated_on: Any,
    val user_id: String,
    val username: String
)

data class FollowerData(
    val follower_count: Int,
    val followers: List<Any>,
    val following: List<Any>,
    val following_count: Int
)