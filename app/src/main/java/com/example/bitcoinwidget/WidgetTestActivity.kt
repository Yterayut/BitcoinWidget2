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
import kotlinx.coroutines.*

/**
 * Test Activity to force widget updates and test price fetching
 */
class WidgetTestActivity : Activity() {
    
    private val TAG = "WidgetTestActivity"
    private val apiService = ApiService()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Reuse main layout
        
        Log.d(TAG, "🧪 WidgetTestActivity started")
        
        setupTestButtons()
        updateStatus("Widget Test Ready")
    }
    
    private fun setupTestButtons() {
        // Force Update All Widgets
        findViewById<Button>(R.id.btn_force_update_widget)?.setOnClickListener {
            Log.d(TAG, "🔄 Force update all widgets clicked")
            forceUpdateAllWidgets()
        }
        
        // Test API Direct
        findViewById<Button>(R.id.btn_test_api)?.setOnClickListener {
            Log.d(TAG, "🌐 Direct API test clicked")
            testApiDirect()
        }
        
        // Test Single Widget
        findViewById<Button>(R.id.btn_widget_settings)?.apply {
            text = "🔧 Test Single Widget"
            setOnClickListener {
                Log.d(TAG, "🔧 Test single widget clicked")
                testSingleWidget()
            }
        }
        
        // Price Alerts (keep original)
        findViewById<Button>(R.id.btn_price_alerts)?.setOnClickListener {
            val intent = Intent(this, PriceAlertsActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun forceUpdateAllWidgets() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                updateStatus("🔄 Force updating all widgets...")
                
                val appWidgetManager = AppWidgetManager.getInstance(this@WidgetTestActivity)
                val thisWidget = ComponentName(this@WidgetTestActivity, BitcoinPriceWidget::class.java)
                val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                
                Log.d(TAG, "📱 Found ${allWidgetIds.size} widgets to update")
                updateStatus("Found ${allWidgetIds.size} widgets to update")
                
                if (allWidgetIds.isEmpty()) {
                    updateStatus("❌ No widgets found. Add widget to home screen first.")
                    Toast.makeText(this@WidgetTestActivity, "No widgets found", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                // Force immediate updates
                for (appWidgetId in allWidgetIds) {
                    Log.d(TAG, "⚡ Force updating widget $appWidgetId")
                    
                    // Use the enhanced update method
                    BitcoinPriceWidget.updateWidget(this@WidgetTestActivity, appWidgetManager, appWidgetId)
                    
                    delay(500) // Small delay between widget updates
                }
                
                updateStatus("✅ All ${allWidgetIds.size} widgets updated!")
                updateWidgetCount()
                
                Toast.makeText(this@WidgetTestActivity, "✅ All widgets updated!", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error force updating widgets: ${e.message}", e)
                updateStatus("❌ Error updating widgets: ${e.message}")
                Toast.makeText(this@WidgetTestActivity, "❌ Error updating widgets", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun testApiDirect() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.Main) {
                    updateStatus("🌐 Testing API connections...")
                }
                
                Log.d(TAG, "🌐 Starting direct API test")
                val startTime = System.currentTimeMillis()
                
                val bitcoinData = apiService.fetchBitcoinData()
                val duration = System.currentTimeMillis() - startTime
                
                Log.d(TAG, "📊 API test result: price=${bitcoinData.price}, duration=${duration}ms")
                
                withContext(Dispatchers.Main) {
                    if (bitcoinData.price != null) {
                        val statusMessage = "✅ API Test Successful! (${duration}ms)\n" +
                                "💰 Price: $${String.format("%,.0f", bitcoinData.price)}\n" +
                                "📈 Change: ${bitcoinData.priceChangePercent24h ?: "N/A"}%\n" +
                                "🔗 API Health: ${bitcoinData.apiStatus?.overallHealth ?: "Unknown"}\n" +
                                "⏰ Updated: ${bitcoinData.lastUpdated}"
                        updateStatus(statusMessage)
                        Toast.makeText(this@WidgetTestActivity, "✅ API working! Price: $${String.format("%,.0f", bitcoinData.price)}", Toast.LENGTH_LONG).show()
                    } else {
                        updateStatus("❌ API test failed - no price data")
                        Toast.makeText(this@WidgetTestActivity, "❌ API test failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ API test error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    updateStatus("❌ API test error: ${e.message}")
                    Toast.makeText(this@WidgetTestActivity, "❌ API test failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun testSingleWidget() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(this@WidgetTestActivity)
                val thisWidget = ComponentName(this@WidgetTestActivity, BitcoinPriceWidget::class.java)
                val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                
                if (allWidgetIds.isNotEmpty()) {
                    val firstWidgetId = allWidgetIds[0]
                    updateStatus("🔧 Testing widget $firstWidgetId...")
                    
                    // Test single widget update
                    BitcoinPriceWidget.updateWidget(this@WidgetTestActivity, appWidgetManager, firstWidgetId)
                    
                    updateStatus("✅ Widget $firstWidgetId test completed!")
                    Toast.makeText(this@WidgetTestActivity, "✅ Widget $firstWidgetId tested!", Toast.LENGTH_SHORT).show()
                } else {
                    updateStatus("❌ No widgets found for testing")
                    Toast.makeText(this@WidgetTestActivity, "❌ No widgets found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error testing single widget: ${e.message}", e)
                updateStatus("❌ Widget test error: ${e.message}")
                Toast.makeText(this@WidgetTestActivity, "❌ Widget test failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateStatus(message: String) {
        findViewById<TextView>(R.id.status_text)?.text = message
        Log.d(TAG, "📋 Status: $message")
    }
    
    private fun updateWidgetCount() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val thisWidget = ComponentName(this, BitcoinPriceWidget::class.java)
        val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
        
        findViewById<TextView>(R.id.widget_count_text)?.text = "Widgets: ${allWidgetIds.size} active"
        Log.d(TAG, "📊 Widget count updated: ${allWidgetIds.size} active widgets")
    }
    
    override fun onResume() {
        super.onResume()
        updateWidgetCount()
        Log.d(TAG, "📱 WidgetTestActivity resumed")
    }
    
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, WidgetTestActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
        }
    }
}
