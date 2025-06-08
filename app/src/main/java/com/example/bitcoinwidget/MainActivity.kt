package com.example.bitcoinwidget

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bitcoinwidget.alerts.NotificationHelper
import com.example.bitcoinwidget.alerts.PriceAlertManager
import com.example.bitcoinwidget.updates.AutoUpdateManager
import kotlinx.coroutines.*

class MainActivity : Activity() {
    
    private val apiService = ApiService()
    private lateinit var autoUpdateManager: AutoUpdateManager
    private lateinit var priceAlertManager: PriceAlertManager
    private val TAG = "MainActivity"
    
    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "🏠 MainActivity created")
        
        // Set content view to our layout
        setContentView(R.layout.activity_main)
        
        // Initialize managers
        autoUpdateManager = AutoUpdateManager(this)
        priceAlertManager = PriceAlertManager(this)
        
        // Setup notifications
        setupNotifications()
        
        // Start auto-update checking if enabled
        if (autoUpdateManager.isAutoUpdateEnabled()) {
            autoUpdateManager.startPeriodicUpdateCheck()
        }
        
        // Start price monitoring if alerts are enabled
        if (priceAlertManager.hasActiveAlerts()) {
            priceAlertManager.startMonitoring()
            Log.d(TAG, "🔔 Price alert monitoring started")
        }
        
        setupButtons()
        updateWidgetCount()
        updateStatus("Ready")
    }
    
    private fun setupNotifications() {
        // Create notification channel
        NotificationHelper.createNotificationChannel(this)
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
                Log.d(TAG, "📱 Requesting notification permission")
            } else {
                Log.d(TAG, "✅ Notification permission already granted")
            }
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "✅ Notification permission granted")
                    Toast.makeText(this, "✅ Notifications enabled", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w(TAG, "❌ Notification permission denied")
                    Toast.makeText(this, "⚠️ Price alerts need notification permission", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun setupButtons() {
        // Force Update Widget Button
        findViewById<Button>(R.id.btn_force_update_widget).setOnClickListener {
            Log.d(TAG, "🔄 Force update button clicked")
            updateStatus("Forcing widget update...")
            forceUpdateAllWidgets()
        }
        
        // Test API Connection Button
        findViewById<Button>(R.id.btn_test_api).setOnClickListener {
            Log.d(TAG, "🌐 Test API button clicked")
            updateStatus("Testing API connections...")
            testApiConnections()
        }
        
        // Price Alerts Button
        findViewById<Button>(R.id.btn_price_alerts)?.setOnClickListener {
            Log.d(TAG, "🔔 Price alerts button clicked")
            val intent = Intent(this, PriceAlertsActivity::class.java)
            startActivity(intent)
        }
        
        // Feature Test Button
        findViewById<Button>(R.id.btn_feature_test).setOnClickListener {
            Log.d(TAG, "🧪 Feature test button clicked")
            val intent = Intent(this, FeatureTestActivity::class.java)
            startActivity(intent)
        }
        
        // Widget Test Button (repurpose existing button or add new)
        findViewById<Button>(R.id.btn_widget_settings)?.apply {
            text = "🧪 Widget Test & Fix"
            setOnClickListener {
                Log.d(TAG, "🧪 Widget test button clicked")
                val intent = Intent(this@MainActivity, WidgetTestActivity::class.java)
                startActivity(intent)
            }
        }
    }
    
    private fun forceUpdateAllWidgets() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(this@MainActivity)
                val thisWidget = ComponentName(this@MainActivity, BitcoinPriceWidget::class.java)
                val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                
                Log.d(TAG, "📱 Found ${allWidgetIds.size} widgets to force update")
                updateStatus("Updating ${allWidgetIds.size} widgets...")
                
                if (allWidgetIds.isEmpty()) {
                    updateStatus("No widgets found. Add widget to home screen first.")
                    Toast.makeText(this@MainActivity, "No widgets found", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                for (appWidgetId in allWidgetIds) {
                    Log.d(TAG, "⚡ Force updating widget $appWidgetId")
                    BitcoinPriceWidget.updateWidget(this@MainActivity, appWidgetManager, appWidgetId)
                }
                
                updateStatus("All widgets updated successfully!")
                updateWidgetCount()
                
                Toast.makeText(this@MainActivity, "✅ All widgets updated!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error force updating widgets: ${e.message}")
                updateStatus("Error updating widgets: ${e.message}")
                Toast.makeText(this@MainActivity, "❌ Error updating widgets", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateWidgetCount() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val thisWidget = ComponentName(this, BitcoinPriceWidget::class.java)
        val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
        
        findViewById<TextView>(R.id.widget_count_text).text = "Widgets: ${allWidgetIds.size} active"
        Log.d(TAG, "📊 Widget count updated: ${allWidgetIds.size} active widgets")
    }
    
    private fun updateStatus(message: String) {
        findViewById<TextView>(R.id.status_text).text = message
        Log.d(TAG, "📋 Status: $message")
    }
    
    private fun testApiConnections() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.Main) {
                    updateStatus("Testing Bitcoin price API...")
                }
                
                val bitcoinData = apiService.fetchBitcoinData()
                
                withContext(Dispatchers.Main) {
                    if (bitcoinData.price != null) {
                        val statusMessage = "✅ API Test Successful!\n" +
                                "Price: $${String.format("%,.0f", bitcoinData.price)}\n" +
                                "Updated: ${bitcoinData.lastUpdated}"
                        updateStatus(statusMessage)
                        Toast.makeText(this@MainActivity, "✅ API connections working!", Toast.LENGTH_SHORT).show()
                    } else {
                        updateStatus("❌ API test failed - no price data")
                        Toast.makeText(this@MainActivity, "❌ API test failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ API test error: ${e.message}")
                withContext(Dispatchers.Main) {
                    updateStatus("❌ API test error: ${e.message}")
                    Toast.makeText(this@MainActivity, "❌ API test failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        updateWidgetCount()
        
        // Restart price monitoring if alerts are enabled
        if (priceAlertManager.hasActiveAlerts()) {
            priceAlertManager.startMonitoring()
            Log.d(TAG, "🔔 Price alert monitoring restarted")
        }
        
        Log.d(TAG, "📱 MainActivity resumed")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Don't stop monitoring here - we want alerts to continue in background
        Log.d(TAG, "📱 MainActivity destroyed (monitoring continues)")
    }
}