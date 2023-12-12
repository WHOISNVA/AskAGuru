package com.example.askguru.viewmodel.signup


data class SignupResponse(
    val accepted_recs_count: Any,
    val access_token: AccessToken,
    val biography: Any,
    val created_on: String,
    val email: String,
    val email_verified: Boolean,
    val follower_data: Any,
    val is_active: Boolean,
    val is_trending: Boolean,
    val profile_pic: String,
    val total_listens: Int,
    val updated_on: String,
    val user_id: String,
    val username: String
)

data class AccessToken(
    val access_token: String,
    val token_type: String
)