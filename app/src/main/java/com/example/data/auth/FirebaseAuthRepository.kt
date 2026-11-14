package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

class FirebaseAuthRepository(
    context: Context,
    private val userDao: UserDao
) : AuthRepository {

    private val appContext: Context = context.applicationContext ?: context
    private val tag = "FirebaseAuthRepo"

    private val sessionPrefs = appContext.getSharedPreferences(
        "studybuddy_auth_session_prefs",
        Context.MODE_PRIVATE
    )

    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            if (FirebaseApp.getApps(appContext).isNotEmpty()) {
                Firebase.auth
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(tag, "Firebase Auth not initialized: ${e.message}")
            null
        }
    }

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        val fbUser = auth.currentUser
        if (fbUser != null) {
            val email = fbUser.email.orEmpty()
            val mapped = AuthUser(
                uid = fbUser.uid,
                displayName = fbUser.displayName?.takeIf { it.isNotBlank() }
                    ?: deriveNameFromEmail(email),
                email = email,
                photoUrl = fbUser.photoUrl?.toString(),
                providerId = fbUser.providerData.lastOrNull()?.providerId ?: "google.com"
            )
            _currentUser.value = mapped
            if (email.isNotBlank()) {
                markLocalSessionActive(email)
            }
        }
    }

    init {
        firebaseAuth?.addAuthStateListener(authStateListener)
    }

    override fun hasPersistedSession(): Boolean {
        val fbUser = try {
            firebaseAuth?.currentUser
        } catch (_: Exception) {
            null
        }
        if (fbUser != null) return true
        return sessionPrefs.getBoolean(KEY_IS_SIGNED_IN, false) &&
            !sessionPrefs.getString(KEY_ACTIVE_EMAIL, null).isNullOrBlank()
    }

    override suspend fun restoreSignedInUser(): AuthUser? = withContext(Dispatchers.IO) {
        val fbUser = try {
            firebaseAuth?.currentUser
        } catch (_: Exception) {
            null
        }

        if (fbUser != null) {
            val email = fbUser.email.orEmpty()
            val existingLocal = if (email.isNotBlank()) userDao.getUserByEmail(email) else null
            val resolvedName = fbUser.displayName?.takeIf { it.isNotBlank() }
                ?: existingLocal?.name
                ?: deriveNameFromEmail(email)
            val resolvedPhoto = fbUser.photoUrl?.toString() ?: existingLocal?.profilePhotoUri
            val resolvedGrade = existingLocal?.grade ?: "High School (Grades 9-12)"

            val restored = AuthUser(
                uid = fbUser.uid,
                displayName = resolvedName,
                email = email,
                photoUrl = resolvedPhoto,
                providerId = fbUser.providerData.lastOrNull()?.providerId ?: "google.com",
                grade = resolvedGrade
            )
            if (email.isNotBlank()) {
                saveOrUpdateLocalUser(restored)
                markLocalSessionActive(email)
            }
            _currentUser.value = restored
            return@withContext restored
        }

        if (!sessionPrefs.getBoolean(KEY_IS_SIGNED_IN, false)) {
            return@withContext null
        }

        val savedEmail = sessionPrefs.getString(KEY_ACTIVE_EMAIL, null)?.trim().orEmpty()
        val localUser = if (savedEmail.isNotBlank()) {
            userDao.getUserByEmail(savedEmail)
        } else {
            userDao.getLatestUser()
        } ?: return@withContext null

        val restoredLocal = AuthUser(
            uid = "local_${localUser.id}",
            displayName = localUser.name,
            email = localUser.email,
            photoUrl = localUser.profilePhotoUri,
            providerId = "local",
            grade = localUser.grade
        )
        _currentUser.value = restoredLocal
        restoredLocal
    }

    override suspend fun signInWithGoogle(context: Context): Result<AuthUser> {
        val activity = context.findActivity()
            ?: return Result.failure(
                IllegalStateException("Google Sign-In requires an active screen context.")
            )

        val webClientId = try {
            appContext.getString(R.string.default_web_client_id).trim()
        } catch (e: Exception) {
            Log.e(tag, "default_web_client_id resource lookup failed", e)
            ""
        }

        if (webClientId.isBlank()) {
            return Result.failure(
                IllegalStateException("Google Sign-In configuration (default_web_client_id) is missing.")
            )
        }

        val credentialManager = CredentialManager.create(activity)
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
            .build()

        val primaryRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        val credentialResult = try {
            // Credential Manager requires Main thread + Activity context to present the Google picker
            withContext(Dispatchers.Main) {
                credentialManager.getCredential(context = activity, request = primaryRequest)
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(tag, "User cancelled Google Sign-In picker: ${e.message}", e)
            return Result.failure(e)
        } catch (primaryError: GetCredentialException) {
            Log.w(
                tag,
                "Primary GetSignInWithGoogleOption failed (${primaryError.type}); attempting GetGoogleIdOption fallback",
                primaryError
            )
            // Keep 1 credential option per request for Android 16 bottom-sheet responsiveness
            val fallbackGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()
            val fallbackRequest = GetCredentialRequest.Builder()
                .addCredentialOption(fallbackGoogleIdOption)
                .build()
            try {
                withContext(Dispatchers.Main) {
                    credentialManager.getCredential(context = activity, request = fallbackRequest)
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(tag, "User cancelled Google ID fallback picker: ${e.message}", e)
                return Result.failure(e)
            } catch (e: NoCredentialException) {
                Log.w(tag, "No Google account available on device: ${e.message}", e)
                return Result.failure(
                    IllegalStateException(
                        "No Google account is signed in on this device. Add a Google account in device Settings and try again."
                    )
                )
            } catch (e: GetCredentialException) {
                Log.e(tag, "CredentialManager Google Sign-In failed (${e.type}): ${e.errorMessage ?: e.message}", e)
                val detail = e.errorMessage?.toString()?.takeIf { it.isNotBlank() }
                    ?: primaryError.errorMessage?.toString()?.takeIf { it.isNotBlank() }
                    ?: e.localizedMessage
                    ?: "Google Sign-In failed. Please check Google Play Services and try again."
                return Result.failure(IllegalStateException(detail, e))
            } catch (e: Exception) {
                Log.e(tag, "Unexpected error during Google ID fallback", e)
                return Result.failure(
                    IllegalStateException(e.localizedMessage ?: "Unable to open Google Sign-In.", e)
                )
            }
        } catch (e: Exception) {
            Log.e(tag, "Unexpected error launching Google Sign-In", e)
            return Result.failure(
                IllegalStateException(e.localizedMessage ?: "Unable to open Google Sign-In.", e)
            )
        }

        return withContext(Dispatchers.IO) {
            try {
                val credential = credentialResult.credential
                if (credential !is CustomCredential ||
                    credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    return@withContext Result.failure(
                        IllegalStateException("Unsupported credential returned by Google Sign-In.")
                    )
                }

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val googleEmail = googleIdTokenCredential.id.trim()
                val googleDisplayName = googleIdTokenCredential.displayName
                    ?.takeIf { it.isNotBlank() && !it.equals("Google Student", ignoreCase = true) }
                    ?: deriveNameFromEmail(googleEmail)
                val googlePhotoUrl = googleIdTokenCredential.profilePictureUri?.toString()

                val auth = firebaseAuth
                    ?: return@withContext Result.failure(
                        IllegalStateException("Firebase Authentication is not initialized.")
                    )

                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val fbUser = authResult.user
                    ?: return@withContext Result.failure(
                        IllegalStateException("Google Sign-In did not return an authenticated Firebase user.")
                    )

                val resolvedEmail = fbUser.email?.takeIf { it.isNotBlank() } ?: googleEmail
                val resolvedName = fbUser.displayName
                    ?.takeIf { it.isNotBlank() && !it.equals("Google Student", ignoreCase = true) }
                    ?: googleDisplayName
                val resolvedPhoto = fbUser.photoUrl?.toString() ?: googlePhotoUrl

                val existingLocal = if (resolvedEmail.isNotBlank()) userDao.getUserByEmail(resolvedEmail) else null
                val user = AuthUser(
                    uid = fbUser.uid,
                    displayName = resolvedName,
                    email = resolvedEmail,
                    photoUrl = resolvedPhoto,
                    providerId = "google.com",
                    grade = existingLocal?.grade ?: "High School (Grades 9-12)"
                )
                saveOrUpdateLocalUser(user)
                if (resolvedEmail.isNotBlank()) {
                    markLocalSessionActive(resolvedEmail)
                }
                _currentUser.value = user
                Result.success(user)
            } catch (e: Exception) {
                Log.e(tag, "Firebase Google credential sign-in failed", e)
                Result.failure(
                    IllegalStateException(
                        e.localizedMessage ?: "Failed to complete Google Sign-In with Firebase.",
                        e
                    )
                )
            }
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> = withContext(Dispatchers.IO) {
        try {
            val normalizedEmail = email.trim()
            val existing = userDao.getUserByEmail(normalizedEmail)
            val user = if (existing != null) {
                AuthUser(
                    uid = "local_${existing.id}",
                    displayName = existing.name,
                    email = existing.email,
                    photoUrl = existing.profilePhotoUri,
                    grade = existing.grade,
                    providerId = "local"
                )
            } else {
                val newUser = AuthUser(
                    uid = "local_${System.currentTimeMillis()}",
                    displayName = deriveNameFromEmail(normalizedEmail),
                    email = normalizedEmail,
                    grade = "High School (Grades 9-12)",
                    providerId = "local"
                )
                saveOrUpdateLocalUser(newUser)
                newUser
            }
            markLocalSessionActive(user.email)
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        name: String,
        grade: String
    ): Result<AuthUser> = withContext(Dispatchers.IO) {
        try {
            val normalizedEmail = email.trim()
            val user = AuthUser(
                uid = "local_${System.currentTimeMillis()}",
                displayName = name.ifBlank { deriveNameFromEmail(normalizedEmail) },
                email = normalizedEmail,
                grade = grade,
                providerId = "local"
            )
            saveOrUpdateLocalUser(user)
            markLocalSessionActive(user.email)
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun markLocalSessionActive(email: String) {
        if (email.isBlank()) return
        sessionPrefs.edit()
            .putBoolean(KEY_IS_SIGNED_IN, true)
            .putString(KEY_ACTIVE_EMAIL, email.trim())
            .apply()
    }

    override suspend fun signOutAndClearCredentials() {
        withContext(NonCancellable) {
            clearLocalSessionFlag()
            try {
                firebaseAuth?.signOut()
            } catch (e: Exception) {
                Log.w(tag, "Error signing out of Firebase: ${e.message}")
            }
            try {
                CredentialManager.create(appContext)
                    .clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(tag, "Failed to clear CredentialManager state: ${e.message}")
            }
            _currentUser.value = null
        }
    }

    override fun signOut() {
        clearLocalSessionFlag()
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.w(tag, "Error signing out of Firebase: ${e.message}")
        }
        _currentUser.value = null
    }

    private fun clearLocalSessionFlag() {
        sessionPrefs.edit()
            .putBoolean(KEY_IS_SIGNED_IN, false)
            .remove(KEY_ACTIVE_EMAIL)
            .apply()
    }

    private suspend fun saveOrUpdateLocalUser(authUser: AuthUser) {
        if (authUser.email.isBlank()) return
        try {
            val existing = userDao.getUserByEmail(authUser.email)
            if (existing == null) {
                userDao.insertUser(
                    UserEntity(
                        name = authUser.displayName,
                        email = authUser.email,
                        grade = authUser.grade,
                        profilePhotoUri = authUser.photoUrl
                    )
                )
            } else {
                userDao.updateUserProfile(
                    userId = existing.id,
                    name = authUser.displayName.ifBlank { existing.name },
                    grade = authUser.grade.ifBlank { existing.grade },
                    photoUri = authUser.photoUrl
                )
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to sync user to local DB: ${e.message}")
        }
    }

    private fun deriveNameFromEmail(email: String): String {
        val localPart = email.substringBefore("@").trim()
        if (localPart.isBlank()) return "Student"
        return localPart
            .replace(".", " ")
            .replace("_", " ")
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
            .ifBlank { "Student" }
    }

    private companion object {
        const val KEY_IS_SIGNED_IN = "is_signed_in"
        const val KEY_ACTIVE_EMAIL = "active_email"
    }
}
