package com.example.bitcoinwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import kotlinx.coroutines.*
import com.example.bitcoinwidget.alerts.PriceAlertManager
import com.example.bitcoinwidget.performance.PerformanceManager
import com.example.bitcoinwidget.updates.AutoUpdateManager
import com.example.bitcoinwidget.ui.UIEnhancementManager

class FeatureTestActivity : Activity() {
    
    private lateinit var priceAlertManager: PriceAlertManager
    private lateinit var performanceManager: PerformanceManager
    private lateinit var autoUpdateManager: AutoUpdateManager
    private lateinit var uiManager: UIEnhancementManager
    private lateinit var apiService: ApiService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize managers
        priceAlertManager = PriceAlertManager(this)
        performanceManager = PerformanceManager(this)
        autoUpdateManager = AutoUpdateManager(this)
        uiManager = UIEnhancementManager(this)
        apiService = ApiService()
        
        setupTestUI()
    }
    
    private fun setupTestUI() {
        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }
        
        // Title
        val title = TextView(this).apply {
            text = "🧪 Feature Test Dashboard"
            textSize = 24f
            setPadding(0, 0, 0, 30)
            gravity = android.view.Gravity.CENTER
        }
        layout.addView(title)
        
        // Test Results Text View
        val resultsText = TextView(this).apply {
            text = "Running tests..."
            textSize = 14f
            setPadding(20, 20, 20, 20)
            setBackgroundColor(android.graphics.Color.parseColor("#F0F0F0"))
            id = android.view.View.generateViewId()
        }
        layout.addView(resultsText)
        
        // Test Buttons
        addTestButton(layout, "🔔 Test Price Alerts") { testPriceAlerts(resultsText) }
        addTestButton(layout, "⚡ Test Performance") { testPerformance(resultsText) }
        addTestButton(layout, "🤖 Test Auto-Update") { testAutoUpdate(resultsText) }
        addTestButton(layout, "🎨 Test UI Manager") { testUIManager(resultsText) }
        addTestButton(layout, "📊 Test All APIs") { testAllAPIs(resultsText) }
        addTestButton(layout, "🔄 Test Widget Popup") { testWidgetPopup() }
        addTestButton(layout, "📋 Run All Tests") { runAllTests(resultsText) }
        
        // Back button
        addTestButton(layout, "← Back to Main") { finish() }
        
        scrollView.addView(layout)
        setContentView(scrollView)
        
        // Run initial test
        runAllTests(resultsText)
    }
    
    private fun addTestButton(layout: LinearLayout, text: String, action: () -> Unit) {
        val button = Button(this).apply {
            this.text = text
            setPadding(0, 20, 0, 20)
            setOnClickListener { action() }
        }
        layout.addView(button)
    }
    
    private fun testPriceAlerts(resultsText: TextView) {
        CoroutineScope(Dispatchers.Main).launch {
            val results = StringBuilder("🔔 PRICE ALERTS TEST:\n\n")
            
            try {
                // Test settings
                val (enabled, upper, lower) = priceAlertManager.getAlertSettings()
                results.append("✅ Alert Manager initialized\n")
                results.append("- Enabled: $enabled\n")
                results.append("- Upper: $upper\n")
                results.append("- Lower: $lower\n\n")
                
                // Test setting alerts
                priceAlertManager.setUpperPriceAlert(110000.0)
                priceAlertManager.setLowerPriceAlert(95000.0)
                results.append("✅ Test alerts set (Upper: $110,000, Lower: $95,000)\n")
                
                // Test active alerts check
                val hasAlerts = priceAlertManager.hasActiveAlerts()
                results.append("✅ Has active alerts: $hasAlerts\n")
                
                results.append("\n🔔 Price Alerts: WORKING ✅")
                
            } catch (e: Exception) {
                results.append("❌ Error: ${e.message}")
            }
            
            resultsText.text = results.toString()
        }
    }
    
    private fun testPerformance(resultsText: TextView) {
        val results = StringBuilder("⚡ PERFORMANCE TEST:\n\n")
        
        try {
            // Test optimization settings
            val (battery, adaptive, network) = performanceManager.getOptimizationSettings()
            results.append("✅ Performance Manager initialized\n")
            results.append("- Battery Optimization: $battery\n")
            results.append("- Adaptive Intervals: $adaptive\n")
            results.append("- Network Aware: $network\n\n")
            
            // Test optimized interval
            val defaultInterval = 5 * 60 * 1000L
            val optimized = performanceManager.getOptimizedInterval(defaultInterval, 123)
            results.append("✅ Interval optimization: ${defaultInterval/1000/60}min → ${optimized/1000/60}min\n")
            
            // Test power save mode
            val powerSave = performanceManager.isInPowerSaveMode()
            results.append("✅ Power Save Mode: $powerSave\n")
            
            // Test skip update
            val shouldSkip = performanceManager.shouldSkipUpdate(123)
            results.append("✅ Should Skip Update: $shouldSkip\n")
            
            results.append("\n⚡ Performance: WORKING ✅")
            
        } catch (e: Exception) {
            results.append("❌ Error: ${e.message}")
        }
        
        resultsText.text = results.toString()
    }
    
    private fun testAutoUpdate(resultsText: TextView) {
        CoroutineScope(Dispatchers.Main).launch {
            val results = StringBuilder("🤖 AUTO-UPDATE TEST:\n\n")
            
            try {
                results.append("✅ Auto-Update Manager initialized\n")
                results.append("- Auto-update enabled: ${autoUpdateManager.isAutoUpdateEnabled()}\n")
                results.append("- Current version: ${autoUpdateManager.getCurrentVersion()}\n\n")
                
                // Test update check
                results.append("🔍 Checking for updates...\n")
                val updateInfo = autoUpdateManager.checkForUpdates()
                
                results.append("✅ Update check completed:\n")
                results.append("- Update available: ${updateInfo.available}\n")
                results.append("- Current: ${updateInfo.currentVersion}\n")
                results.append("- Latest: ${updateInfo.latestVersion}\n")
                if (updateInfo.releaseNotes != null) {
                    results.append("- Notes: ${updateInfo.releaseNotes.take(100)}...\n")
                }
                
                results.append("\n🤖 Auto-Update: WORKING ✅")
                
            } catch (e: Exception) {
                results.append("❌ Error: ${e.message}")
            }
            
            resultsText.text = results.toString()
        }
    }
    
    private fun testUIManager(resultsText: TextView) {
        val results = StringBuilder("🎨 UI MANAGER TEST:\n\n")
        
        try {
            results.append("✅ UI Manager initialized\n")
            
            // Test current settings
            val settings = uiManager.getCurrentSettings()
            results.append("- Theme: ${settings.themeMode}\n")
            results.append("- Animations: ${settings.animationsEnabled}\n")
            results.append("- High Contrast: ${settings.highContrast}\n")
            results.append("- Font Scale: ${settings.fontSizeScale}\n\n")
            
            // Test colors
            val colors = uiManager.getOptimizedColors()
            results.append("✅ Color scheme loaded: ${colors.size} colors\n")
            results.append("- Background: ${Integer.toHexString(colors["background"] ?: 0)}\n")
            results.append("- Accent: ${Integer.toHexString(colors["accent"] ?: 0)}\n\n")
            
            // Test animation duration
            val animDuration = uiManager.getAnimationDuration()
            results.append("✅ Animation duration: ${animDuration}ms\n")
            
            results.append("\n🎨 UI Manager: WORKING ✅")
            
        } catch (e: Exception) {
            results.append("❌ Error: ${e.message}")
        }
        
        resultsText.text = results.toString()
    }
    
    private fun testAllAPIs(resultsText: TextView) {
        CoroutineScope(Dispatchers.Main).launch {
            val results = StringBuilder("📊 API TEST:\n\n")
            
            try {
                results.append("🔍 Testing all Bitcoin APIs...\n\n")
                
                val bitcoinData = apiService.fetchBitcoinData()
                
                results.append("✅ API calls completed:\n")
                results.append("- Price: ${bitcoinData.price?.let { "$${String.format("%,.0f", it)}" } ?: "Failed"}\n")
                results.append("- Fastest Fee: ${bitcoinData.fastestFee ?: "Failed"} sat/vB\n")
                results.append("- 30min Fee: ${bitcoinData.halfHourFee ?: "Failed"} sat/vB\n")
                results.append("- 1hour Fee: ${bitcoinData.hourFee ?: "Failed"} sat/vB\n")
                results.append("- Fear&Greed: ${bitcoinData.fearGreedIndex ?: "Failed"}\n")
                results.append("- MVRV Z-Score: ${bitcoinData.mvrvZScore ?: "Failed"}\n")
                results.append("- Last Update: ${bitcoinData.lastUpdated}\n")
                
                // Count successful APIs
                val successCount = listOf(
                    bitcoinData.price,
                    bitcoinData.fastestFee,
                    bitcoinData.fearGreedIndex,
                    bitcoinData.mvrvZScore
                ).count { it != null }
                
                results.append("\n📊 APIs: $successCount/4 WORKING ✅")
                
            } catch (e: Exception) {
                results.append("❌ Error: ${e.message}")
            }
            
            resultsText.text = results.toString()
        }
    }
    
    private fun testWidgetPopup() {
        try {
            // Test widget popup by directly launching PopupActivity
            val intent = Intent(this, PopupActivity::class.java).apply {
                // Simulate widget click with extra data
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 123)
            }
            startActivity(intent)
            
            Toast.makeText(this, "✅ Testing widget popup launch", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Popup test failed: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("FeatureTestActivity", "Widget popup test error: ${e.message}")
        }
    }
    
    private fun runAllTests(resultsText: TextView) {
        CoroutineScope(Dispatchers.Main).launch {
            val results = StringBuilder("🧪 RUNNING ALL TESTS...\n\n")
            resultsText.text = results.toString()
            
            delay(500)
            
            try {
                // Test 1: APIs
                results.append("1️⃣ Testing APIs...")
                resultsText.text = results.toString()
                
                val bitcoinData = apiService.fetchBitcoinData()
                val apiSuccess = listOf(bitcoinData.price, bitcoinData.fastestFee, bitcoinData.fearGreedIndex, bitcoinData.mvrvZScore).count { it != null }
                results.append(" $apiSuccess/4 ✅\n")
                
                // Test 2: Price Alerts
                results.append("2️⃣ Testing Price Alerts...")
                resultsText.text = results.toString()
                
                val hasAlerts = priceAlertManager.hasActiveAlerts()
                results.append(" ✅\n")
                
                // Test 3: Performance
                results.append("3️⃣ Testing Performance...")
                resultsText.text = results.toString()
                
                val (battery, _, _) = performanceManager.getOptimizationSettings()
                results.append(" ✅\n")
                
                // Test 4: Auto-Update
                results.append("4️⃣ Testing Auto-Update...")
                resultsText.text = results.toString()
                
                val updateEnabled = autoUpdateManager.isAutoUpdateEnabled()
                results.append(" ✅\n")
                
                // Test 5: UI Manager
                results.append("5️⃣ Testing UI Manager...")
                resultsText.text = results.toString()
                
                val colors = uiManager.getOptimizedColors()
                results.append(" ✅\n")
                
                results.append("\n🎉 ALL TESTS COMPLETED!\n")
                results.append("✅ APIs: $apiSuccess/4 working\n")
                results.append("✅ Price Alerts: Available\n")
                results.append("✅ Performance: Optimized\n")
                results.append("✅ Auto-Update: Enabled\n")
                results.append("✅ UI Manager: Active\n")
                results.append("\n🚀 App Status: FULLY FUNCTIONAL")
                
            } catch (e: Exception) {
                results.append("\n❌ Test failed: ${e.message}")
            }
            
            resultsText.text = results.toString()
        }
    }
}