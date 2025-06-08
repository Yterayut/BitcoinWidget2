package com.example.bitcoinwidget.alerts

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.bitcoinwidget.ApiService
import kotlinx.coroutines.*

/**
 * Price Alert Manager - handles Bitcoin price monitoring and notifications
 */
class PriceAlertManager(private val context: Context) {
    
    companion object {
        private const val TAG = "PriceAlertManager"
        private const val PREFS_NAME = "PriceAlerts"
        private const val KEY_ALERTS_ENABLED = "alerts_enabled"
        private const val KEY_UPPER_LIMIT = "upper_limit"
        private const val KEY_LOWER_LIMIT = "lower_limit"
        private const val KEY_LAST_ALERT_TIME = "last_alert_time"
        private const val KEY_ALERT_COOLDOWN = "alert_cooldown"
        
        // Minimum time between alerts (30 minutes)
        private const val ALERT_COOLDOWN_MS = 30 * 60 * 1000L
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val apiService = ApiService()
    private var monitoringJob: Job? = null
    
    data class PriceAlert(
        val id: String,
        val targetPrice: Double,
        val alertType: AlertType,
        val isEnabled: Boolean = true,
        val createdAt: Long = System.currentTimeMillis()
    )
    
    enum class AlertType {
        ABOVE,      // Alert when price goes above target
        BELOW,      // Alert when price goes below target
        CHANGE      // Alert when price changes by X%
    }
    
    /**
     * Set price alert for upper limit (sell signal)
     */
    fun setUpperPriceAlert(targetPrice: Double) {
        prefs.edit()
            .putFloat(KEY_UPPER_LIMIT, targetPrice.toFloat())
            .putBoolean(KEY_ALERTS_ENABLED, true)
            .apply()
        
        Log.d(TAG, "✅ Upper price alert set: $${String.format("%,.0f", targetPrice)}")
        startMonitoring()
    }
    
    /**
     * Set price alert for lower limit (buy signal)
     */
    fun setLowerPriceAlert(targetPrice: Double) {
        prefs.edit()
            .putFloat(KEY_LOWER_LIMIT, targetPrice.toFloat())
            .putBoolean(KEY_ALERTS_ENABLED, true)
            .apply()
        
        Log.d(TAG, "✅ Lower price alert set: $${String.format("%,.0f", targetPrice)}")
        startMonitoring()
    }
    
    /**
     * Get current alert settings
     */
    fun getAlertSettings(): Triple<Boolean, Double?, Double?> {
        val enabled = prefs.getBoolean(KEY_ALERTS_ENABLED, false)
        val upperLimit = prefs.getFloat(KEY_UPPER_LIMIT, 0f).let { if (it > 0) it.toDouble() else null }
        val lowerLimit = prefs.getFloat(KEY_LOWER_LIMIT, 0f).let { if (it > 0) it.toDouble() else null }
        
        return Triple(enabled, upperLimit, lowerLimit)
    }
    
    /**
     * Disable all price alerts
     */
    fun disableAlerts() {
        prefs.edit()
            .putBoolean(KEY_ALERTS_ENABLED, false)
            .apply()
        
        stopMonitoring()
        Log.d(TAG, "🔕 Price alerts disabled")
    }
    
    /**
     * Start monitoring Bitcoin price for alerts
     */
    fun startMonitoring() {
        if (!prefs.getBoolean(KEY_ALERTS_ENABLED, false)) {
            Log.d(TAG, "Alerts disabled, not starting monitoring")
            return
        }
        
        val (enabled, upperLimit, lowerLimit) = getAlertSettings()
        if (!enabled || (upperLimit == null && lowerLimit == null)) {
            Log.d(TAG, "No active alerts, not starting monitoring")
            return
        }
        
        stopMonitoring() // Stop existing monitoring
        
        monitoringJob = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            Log.d(TAG, "🔍 Started price monitoring for alerts - Upper: $upperLimit, Lower: $lowerLimit")
            
            while (isActive) {
                try {
                    checkPriceAlerts()
                    delay(30_000L) // Check every 30 seconds for better responsiveness
                } catch (e: Exception) {
                    Log.e(TAG, "Error in price monitoring: ${e.message}")
                    delay(30_000L) // Wait before retry
                }
            }
        }
        
        Log.d(TAG, "🚀 Price monitoring job started successfully")
    }
    
    /**
     * Stop price monitoring
     */
    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        Log.d(TAG, "⏹️ Stopped price monitoring")
    }
    
    /**
     * Check current price against alert conditions
     */
    private suspend fun checkPriceAlerts() {
        try {
            Log.d(TAG, "🔍 Checking price alerts...")
            
            val bitcoinData = apiService.fetchBitcoinData()
            val currentPrice = bitcoinData.price
            
            if (currentPrice == null) {
                Log.w(TAG, "⚠️ No price data available for alert check")
                return
            }
            
            val (enabled, upperLimit, lowerLimit) = getAlertSettings()
            if (!enabled) {
                Log.d(TAG, "Alerts not enabled, skipping check")
                return
            }
            
            // Check cooldown period
            val lastAlertTime = prefs.getLong(KEY_LAST_ALERT_TIME, 0)
            val currentTime = System.currentTimeMillis()
            val timeSinceLastAlert = currentTime - lastAlertTime
            
            if (timeSinceLastAlert < ALERT_COOLDOWN_MS) {
                val minutesLeft = (ALERT_COOLDOWN_MS - timeSinceLastAlert) / (60 * 1000)
                Log.d(TAG, "🕒 Still in cooldown period: ${minutesLeft} minutes left")
                return
            }
            
            Log.d(TAG, "💰 Current price: $${String.format("%,.0f", currentPrice)} (Upper: $upperLimit, Lower: $lowerLimit)")
            
            // Check upper limit (sell signal)
            upperLimit?.let { limit ->
                if (currentPrice >= limit) {
                    Log.d(TAG, "🚀 UPPER ALERT TRIGGERED! Price $${String.format("%,.0f", currentPrice)} >= $${String.format("%,.0f", limit)}")
                    triggerAlert(
                        "🚀 Bitcoin Price Alert",
                        "Bitcoin reached your target price of $${String.format("%,.0f", limit)}!\nCurrent price: $${String.format("%,.0f", currentPrice)}\n\nTime to consider selling! 💰",
                        AlertType.ABOVE
                    )
                    return
                }
            }
            
            // Check lower limit (buy opportunity)
            lowerLimit?.let { limit ->
                if (currentPrice <= limit) {
                    Log.d(TAG, "📉 LOWER ALERT TRIGGERED! Price $${String.format("%,.0f", currentPrice)} <= $${String.format("%,.0f", limit)}")
                    triggerAlert(
                        "📉 Bitcoin Price Alert", 
                        "Bitcoin dropped to your target price of $${String.format("%,.0f", limit)}!\nCurrent price: $${String.format("%,.0f", currentPrice)}\n\nTime to consider buying! 🛒",
                        AlertType.BELOW
                    )
                    return
                }
            }
            
            Log.d(TAG, "✅ Price check complete - no alerts triggered")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error checking price alerts: ${e.message}", e)
        }
    }
    
    /**
     * Trigger notification alert
     */
    private fun triggerAlert(title: String, message: String, alertType: AlertType) {
        // Update last alert time
        prefs.edit()
            .putLong(KEY_LAST_ALERT_TIME, System.currentTimeMillis())
            .apply()
        
        // Send notification
        NotificationHelper.sendPriceAlert(context, title, message)
        
        Log.d(TAG, "🔔 Alert triggered: $title - $message")
    }
    
    /**
     * Clear specific alert
     */
    fun clearUpperAlert() {
        prefs.edit().remove(KEY_UPPER_LIMIT).apply()
        Log.d(TAG, "Cleared upper price alert")
    }
    
    fun clearLowerAlert() {
        prefs.edit().remove(KEY_LOWER_LIMIT).apply()
        Log.d(TAG, "Cleared lower price alert")
    }
    
    /**
     * Check if any alerts are currently active
     */
    fun hasActiveAlerts(): Boolean {
        val (enabled, upper, lower) = getAlertSettings()
        return enabled && (upper != null || lower != null)
    }
    
    /**
     * Test alert system by sending a test notification
     */
    fun testAlert() {
        Log.d(TAG, "🧪 Testing alert system...")
        NotificationHelper.sendPriceAlert(
            context,
            "🧪 Test Alert",
            "This is a test notification to verify your alerts are working!"
        )
        Log.d(TAG, "✅ Test alert sent")
    }
    
    /**
     * Force check alerts now (for testing)
     */
    suspend fun forceCheckAlerts() {
        Log.d(TAG, "🔍 Force checking alerts now...")
        checkPriceAlerts()
    }
}