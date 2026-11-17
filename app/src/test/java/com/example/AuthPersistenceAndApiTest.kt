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

        val appCheckRateLimit = RequestRetryService.classifyFailure(
            httpCode = null,
            error = java.io.IOException("App Check (RELEASE (reCAPTCHA Enterprise installed)) failed: Too many attempts."),
            isFirebaseAppCheckRoute = true
        )
        assertEquals(RetryableErrorKind.INVALID_KEY_401_403, appCheckRateLimit.kind)
        assertTrue(appCheckRateLimit.safeMessage.contains("App Check"))

        val invalidKeyClassification = RequestRetryService.classifyFailure(
            httpCode = 400,
            error = RuntimeException("API key not valid. Please pass a valid API key.")
        )
        assertEquals(RetryableErrorKind.INVALID_KEY_401_403, invalidKeyClassification.kind)

        assertEquals("", APIKeyManager.getBuildConfigGeminiKey())
        assertEquals("", GeminiClient.getEffectiveApiKey())
    }

    @Test
    fun `api key manager purges legacy built in keys and persists invalid health status`() {
        app.getSharedPreferences("studybuddy_secure_ai_keys_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        APIKeyManager.initialize(app)
        APIKeyManager.saveKeys(
            app,
            listOf(
                com.example.data.api.ApiKeyEntry(
                    id = "gemini-primary-built-in",
                    provider = com.example.data.api.AiProviderType.GEMINI,
                    key = "AIzaSyStalePlaceholderKey",
                    label = "Legacy Built-In",
                    priority = 1
                ),
                com.example.data.api.ApiKeyEntry(
                    id = "user-invalid-key",
                    provider = com.example.data.api.AiProviderType.GEMINI,
                    key = "AIzaSyBadUserKey1234",
                    label = "Invalid Key",
                    priority = 2,
                    status = com.example.data.api.KeyHealthStatus.INVALID
                )
            )
        )

        // Force upgrade purge path
        app.getSharedPreferences("studybuddy_secure_ai_keys_prefs", Context.MODE_PRIVATE)
            .edit()
            .remove("purged_legacy_seeded_keys_v2")
            .commit()

        val afterPurge = APIKeyManager.initialize(app)
        assertTrue("Legacy built-in and invalid keys must be purged on upgrade", afterPurge.isEmpty())

        // Persisting INVALID status survives subsequent initializations
        APIKeyManager.saveKeys(
            app,
            listOf(
                com.example.data.api.ApiKeyEntry(
                    id = "k-invalid",
                    provider = com.example.data.api.AiProviderType.GROQ,
                    key = "gsk_badKey9999",
                    label = "Bad Groq Key",
                    status = com.example.data.api.KeyHealthStatus.INVALID
                )
            )
        )
        val reloaded = APIKeyManager.initialize(app)
        assertEquals(1, reloaded.size)
        assertEquals(com.example.data.api.KeyHealthStatus.INVALID, reloaded.first().status)
    }

    @Test
    fun `release recaptcha_site_key resource resolves cleanly and rotation prioritizes firebase ai logic`() {
        val resId = app.resources.getIdentifier("recaptcha_site_key", "string", app.packageName)
        assertTrue("recaptcha_site_key must resolve via package resource lookup for Release App Check", resId != 0)
        assertTrue("recaptcha_site_key must be non-blank", app.getString(resId).isNotBlank())

        val candidates = com.example.data.api.APIKeyRotationService.selectOrderedCandidates(
            allKeys = listOf(
                com.example.data.api.ApiKeyEntry(
                    id = "custom-groq",
                    provider = com.example.data.api.AiProviderType.GROQ,
                    key = "gsk_customKey123456",
                    label = "Groq",
                    priority = 1
                ),
                com.example.data.api.ApiKeyEntry(
                    id = com.example.data.api.FirebaseAiLogicService.VIRTUAL_KEY_ID,
                    provider = com.example.data.api.AiProviderType.GEMINI,
                    key = com.example.data.api.FirebaseAiLogicService.VIRTUAL_KEY_SENTINEL,
                    label = "StudyBuddy AI (Firebase App Check)",
                    priority = 1
                )
            ),
            tier = com.example.data.api.StudyBuddyModelTier.FREE,
            capability = com.example.data.api.AiTaskCapability.TEXT_CHAT
        )
        assertEquals(com.example.data.api.FirebaseAiLogicService.VIRTUAL_KEY_ID, candidates.firstOrNull()?.id)
    }

    @Test
    fun `configureAppCheck updates diagnostic state for launch debug token presence`() {
        val noTokenIntent = android.content.Intent()
        com.example.data.api.configureAppCheck(app, noTokenIntent)
        assertFalse(com.example.data.api.AppCheckDiagnostics.hasLaunchDebugToken)
        assertTrue(com.example.data.api.AppCheckDiagnostics.statusSummary.contains("DEBUG"))

        val withTokenIntent = android.content.Intent().apply {
            putExtra("FIREBASE_APPCHECK_DEBUG_TOKEN", "00000000-0000-4000-8000-000000000001")
        }
        com.example.data.api.configureAppCheck(app, withTokenIntent)
        assertTrue(com.example.data.api.AppCheckDiagnostics.hasLaunchDebugToken)
        assertTrue(com.example.data.api.AppCheckDiagnostics.statusSummary.contains("launch debug token configured"))
    }

    @Test
    fun `app check diagnostics preserves underlying error across rate-limit retries and avoids truncation`() {
        com.example.data.api.AppCheckDiagnostics.clearTokenFailure()
        com.example.data.api.AppCheckDiagnostics.recordTokenFailure("RecaptchaException: Network connection error")
        com.example.data.api.AppCheckDiagnostics.recordTokenFailure("Too many attempts.")
        assertEquals(
            "RecaptchaException: Network connection error",
            com.example.data.api.AppCheckDiagnostics.lastUnderlyingTokenError
        )

        val cooldownMsg = com.example.data.api.FirebaseAiLogicService.formatAppCheckFailureMessage("Too many attempts.")
        assertTrue(cooldownMsg.contains("RecaptchaException: Network connection error"))
        assertTrue(cooldownMsg.contains("VPN"))
        assertFalse(cooldownMsg.contains("Firebase Console"))

        val sanitized = RequestRetryService.classifyFailure(
            httpCode = null,
            error = java.io.IOException(cooldownMsg),
            isFirebaseAppCheckRoute = true
        )
        assertEquals(cooldownMsg, sanitized.safeMessage)
        com.example.data.api.AppCheckDiagnostics.clearTokenFailure()
    }

    @Test
    fun `google credential error classifier surfaces SHA-1 and Play Services diagnostics instead of silent cancel`() {
        val devConsoleCancel = androidx.credentials.exceptions.GetCredentialCancellationException(
            "[28444] Developer console is not set up correctly."
        )
        val classified = FirebaseAuthRepository.classifyGoogleCredentialFailure(
            error = devConsoleCancel,
            primaryError = null
        )
        assertFalse(
            "SHA-1 / Developer Console errors must not be treated as silent cancellation",
            classified is androidx.credentials.exceptions.GetCredentialCancellationException
        )
        assertTrue(
            "Message should guide user about Release SHA-1 registration",
            classified.message.orEmpty().contains("SHA-1")
        )

        val fallbackCancel = androidx.credentials.exceptions.GetCredentialCancellationException("User cancelled")
        val primaryConfigError = androidx.credentials.exceptions.NoCredentialException(
            "Caller not allowed to call this method"
        )
        val fromFallback = FirebaseAuthRepository.classifyGoogleCredentialFailure(
            error = fallbackCancel,
            primaryError = primaryConfigError
        )
        assertFalse(fromFallback is androidx.credentials.exceptions.GetCredentialCancellationException)
        assertTrue(fromFallback.message.orEmpty().contains("SHA-1"))
    }
}
