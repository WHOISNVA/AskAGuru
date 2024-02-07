package com.example.askguru.viewmodel.ranking

class RankingByRecommendedResponse : ArrayList<RankingByRecommendedItem>()

data class RankingByRecommendedItem(
    val accepted_recs_count: Int,
    val user: User
)

data class User(
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