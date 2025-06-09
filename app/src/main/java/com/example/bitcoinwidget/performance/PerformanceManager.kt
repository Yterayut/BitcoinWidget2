package com.example.bitcoinwidget.performance

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * Performance Manager - optimizes battery usage and network efficiency
 */
class PerformanceManager(private val context: Context) {
    
    companion object {
        private const val TAG = "PerformanceManager"
        private const val PREFS_NAME = "PerformanceSettings"
        private const val KEY_BATTERY_OPTIMIZATION = "battery_optimization"
        private const val KEY_ADAPTIVE_INTERVALS = "adaptive_intervals"
        private const val KEY_NETWORK_AWARE = "network_aware"
        private const val KEY_LAST_BATTERY_LEVEL = "last_battery_level"
        private const val KEY_LOW_BATTERY_MODE = "low_battery_mode"
        
        // Battery level thresholds
        private const val LOW_BATTERY_THRESHOLD = 20
        private const val CRITICAL_BATTERY_THRESHOLD = 10
        
        // Adaptive intervals based on conditions
        private const val NORMAL_INTERVAL = 5 * 60 * 1000L        // 5 minutes
        private const val LOW_BATTERY_INTERVAL = 15 * 60 * 1000L  // 15 minutes
        private const val NIGHT_MODE_INTERVAL = 30 * 60 * 1000L   // 30 minutes
        private const val NO_NETWORK_INTERVAL = 10 * 60 * 1000L   // 10 minutes
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Get optimized refresh interval based on current conditions
     */
    fun getOptimizedInterval(defaultInterval: Long, widgetId: Int): Long {
        if (!prefs.getBoolean(KEY_BATTERY_OPTIMIZATION, true)) {
            return defaultInterval // No optimization, use default
        }
        
        val batteryLevel = getBatteryLevel()
        val isCharging = isDeviceCharging()
        val hasStrongNetwork = hasStrongNetworkConnection()
        val isNightMode = isNightTime()
        
        Log.d(TAG, "Conditions - Battery: $batteryLevel%, Charging: $isCharging, Network: $hasStrongNetwork, Night: $isNightMode")
        
        return when {
            // Critical battery - minimal updates
            batteryLevel <= CRITICAL_BATTERY_THRESHOLD && !isCharging -> {
                Log.d(TAG, "Critical battery mode: using ${NIGHT_MODE_INTERVAL/1000/60}min interval")
                NIGHT_MODE_INTERVAL
            }
            
            // Low battery - reduced frequency
            batteryLevel <= LOW_BATTERY_THRESHOLD && !isCharging -> {
                Log.d(TAG, "Low battery mode: using ${LOW_BATTERY_INTERVAL/1000/60}min interval")
                LOW_BATTERY_INTERVAL
            }
            
            // Night time - less frequent updates
            isNightMode && !isCharging -> {
                Log.d(TAG, "Night mode: using ${NIGHT_MODE_INTERVAL/1000/60}min interval")
                NIGHT_MODE_INTERVAL
            }
            
            // Poor network connection
            !hasStrongNetwork -> {
                Log.d(TAG, "Poor network: using ${NO_NETWORK_INTERVAL/1000/60}min interval")
                NO_NETWORK_INTERVAL
            }
            
            // Charging - normal or faster updates
            isCharging -> {
                Log.d(TAG, "Device charging: using default ${defaultInterval/1000/60}min interval")
                defaultInterval
            }
            
            // Normal conditions
            else -> {
                Log.d(TAG, "Normal conditions: using ${NORMAL_INTERVAL/1000/60}min interval")
                minOf(defaultInterval, NORMAL_INTERVAL)
            }
        }
    }
    
    /**
     * Get current battery level percentage
     */
    private fun getBatteryLevel(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
    
    /**
     * Check if device is currently charging
     */
    private fun isDeviceCharging(): Boolean {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val status = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
        return status == BatteryManager.BATTERY_STATUS_CHARGING || 
               status == BatteryManager.BATTERY_STATUS_FULL
    }
    
    /**
     * Check network connection strength
     */
    private fun hasStrongNetworkConnection(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                }
                else -> false
            }
        } else {
            // Fallback for API < 23
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected == true && networkInfo.type == ConnectivityManager.TYPE_WIFI
        }
    }
    
    /**
     * Check if it's night time (10 PM - 6 AM)
     */
    private fun isNightTime(): Boolean {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return hour >= 22 || hour <= 6
    }
    
    /**
     * Check if device is in power save mode
     */
    fun isInPowerSaveMode(): Boolean {
        val batteryLevel = getBatteryLevel()
        val isCharging = isDeviceCharging()
        
        val isLowBattery = batteryLevel <= LOW_BATTERY_THRESHOLD && !isCharging
        val userEnabledPowerSave = prefs.getBoolean(KEY_LOW_BATTERY_MODE, false)
        
        return isLowBattery || userEnabledPowerSave
    }
    
    /**
     * Should skip update based on performance conditions
     */
    fun shouldSkipUpdate(widgetId: Int): Boolean {
        if (!prefs.getBoolean(KEY_NETWORK_AWARE, true)) {
            return false // Network awareness disabled
        }
        
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        
        // Skip if no network connection
        if (network == null) {
            Log.d(TAG, "Skipping update: No network connection")
            return true
        }
        
        // Skip if in airplane mode or very poor connection
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        if (capabilities == null || !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            Log.d(TAG, "Skipping update: No internet capability")
            return true
        }
        
        return false
    }
    
    /**
     * Log performance metrics
     */
    fun logPerformanceMetrics(widgetId: Int, apiCallDuration: Long, batteryBefore: Int, batteryAfter: Int) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val batteryDelta = batteryBefore - batteryAfter
        
        Log.d(TAG, "Performance metrics [$timestamp]:")
        Log.d(TAG, "  Widget: $widgetId")
        Log.d(TAG, "  API Call: ${apiCallDuration}ms")
        Log.d(TAG, "  Battery: $batteryBefore% → $batteryAfter% (Δ$batteryDelta%)")
        
        // Store metrics for analysis
        val metricsPrefs = context.getSharedPreferences("PerformanceMetrics", Context.MODE_PRIVATE)
        metricsPrefs.edit()
            .putLong("last_api_duration_$widgetId", apiCallDuration)
            .putInt("last_battery_delta_$widgetId", batteryDelta)
            .putLong("last_update_time_$widgetId", System.currentTimeMillis())
            .apply()
    }
    
    /**
     * Get performance recommendations
     */
    fun getPerformanceRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()
        val batteryLevel = getBatteryLevel()
        val isCharging = isDeviceCharging()
        
        if (batteryLevel <= LOW_BATTERY_THRESHOLD && !isCharging) {
            recommendations.add("🔋 Enable battery optimization to extend device life")
        }
        
        if (!hasStrongNetworkConnection()) {
            recommendations.add("📶 Poor network detected - updates may be delayed")
        }
        
        if (isNightTime()) {
            recommendations.add("🌙 Night mode active - reduced update frequency")
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("✅ Performance conditions are optimal")
        }
        
        return recommendations
    }
    
    /**
     * Enable/disable battery optimization
     */
    fun setBatteryOptimization(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BATTERY_OPTIMIZATION, enabled).apply()
        Log.d(TAG, "Battery optimization: ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Enable/disable adaptive intervals
     */
    fun setAdaptiveIntervals(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ADAPTIVE_INTERVALS, enabled).apply()
        Log.d(TAG, "Adaptive intervals: ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Enable/disable network awareness
     */
    fun setNetworkAware(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NETWORK_AWARE, enabled).apply()
        Log.d(TAG, "Network awareness: ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Get current optimization settings
     */
    fun getOptimizationSettings(): Triple<Boolean, Boolean, Boolean> {
        return Triple(
            prefs.getBoolean(KEY_BATTERY_OPTIMIZATION, true),
            prefs.getBoolean(KEY_ADAPTIVE_INTERVALS, true),
            prefs.getBoolean(KEY_NETWORK_AWARE, true)
        )
    }
}