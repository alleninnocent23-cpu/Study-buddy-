package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class FishAudioTtsService(private val context: Context) {

    private val tag = "FishAudioTTS"
    private val apiKey = "sk-fish-TczqYXsj5efe-16F_ZpPkpqJoIwwORUzv3Py8E1dvoE"
    // Nigerian voice model reference ID retrieved from Fish Audio API
    private val nigerianReferenceId = "cf1aa977afd941b7b2be8d9e3a921e30"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private var mediaPlayer: MediaPlayer? = null

    suspend fun synthesizeAndPlay(
        text: String,
        onStart: () -> Unit = {},
        onCompletion: () -> Unit = {},
        onError: (String) -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            stop()

            val jsonBody = JSONObject().apply {
                put("text", text)
                put("reference_id", nigerianReferenceId)
                put("format", "mp3")
            }.toString()

            val request = Request.Builder()
                .url("https://api.fish.audio/v1/tts")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            val tempFile = File(context.cacheDir, "fish_audio_nigerian_${System.currentTimeMillis()}.mp3")
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.w(tag, "Fish Audio API returned ${response.code}: $errBody")
                    withContext(Dispatchers.Main) {
                        onError("Fish Audio (${response.code}): $errBody")
                    }
                    return@withContext false
                }

                val responseBody = response.body
                if (responseBody == null) {
                    withContext(Dispatchers.Main) { onError("Empty audio response") }
                    return@withContext false
                }

                FileOutputStream(tempFile).use { out ->
                    responseBody.byteStream().copyTo(out)
                }
            }

            withContext(Dispatchers.Main) {
                try {
                    val player = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                        )
                        setDataSource(tempFile.absolutePath)
                        setOnPreparedListener { mp ->
                            onStart()
                            mp.start()
                        }
                        setOnCompletionListener { mp ->
                            mp.release()
                            mediaPlayer = null
                            tempFile.delete()
                            onCompletion()
                        }
                        setOnErrorListener { mp, what, extra ->
                            mp.release()
                            mediaPlayer = null
                            tempFile.delete()
                            onError("MediaPlayer error: $what, $extra")
                            true
                        }
                        prepareAsync()
                    }
                    mediaPlayer = player
                } catch (e: Exception) {
                    Log.e(tag, "Error setting up MediaPlayer: ${e.message}", e)
                    onError("Player setup failed: ${e.message}")
                }
            }
            return@withContext true
        } catch (e: Exception) {
            Log.e(tag, "FishAudio synthesis error: ${e.message}", e)
            withContext(Dispatchers.Main) {
                onError("Network error: ${e.message}")
            }
            return@withContext false
        }
    }

    fun stop() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.w(tag, "Error stopping mediaPlayer: ${e.message}")
        } finally {
            mediaPlayer = null
        }
    }

    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (e: Exception) {
            false
        }
    }
}
