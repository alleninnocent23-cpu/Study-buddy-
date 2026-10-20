package com.example.data.auth

data class AuthUser(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String? = null,
    val providerId: String = "firebase",
    val grade: String = "High School"
)
