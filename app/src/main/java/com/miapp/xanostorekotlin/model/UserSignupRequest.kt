package com.miapp.xanostorekotlin.model

data class UserSignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String // "admin" o "client"
)