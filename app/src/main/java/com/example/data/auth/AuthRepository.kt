package com.example.data.auth

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<AuthUser?>

    suspend fun signInWithGoogle(context: Context): Result<AuthUser>

    suspend fun signInWithFacebook(activity: Activity): Result<AuthUser>

    suspend fun signInWithEmail(email: String, password: String): Result<AuthUser>

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        name: String,
        grade: String
    ): Result<AuthUser>

    fun signOut()
}
