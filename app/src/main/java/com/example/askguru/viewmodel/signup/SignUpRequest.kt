package com.example.askguru.viewmodel.signup

data class SignUpRequest(
    val new_user: NewUser
)

data class NewUser(
    val email: String,
    val password: String
)