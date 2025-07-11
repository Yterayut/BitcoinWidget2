package com.example.bitcoinwidget.updates

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.util.concurrent.TimeUnit

/**
 * Auto Update Manager - checks for app updates and manages auto-update functionality
 */
class AutoUpdateManager(private val context: Context) {
    
    companion object {
        private const val TAG = "AutoUpdateManager"
        private const val PREFS_NAME = "AutoUpdateSettings"
        private const val KEY_AUTO_UPDATE_ENABLED = "auto_update_enabled"
        private const val KEY_UPDATE_CHECK_INTERVAL = "update_check_interval"
        private const val KEY_LAST_CHECK_TIME = "last_check_time"
        private const val KEY_CURRENT_VERSION = "current_version"
        private const val KEY_LAST_KNOWN_VERSION = "last_known_version"
        
        // Check for updates every 24 hours
        private const val DEFAULT_CHECK_INTERVAL = 24 * 60 * 60 * 1000L
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private var updateCheckJob: Job? = null
    
    data class UpdateInfo(
        val available: Boolean,
        val currentVersion: String,
        val latestVersion: String,
        val releaseNotes: String?,
        val downloadUrl: String?,
        val forceUpdate: Boolean = false
    )
    
    /**
     * Enable or disable auto-update checking
     */
    fun setAutoUpdateEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_AUTO_UPDATE_ENABLED, enabled)
            .apply()
        
        if (enabled) {
            startPeriodicUpdateCheck()
        } else {
            stopPeriodicUpdateCheck()
        }
        
        Log.d(TAG, "Auto-update ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Check if auto-update is enabled
     */
    fun isAutoUpdateEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_UPDATE_ENABLED, true) // Default enabled
    }
    
    /**
     * Get current app version
     */
    fun getCurrentVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }
    
    /**
     * Manually check for updates
     */
    suspend fun checkForUpdates(): UpdateInfo = withContext(Dispatchers.IO) {
        try {
            val currentVersion = getCurrentVersion()
            
            // Check for updates from GitHub releases
            val updateInfo = checkFromGitHub(currentVersion)
            
            // Update last check time
            prefs.edit()
                .putLong(KEY_LAST_CHECK_TIME, System.currentTimeMillis())
                .putString(KEY_CURRENT_VERSION, currentVersion)
                .apply()
            
            Log.d(TAG, "Update check completed: available=${updateInfo.available}, latest=${updateInfo.latestVersion}")
            return@withContext updateInfo
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates: ${e.message}")
            return@withContext UpdateInfo(
                available = false,
                currentVersion = getCurrentVersion(),
                latestVersion = getCurrentVersion(),
                releaseNotes = null,
                downloadUrl = null
            )
        }
    }
    
    /**
     * Check for updates from GitHub releases API
     */
    private suspend fun checkFromGitHub(currentVersion: String): UpdateInfo = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.github.com/repos/your-username/BitcoinWidget2/releases/latest")
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("User-Agent", "BitcoinWidget2-UpdateChecker")
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val releaseInfo = gson.fromJson(responseBody, GitHubRelease::class.java)
                    val latestVersion = releaseInfo.tagName.removePrefix("v")
                    
                    val isNewer = compareVersions(latestVersion, currentVersion) > 0
                    
                    return@withContext UpdateInfo(
                        available = isNewer,
                        currentVersion = currentVersion,
                        latestVersion = latestVersion,
                        releaseNotes = releaseInfo.body,
                        downloadUrl = releaseInfo.assets.firstOrNull { it.name.endsWith(".apk") }?.downloadUrl
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check GitHub releases: ${e.message}")
        }
        
        // Fallback to alternative update check
        return@withContext checkFromAlternativeSource(currentVersion)
    }
    
    /**
     * Alternative update check method
     */
    private fun checkFromAlternativeSource(currentVersion: String): UpdateInfo {
        // This could check your own server, Firebase Remote Config, etc.
        // For now, return no update available as fallback
        return UpdateInfo(
            available = false,
            currentVersion = currentVersion,
            latestVersion = currentVersion,
            releaseNotes = null,
            downloadUrl = null
        )
    }
    
    /**
     * Compare version strings (semantic versioning)
     */
    private fun compareVersions(version1: String, version2: String): Int {
        val parts1 = version1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = version2.split(".").map { it.toIntOrNull() ?: 0 }
        
        val maxLength = maxOf(parts1.size, parts2.size)
        
        for (i in 0 until maxLength) {
            val part1 = parts1.getOrNull(i) ?: 0
            val part2 = parts2.getOrNull(i) ?: 0
            
            when {
                part1 > part2 -> return 1
                part1 < part2 -> return -1
            }
        }
        
        return 0
    }
    
    // GitHub API response data classes
    data class GitHubRelease(
        @SerializedName("tag_name") val tagName: String,
        @SerializedName("name") val name: String,
        @SerializedName("body") val body: String?,
        @SerializedName("draft") val draft: Boolean,
        @SerializedName("prerelease") val prerelease: Boolean,
        @SerializedName("assets") val assets: List<GitHubAsset>
    )
    
    data class GitHubAsset(
        @SerializedName("name") val name: String,
        @SerializedName("browser_download_url") val downloadUrl: String,
        @SerializedName("size") val size: Long
    )
    
    /**
     * Start periodic update checking
     */
    fun startPeriodicUpdateCheck() {
        stopPeriodicUpdateCheck() // Stop existing job
        
        updateCheckJob = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            while (isActive) {
                try {
                    if (shouldCheckForUpdates()) {
                        val updateInfo = checkForUpdates()
                        if (updateInfo.available) {
                            notifyUpdateAvailable(updateInfo)
                        }
                    }
                    
                    // Wait for next check (24 hours)
                    delay(DEFAULT_CHECK_INTERVAL)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error in periodic update check: ${e.message}")
                    delay(60 * 60 * 1000L) // Retry in 1 hour on error
                }
            }
        }
        
        Log.d(TAG, "Started periodic update checking")
    }
    
    /**
     * Stop periodic update checking
     */
    fun stopPeriodicUpdateCheck() {
        updateCheckJob?.cancel()
        updateCheckJob = null
        Log.d(TAG, "Stopped periodic update checking")
    }
    
    /**
     * Check if it's time to check for updates
     */
    private fun shouldCheckForUpdates(): Boolean {
        val lastCheckTime = prefs.getLong(KEY_LAST_CHECK_TIME, 0)
        val checkInterval = prefs.getLong(KEY_UPDATE_CHECK_INTERVAL, DEFAULT_CHECK_INTERVAL)
        val currentTime = System.currentTimeMillis()
        
        return (currentTime - lastCheckTime) >= checkInterval
    }
    
    /**
     * Notify user about available update
     */
    private fun notifyUpdateAvailable(updateInfo: UpdateInfo) {
        Log.d(TAG, "🔄 Update available: ${updateInfo.latestVersion}")
        Log.d(TAG, "Release notes: ${updateInfo.releaseNotes}")
        
        // Store update info for later retrieval
        prefs.edit()
            .putString(KEY_LAST_KNOWN_VERSION, updateInfo.latestVersion)
            .putString("last_release_notes", updateInfo.releaseNotes)
            .putString("last_download_url", updateInfo.downloadUrl)
            .putBoolean("last_force_update", updateInfo.forceUpdate)
            .apply()
    }
    
    /**
     * Get last known update info
     */
    fun getLastKnownUpdate(): UpdateInfo? {
        val lastVersion = prefs.getString(KEY_LAST_KNOWN_VERSION, null) ?: return null
        val currentVersion = getCurrentVersion()
        
        if (lastVersion == currentVersion) return null
        
        return UpdateInfo(
            available = true,
            currentVersion = currentVersion,
            latestVersion = lastVersion,
            releaseNotes = prefs.getString("last_release_notes", null),
            downloadUrl = prefs.getString("last_download_url", null),
            forceUpdate = prefs.getBoolean("last_force_update", false)
        )
    }
    
    /**
     * Force immediate update check
     */
    suspend fun forceUpdateCheck(): UpdateInfo {
        Log.d(TAG, "🔄 Forcing immediate update check...")
        return checkForUpdates()
    }
}