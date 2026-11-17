package com.example.data.api

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

private const val TAG = "AppCheckInit"

private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

/**
 * Configures Firebase App Check before any Compose UI, ViewModel, or Firebase AI Logic call.
 * Must be invoked from MainActivity.onCreate() with the launch Intent so debug tokens
 * are stored synchronously via commit() prior to DebugAppCheckProviderFactory initialization.
 */
object AppCheckDiagnostics {
    @Volatile
    var statusSummary: String = "App Check not initialized"
        internal set

    @Volatile
    var hasLaunchDebugToken: Boolean = false
        internal set
}

fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        if (BuildConfig.DEBUG) {
            val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
            if (!debugToken.isNullOrEmpty()) {
                val persistenceKey = FirebaseApp.getInstance().persistenceKey
                val prefs = context.getSharedPreferences(
                    "$DEBUG_STORE_PREFIX$persistenceKey",
                    Context.MODE_PRIVATE
                )
                prefs.edit()
                    .putString(DEBUG_SECRET_KEY, debugToken)
                    .commit()
                AppCheckDiagnostics.hasLaunchDebugToken = true
                AppCheckDiagnostics.statusSummary = "DEBUG (launch debug token configured)"
                Log.d(TAG, "App Check debug secret configured successfully.")
            } else {
                AppCheckDiagnostics.hasLaunchDebugToken = false
                AppCheckDiagnostics.statusSummary = "DEBUG (no launch debug token)"
                Log.w(TAG, "No debug token found in Intent extra.")
            }

            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d(TAG, "DebugAppCheckProviderFactory installed.")
        } else {
            val resId = context.resources.getIdentifier(
                RECAPTCHA_SITE_KEY_RES,
                "string",
                context.packageName
            )
            if (resId != 0) {
                val siteKey = context.getString(resId)
                if (siteKey.isNotBlank()) {
                    FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                        RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                    )
                    AppCheckDiagnostics.statusSummary = "RELEASE (reCAPTCHA Enterprise installed)"
                    Log.d(TAG, "RecaptchaAppCheckProviderFactory installed.")
                } else {
                    AppCheckDiagnostics.statusSummary = "RELEASE (recaptcha_site_key is blank)"
                    Log.e(TAG, "recaptcha_site_key resource string is blank.")
                }
            } else {
                AppCheckDiagnostics.statusSummary = "RELEASE (recaptcha_site_key missing)"
                Log.e(TAG, "recaptcha_site_key string resource not found in package.")
            }
        }
    } catch (e: Exception) {
        AppCheckDiagnostics.statusSummary = "INIT ERROR (${e.localizedMessage ?: e.javaClass.simpleName})"
        Log.e(TAG, "Failed to initialize Firebase App Check", e)
    }
}
