package com.example.data.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("caitlin_daily_preferences", Context.MODE_PRIVATE)
    private val alias = "caitlin_daily_secure_key"
    var userName: String get() = prefs.getString("user_name", "") ?: ""; set(v) { prefs.edit().putString("user_name", v).apply() }
    var userAge: Int get() = prefs.getInt("user_age", 0); set(v) { prefs.edit().putInt("user_age", v).apply() }
    var profilePhotoBase64: String get() = prefs.getString("profile_photo", "") ?: ""; set(v) { prefs.edit().putString("profile_photo", v).apply() }
    var calendarSyncEnabled: Boolean get() = prefs.getBoolean("calendar_sync", true); set(v) { prefs.edit().putBoolean("calendar_sync", v).apply() }
    var aiOptimizationEnabled: Boolean get() = prefs.getBoolean("ai_optimization", true); set(v) { prefs.edit().putBoolean("ai_optimization", v).apply() }
    var notificationsEnabled: Boolean get() = prefs.getBoolean("notifications", true); set(v) { prefs.edit().putBoolean("notifications", v).apply() }
    var studyTimeBetaEnabled: Boolean get() = prefs.getBoolean("study_time_beta", false); set(v) { prefs.edit().putBoolean("study_time_beta", v).apply() }
    var studyAppName: String get() = prefs.getString("study_app_name", "") ?: ""; set(v) { prefs.edit().putString("study_app_name", v).apply() }
    var lastAiInsights: String get() = prefs.getString("last_ai_insights", "") ?: ""; set(v) { prefs.edit().putString("last_ai_insights", v).apply() }
    var geminiApiKey: String get() = decrypt(prefs.getString("gemini_api_key", "")); set(v) { prefs.edit().putString("gemini_api_key", encrypt(v)).apply() }
    fun clearGeminiKey() { prefs.edit().remove("gemini_api_key").apply() }

    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(alias, null) as? SecretKey)?.let { return it }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        gen.init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        return gen.generateKey()
    }
    private fun encrypt(value: String): String {
        if (value.isBlank()) return ""
        val cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE, key())
        return Base64.encodeToString(cipher.iv + cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8)), Base64.NO_WRAP)
    }
    private fun decrypt(value: String): String {
        if (value.isBlank()) return ""
        return try {
            val bytes = Base64.decode(value, Base64.NO_WRAP); val iv = bytes.copyOfRange(0, 12); val payload = bytes.copyOfRange(12, bytes.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(payload), StandardCharsets.UTF_8)
        } catch (_: Exception) { "" }
    }
}
