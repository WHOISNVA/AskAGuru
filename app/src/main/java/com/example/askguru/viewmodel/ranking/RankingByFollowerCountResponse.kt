package com.example.askguru.viewmodel.ranking

class RankingByFollowerCountResponse : ArrayList<RankingByFollowerCountResponseItem>()

data class RankingByFollowerCountResponseItem(
    val accepted_recs_count: Int,
    val biography: String,
    val created_on: String,
    val email: String,
    val email_verified: Boolean,
    val follower_data: FollowerData,
    val is_active: Boolean,
    val is_trending: Boolean,
    val profile_pic: Any,
    val total_listens: Int,
    val updated_on: String,
    val user_id: String,
    val username: String
)

data class FollowerData(
    val follower_count: Int,
    val followers: List<Follower>,
    val following: List<Following>,
    val following_count: Int
)

data class Follower(
    val followed_id: String,
    val follower_id: String
)

data class Following(
    val followed_id: String,
    val follower_id: String
)