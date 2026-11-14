package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.APIKeyManager
import com.example.data.api.GeminiClient
import com.example.data.api.RequestRetryService
import com.example.data.api.RetryableErrorKind
import com.example.data.auth.FirebaseAuthRepository
import com.example.data.local.StudyBuddyDatabase
import com.example.data.local.UserEntity
import com.example.viewmodel.AppScreen
import com.example.viewmodel.StudyBuddyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AuthPersistenceAndApiTest {

    private lateinit var app: Application

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        app.getSharedPreferences("studybuddy_auth_session_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @After
    fun tearDown() {
        app.getSharedPreferences("studybuddy_auth_session_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun `default_web_client_id string resource is generated from google services`() {
        val clientId = app.getString(R.string.default_web_client_id)
        assertTrue("default_web_client_id must be present", clientId.isNotBlank())
        assertTrue(
            "default_web_client_id must match Google OAuth web client ID",
            clientId.endsWith(".apps.googleusercontent.com")
        )
    }

    @Test
    fun `persisted session survives repository recreation and clears on sign out`() = runBlocking(Dispatchers.IO) {
        val db = StudyBuddyDatabase.getDatabase(app)
        val userDao = db.userDao()
        userDao.insertUser(
            UserEntity(
                name = "Chidi Okafor",
                email = "chidi@gmail.com",
                grade = "SS3 STEM"
            )
        )

        val repo1 = FirebaseAuthRepository(app, userDao)
        assertFalse(repo1.hasPersistedSession())

        repo1.markLocalSessionActive("chidi@gmail.com")
        assertTrue(repo1.hasPersistedSession())

        // Recreate repository (simulates cold app restart)
        val repo2 = FirebaseAuthRepository(app, userDao)
        assertTrue(repo2.hasPersistedSession())
        val restored = repo2.restoreSignedInUser()
        assertNotNull(restored)
        assertEquals("chidi@gmail.com", restored?.email)
        assertEquals("Chidi Okafor", restored?.displayName)

        // Sign out clears persisted session cleanly
        repo2.signOutAndClearCredentials()
        assertFalse(repo2.hasPersistedSession())
        assertNull(repo2.restoreSignedInUser())
    }

    @Test
    fun `viewmodel skips welcome screen when active session exists and returns on logout`() = runBlocking {
        val db = StudyBuddyDatabase.getDatabase(app)
        val userDao = db.userDao()
        val repo = FirebaseAuthRepository(app, userDao)

        // Fresh state -> starts on WELCOME
        val freshVm = StudyBuddyViewModel(app)
        assertEquals(AppScreen.WELCOME, freshVm.currentScreen.value)

        // Persist user + session -> next ViewModel instance starts directly on TUTOR
        repo.signUpWithEmail(
            email = "amaka@gmail.com",
            password = "",
            name = "Amaka Nwosu",
            grade = "SS3"
        )
        val returningVm = StudyBuddyViewModel(app)
        assertEquals(AppScreen.TUTOR, returningVm.currentScreen.value)

        // Logout navigates back to WELCOME and clears session persistence
        returningVm.logout()
        assertEquals(AppScreen.WELCOME, returningVm.currentScreen.value)
        assertFalse(repo.hasPersistedSession())
    }

    @Test
    fun `google sign in without activity context fails cleanly without fake user fallback`() = runBlocking {
        val db = StudyBuddyDatabase.getDatabase(app)
        val repo = FirebaseAuthRepository(app, db.userDao())
        val result = repo.signInWithGoogle(app) // Application context is not an Activity
        assertTrue(result.isFailure)
        assertFalse(repo.hasPersistedSession())
        assertNull(repo.currentUser.value)
    }

    @Test
    fun `firebase ai logic route preserves authentic error messages without api key confusion`() {
        val classification = RequestRetryService.classifyFailure(
            httpCode = 403,
            error = RuntimeException("HTTP 403: Firebase App Check attestation failed."),
            isFirebaseAppCheckRoute = true
        )
        assertEquals(RetryableErrorKind.INVALID_KEY_401_403, classification.kind)
        assertTrue(classification.safeMessage.contains("App Check"))
        assertFalse(classification.safeMessage.contains("Configured key rejected"))
        assertEquals("", APIKeyManager.getBuildConfigGeminiKey())
        assertEquals("", GeminiClient.getEffectiveApiKey())
    }
}
