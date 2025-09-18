package io.deepone.sdk

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import java.util.*

/**
 * Utility functions for device fingerprinting and system information
 * Provides device data collection for attribution and analytics
 */

/**
 * Generates device fingerprint data for attribution analysis
 * Collects comprehensive device information for tracking and analytics
 */
@SuppressLint("HardwareIds")
internal fun getDeviceFingerprint(context: Context): Map<String, Any> {
    val deviceInfo = mutableMapOf<String, Any>()
    
    // Platform information
    deviceInfo["os"] = "android"

    // Device model
    deviceInfo["model"] = Build.MODEL
    
    // Device ID
    deviceInfo["deviceId"] = getUniqueDeviceId(context)
    
    // Language code
    deviceInfo["languageCode"] = getDeviceLanguage()
    
    return deviceInfo
}

/**
 * Gets a unique device identifier
 * Uses Android ID which persists across app reinstalls but not factory resets
 */
@SuppressLint("HardwareIds")
private fun getUniqueDeviceId(context: Context): String {
    return try {
        // Android ID - persists across app reinstalls, resets on factory reset
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: UUID.randomUUID().toString()
    } catch (e: Exception) {
        // Fallback to random UUID if Android ID is not available
        UUID.randomUUID().toString()
    }
}

/**
 * Gets the device language code
 * Returns the primary language code for the device
 */
private fun getDeviceLanguage(): String {
    return try {
        val locale = Locale.getDefault()
        locale.language
    } catch (e: Exception) {
        "en" // Default to English if unable to determine language
    }
}

/**
 * Extension property to get device language code from Context
 */
val Context.deviceLanguage: String
    get() = getDeviceLanguage()


/**
 * Gets preferred language codes list from system locale preferences
 */
public fun getPreferredLanguageCodes(context: Context): List<String> {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val locales = context.resources.configuration.locales
            (0 until locales.size()).map { locales.get(it).language }
        } else {
            @Suppress("DEPRECATION")
            listOf(context.resources.configuration.locale.language)
        }
    } catch (e: Exception) {
        listOf("en")
    }
}

/**
 * Formatted OS version string for the Android device
 */
val formattedOSVersion: String
    get() = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

/**
 * Device model information for the current Android device
 */
val deviceModel: String
    get() = "${Build.MANUFACTURER} ${Build.MODEL}".trim()

/**
 * Device brand and model information
 */
val deviceInfo: String
    get() = "${Build.BRAND} ${Build.MODEL}".trim()
