package com.example.bitcoinwidget.ui

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.util.Log

/**
 * UI Enhancement Manager - manages visual improvements and polish
 */
class UIEnhancementManager(private val context: Context) {
    
    companion object {
        private const val TAG = "UIEnhancementManager"
        private const val PREFS_NAME = "UISettings"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_ANIMATIONS_ENABLED = "animations_enabled"
        private const val KEY_HIGH_CONTRAST = "high_contrast"
        private const val KEY_FONT_SIZE_SCALE = "font_size_scale"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    enum class ThemeMode {
        DARK, LIGHT, AUTO
    }
    
    data class UISettings(
        val themeMode: ThemeMode,
        val animationsEnabled: Boolean,
        val highContrast: Boolean,
        val fontSizeScale: Float
    )
    
    /**
     * Get optimized colors based on current theme and settings
     */
    fun getOptimizedColors(): Map<String, Int> {
        val highContrast = prefs.getBoolean(KEY_HIGH_CONTRAST, false)
        val isDarkMode = isDarkModeActive()
        
        return if (isDarkMode) {
            if (highContrast) {
                mapOf(
                    "background" to Color.parseColor("#000000"),
                    "surface" to Color.parseColor("#1a1a1a"),
                    "primary" to Color.parseColor("#FFFFFF"),
                    "accent" to Color.parseColor("#FFD700")
                )
            } else {
                mapOf(
                    "background" to Color.parseColor("#1a1a1a"),
                    "surface" to Color.parseColor("#2a2a2a"),
                    "primary" to Color.parseColor("#FFFFFF"),
                    "accent" to Color.parseColor("#FFA726")
                )
            }
        } else {
            mapOf(
                "background" to Color.parseColor("#FFFFFF"),
                "surface" to Color.parseColor("#F8F8F8"),
                "primary" to Color.parseColor("#212121"),
                "accent" to Color.parseColor("#FF5722")
            )
        }
    }
    
    /**
     * Check if dark mode should be active
     */
    private fun isDarkModeActive(): Boolean {
        return when (getThemeMode()) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.AUTO -> {
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                hour >= 18 || hour <= 6
            }
        }
    }
    
    /**
     * Get current theme mode
     */
    fun getThemeMode(): ThemeMode {
        val mode = prefs.getString(KEY_THEME_MODE, ThemeMode.AUTO.name)
        return try {
            ThemeMode.valueOf(mode ?: ThemeMode.AUTO.name)
        } catch (e: Exception) {
            ThemeMode.AUTO
        }
    }
    
    /**
     * Set theme mode
     */
    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        Log.d(TAG, "Theme mode set to: $mode")
    }
    
    /**
     * Get animation duration based on settings
     */
    fun getAnimationDuration(): Long {
        return if (prefs.getBoolean(KEY_ANIMATIONS_ENABLED, true)) {
            300L
        } else {
            0L
        }
    }
    
    /**
     * Get current UI settings
     */
    fun getCurrentSettings(): UISettings {
        return UISettings(
            themeMode = getThemeMode(),
            animationsEnabled = prefs.getBoolean(KEY_ANIMATIONS_ENABLED, true),
            highContrast = prefs.getBoolean(KEY_HIGH_CONTRAST, false),
            fontSizeScale = prefs.getFloat(KEY_FONT_SIZE_SCALE, 1.0f)
        )
    }
}