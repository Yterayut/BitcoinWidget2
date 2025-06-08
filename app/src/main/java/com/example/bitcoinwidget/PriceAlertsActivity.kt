package com.example.bitcoinwidget

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.bitcoinwidget.alerts.NotificationHelper
import com.example.bitcoinwidget.alerts.PriceAlertManager

class PriceAlertsActivity : Activity() {
    
    private lateinit var alertManager: PriceAlertManager
    private val TAG = "PriceAlertsActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_price_alerts)
        
        alertManager = PriceAlertManager(this)
        
        // Create notification channel
        NotificationHelper.createNotificationChannel(this)
        
        setupUI()
        loadCurrentSettings()
        
        Log.d(TAG, "🔔 PriceAlertsActivity created")
    }
    
    private fun setupUI() {
        val enableSwitch = findViewById<Switch>(R.id.alerts_enable_switch)
        val upperPriceInput = findViewById<EditText>(R.id.upper_price_input)
        val lowerPriceInput = findViewById<EditText>(R.id.lower_price_input)
        val setUpperButton = findViewById<Button>(R.id.set_upper_alert_button)
        val setLowerButton = findViewById<Button>(R.id.set_lower_alert_button)
        val clearAlertsButton = findViewById<Button>(R.id.clear_alerts_button)
        val testAlertButton = findViewById<Button>(R.id.test_alert_button)
        val backButton = findViewById<Button>(R.id.back_button)
        val statusText = findViewById<TextView>(R.id.alert_status_text)
        
        // Enable/Disable alerts
        enableSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                alertManager.startMonitoring()
                updateStatusText(statusText)
                showToast("✅ Price alerts enabled")
            } else {
                alertManager.disableAlerts()
                updateStatusText(statusText)
                showToast("🔕 Price alerts disabled")
            }
        }
        
        // Set upper price alert (sell signal)
        setUpperButton.setOnClickListener {
            val priceText = upperPriceInput.text.toString()
            if (priceText.isNotEmpty()) {
                try {
                    val targetPrice = priceText.replace(",", "").toDouble()
                    if (targetPrice > 0) {
                        alertManager.setUpperPriceAlert(targetPrice)
                        upperPriceInput.text.clear()
                        updateStatusText(statusText)
                        showToast("🚀 Upper alert set: $${String.format("%,.0f", targetPrice)}")
                    } else {
                        showToast("❌ Please enter a valid price")
                    }
                } catch (e: Exception) {
                    showToast("❌ Invalid price format")
                }
            } else {
                showToast("❌ Please enter a price")
            }
        }
        
        // Set lower price alert (buy opportunity)
        setLowerButton.setOnClickListener {
            val priceText = lowerPriceInput.text.toString()
            if (priceText.isNotEmpty()) {
                try {
                    val targetPrice = priceText.replace(",", "").toDouble()
                    if (targetPrice > 0) {
                        alertManager.setLowerPriceAlert(targetPrice)
                        lowerPriceInput.text.clear()
                        updateStatusText(statusText)
                        showToast("📉 Lower alert set: $${String.format("%,.0f", targetPrice)}")
                    } else {
                        showToast("❌ Please enter a valid price")
                    }
                } catch (e: Exception) {
                    showToast("❌ Invalid price format")
                }
            } else {
                showToast("❌ Please enter a price")
            }
        }
        
        // Clear all alerts
        clearAlertsButton.setOnClickListener {
            alertManager.clearUpperAlert()
            alertManager.clearLowerAlert()
            alertManager.disableAlerts()
            enableSwitch.isChecked = false
            updateStatusText(statusText)
            showToast("🗑️ All alerts cleared")
        }
        
        // Test alert button
        testAlertButton?.setOnClickListener {
            alertManager.testAlert()
            showToast("🧪 Test notification sent!")
        }
        
        // Back button
        backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun loadCurrentSettings() {
        val enableSwitch = findViewById<Switch>(R.id.alerts_enable_switch)
        val statusText = findViewById<TextView>(R.id.alert_status_text)
        
        val (enabled, upperLimit, lowerLimit) = alertManager.getAlertSettings()
        enableSwitch.isChecked = enabled
        
        updateStatusText(statusText)
    }
    
    private fun updateStatusText(statusText: TextView) {
        val (enabled, upperLimit, lowerLimit) = alertManager.getAlertSettings()
        
        val status = when {
            !enabled -> "🔕 Alerts disabled"
            upperLimit != null && lowerLimit != null -> 
                "🔔 Active alerts:\n🚀 Sell at: $${String.format("%,.0f", upperLimit)}\n📉 Buy at: $${String.format("%,.0f", lowerLimit)}"
            upperLimit != null -> 
                "🔔 Active alert:\n🚀 Sell at: $${String.format("%,.0f", upperLimit)}"
            lowerLimit != null -> 
                "🔔 Active alert:\n📉 Buy at: $${String.format("%,.0f", lowerLimit)}"
            else -> "⚠️ No alerts set"
        }
        
        statusText.text = status
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}