package com.example.bitcoinwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.bitcoinwidget.updates.AutoUpdateManager
import kotlinx.coroutines.*

class MainActivity : Activity() {
    
    private val apiService = ApiService()
    private lateinit var autoUpdateManager: AutoUpdateManager
    private val TAG = "MainActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "🏠 MainActivity created")
        
        // Set content view to our layout
        setContentView(R.layout.activity_main)
        
        // Initialize auto update manager
        autoUpdateManager = AutoUpdateManager(this)
        
        // Start auto-update checking if enabled
        if (autoUpdateManager.isAutoUpdateEnabled()) {
            autoUpdateManager.startPeriodicUpdateCheck()
        }
        
        setupButtons()
        updateWidgetCount()
        updateStatus("Ready")
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
        Log.d(TAG, "📱 MainActivity resumed")
    }
}