package com.example.data.api

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.MessageDigest
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Centralized, secure storage manager for StudyBuddy AI provider keys.
 * Encrypts stored key payloads via Android KeyStore AES/GCM when available,
 * with a deterministic AES/GCM app-scoped fallback for Robolectric/JVM test environments.
 * Never logs raw secret values.
 */
object APIKeyManager {
    private const val TAG = "APIKeyManager"
    private const val PREFS_NAME = "studybuddy_secure_ai_keys_prefs"
    private const val KEY_ENCRYPTED_PAYLOAD = "encrypted_keys_payload_v2"
    private const val LEGACY_PREFS_NAME = "studybuddy_ai_api_manager_prefs"
    private const val LEGACY_KEY_JSON = "configured_keys_json"
    private const val KEYSTORE_ALIAS = "studybuddy_api_key_aes_v1"
    private const val GCM_TAG_BITS = 128

    private const val PREF_LEGACY_PURGE_V2 = "purged_legacy_seeded_keys_v2"
    private var prefs: SharedPreferences? = null
    private var activeOwnerId: String? = null

    private fun resolveStorageKey(ownerId: String?): String {
        val normalized = ownerId?.trim()?.lowercase().orEmpty()
        if (normalized.isBlank() || normalized == "default" || normalized == "guest") {
            return KEY_ENCRYPTED_PAYLOAD
        }
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(normalized.toByteArray(StandardCharsets.UTF_8))
        val hex = digest.joinToString("") { "%02x".format(it) }.take(24)
        return "${KEY_ENCRYPTED_PAYLOAD}_$hex"
    }

    fun initialize(context: Context, ownerId: String? = null): List<ApiKeyEntry> {
        val appContext = context.applicationContext ?: context
        activeOwnerId = ownerId
        val storageKey = resolveStorageKey(ownerId)
        val currentPrefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = currentPrefs

        // 1. Try loading encrypted v2 payload for the authorized owner
        val encrypted = currentPrefs.getString(storageKey, null)
        if (!encrypted.isNullOrBlank()) {
            val decrypted = decryptString(appContext, encrypted)
            var parsed = parseKeysJson(decrypted)
            if (!currentPrefs.getBoolean(PREF_LEGACY_PURGE_V2, false)) {
                parsed = parsed.filterNot {
                    it.id == "gemini-primary-built-in" || it.status == KeyHealthStatus.INVALID
                }
                currentPrefs.edit().putBoolean(PREF_LEGACY_PURGE_V2, true).commit()
                saveKeys(appContext, parsed, ownerId)
            }
            if (parsed.isNotEmpty()) {
                return mergeBuildConfigDefaultIfMissing(parsed)
            }
        } else if (!currentPrefs.getBoolean(PREF_LEGACY_PURGE_V2, false)) {
            currentPrefs.edit().putBoolean(PREF_LEGACY_PURGE_V2, true).commit()
        }

        // 2. Migrate legacy plain JSON only for default partition if present, then erase
        if (storageKey == KEY_ENCRYPTED_PAYLOAD) {
            val legacyPrefs = appContext.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
            val legacyJson = legacyPrefs.getString(LEGACY_KEY_JSON, null)
            if (!legacyJson.isNullOrBlank()) {
                val migrated = parseKeysJson(legacyJson).filterNot {
                    it.id == "gemini-primary-built-in" || it.status == KeyHealthStatus.INVALID
                }
                legacyPrefs.edit().remove(LEGACY_KEY_JSON).apply()
                if (migrated.isNotEmpty()) {
                    val merged = mergeBuildConfigDefaultIfMissing(migrated)
                    saveKeys(appContext, merged, ownerId)
                    return merged
                }
            }
        }

        val seeded = seedDefaultKeys()
        saveKeys(appContext, seeded, ownerId)
        return seeded
    }

    fun saveKeys(context: Context?, entries: List<ApiKeyEntry>, ownerId: String? = activeOwnerId) {
        val appContext = context?.applicationContext ?: context
        val p = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: prefs ?: return
        prefs = p
        val storageKey = resolveStorageKey(ownerId)
        try {
            val persistable = entries.filterNot { FirebaseAiLogicService.isVirtualKey(it) }
            val json = serializeKeysJson(persistable)
            val encrypted = encryptString(appContext, json)
            p.edit().putString(storageKey, encrypted).commit()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist secure key configuration")
        }
    }

    fun maskKey(rawKey: String): String {
        val trimmed = rawKey.trim()
        return when {
            trimmed == FirebaseAiLogicService.VIRTUAL_KEY_SENTINEL -> "Firebase App Check"
            trimmed.isEmpty() -> "(unconfigured)"
            trimmed.length <= 8 -> "••••••••"
            trimmed.startsWith("AIzaSy") -> "AIzaSy••••" + trimmed.takeLast(4)
            trimmed.startsWith("AQ.") -> "AQ.••••" + trimmed.takeLast(4)
            trimmed.startsWith("sk-or-") -> "sk-or-••••" + trimmed.takeLast(4)
            trimmed.startsWith("gsk_") -> "gsk_••••" + trimmed.takeLast(4)
            else -> trimmed.take(4) + "••••••••" + trimmed.takeLast(4)
        }
    }

    fun getBuildConfigGeminiKey(): String = ""

    private fun seedDefaultKeys(): List<ApiKeyEntry> {
        val buildKey = getBuildConfigGeminiKey()
        if (buildKey.isBlank()) return emptyList()
        return listOf(
            ApiKeyEntry(
                id = "gemini-primary-built-in",
                provider = AiProviderType.GEMINI,
                key = buildKey,
                label = "Gemini Primary",
                priority = 1,
                isEnabled = true
            )
        )
    }

    private fun mergeBuildConfigDefaultIfMissing(existing: List<ApiKeyEntry>): List<ApiKeyEntry> {
        val buildKey = getBuildConfigGeminiKey()
        if (buildKey.isBlank()) return existing
        val hasBuiltIn = existing.any { it.id == "gemini-primary-built-in" }
        return if (hasBuiltIn) {
            existing.map {
                if (it.id == "gemini-primary-built-in" && it.key.isBlank()) {
                    it.copy(key = buildKey)
                } else it
            }
        } else {
            val hasSameKey = existing.any { it.key == buildKey }
            if (hasSameKey) existing
            else (existing + ApiKeyEntry(
                id = "gemini-primary-built-in",
                provider = AiProviderType.GEMINI,
                key = buildKey,
                label = "Gemini Primary",
                priority = 1,
                isEnabled = true
            )).sortedBy { it.priority }
        }
    }

    private fun serializeKeysJson(entries: List<ApiKeyEntry>): String {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(JSONObject().apply {
                put("id", entry.id)
                put("provider", entry.provider.name)
                put("key", entry.key)
                put("label", entry.label)
                put("priority", entry.priority)
                put("isEnabled", entry.isEnabled)
                put("status", entry.status.name)
                put("totalRequests", entry.totalRequests)
                put("successfulRequests", entry.successfulRequests)
                put("failedRequests", entry.failedRequests)
                put("rateLimit429Errors", entry.rateLimit429Errors)
                put("lastSuccessTimestamp", entry.lastSuccessTimestamp)
                put("averageLatencyMs", entry.averageLatencyMs)
            })
        }
        return array.toString()
    }

    private fun parseKeysJson(json: String?): List<ApiKeyEntry> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<ApiKeyEntry>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val provider = try {
                    AiProviderType.valueOf(obj.getString("provider"))
                } catch (_: Exception) {
                    AiProviderType.GEMINI
                }
                val parsedStatus = try {
                    KeyHealthStatus.valueOf(obj.optString("status", KeyHealthStatus.HEALTHY.name))
                } catch (_: Exception) {
                    KeyHealthStatus.HEALTHY
                }
                list.add(
                    ApiKeyEntry(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        provider = provider,
                        key = obj.optString("key", ""),
                        label = obj.optString("label", "${provider.displayName} Key"),
                        priority = obj.optInt("priority", 1),
                        isEnabled = obj.optBoolean("isEnabled", true),
                        status = if (parsedStatus == KeyHealthStatus.INVALID) KeyHealthStatus.INVALID else KeyHealthStatus.HEALTHY,
                        totalRequests = obj.optInt("totalRequests", 0),
                        successfulRequests = obj.optInt("successfulRequests", 0),
                        failedRequests = obj.optInt("failedRequests", 0),
                        rateLimit429Errors = obj.optInt("rateLimit429Errors", 0),
                        lastSuccessTimestamp = obj.optLong("lastSuccessTimestamp", 0L),
                        averageLatencyMs = obj.optLong("averageLatencyMs", 0L)
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse encrypted key store")
        }
        return list
    }

    private fun getOrCreateSecretKey(context: Context?): SecretKey {
        try {
            val ks = KeyStore.getInstance("AndroidKeyStore")
            ks.load(null)
            val existing = ks.getKey(KEYSTORE_ALIAS, null) as? SecretKey
            if (existing != null) return existing

            val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val spec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGen.init(spec)
            return keyGen.generateKey()
        } catch (_: Throwable) {
            // Deterministic fallback key in JVM/Robolectric unit test environments without AndroidKeyStore
            val seed = "${context?.packageName ?: "com.example"}.studybuddy.keyvault.v1"
            val digest = MessageDigest.getInstance("SHA-256").digest(seed.toByteArray(StandardCharsets.UTF_8))
            return SecretKeySpec(digest, "AES")
        }
    }

    internal fun encryptString(context: Context?, plaintext: String): String {
        val key = getOrCreateSecretKey(context)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val cipherText = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))
        val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val dataB64 = Base64.encodeToString(cipherText, Base64.NO_WRAP)
        return "$ivB64:$dataB64"
    }

    internal fun decryptString(context: Context?, payload: String): String? {
        return try {
            val parts = payload.split(":")
            if (parts.size != 2) return null
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val cipherText = Base64.decode(parts[1], Base64.NO_WRAP)
            val key = getOrCreateSecretKey(context)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
            String(cipher.doFinal(cipherText), StandardCharsets.UTF_8)
        } catch (_: Throwable) {
            null
        }
    }
}
