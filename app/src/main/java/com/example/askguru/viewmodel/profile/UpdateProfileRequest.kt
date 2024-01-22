package com.example.askguru.viewmodel.profile

data class UpdateProfileRequest(
    val users_update: UserProfileUpdate
)

data class UserProfileUpdate(
    val biography: String
)
