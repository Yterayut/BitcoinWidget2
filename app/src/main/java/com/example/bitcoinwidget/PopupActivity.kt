package com.example.bitcoinwidget

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class PopupActivity : Activity() {

    private val apiService = ApiService()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var isRefreshing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("PopupActivity", "🔥 Enhanced PopupActivity onCreate() called")
        
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.popup_layout)
            
            Log.d("PopupActivity", "✅ Enhanced layout set successfully")

            // Setup SwipeRefreshLayout if available
            setupSwipeRefresh()
            
            // Setup main app button
            setupMainAppButton()
            
            // Setup BTC price header click
            setupBtcPriceClick()

            // Load and display data
            loadEnhancedData()

            // ปิด Activity เมื่อกดนอกพื้นที่
            setFinishOnTouchOutside(true)
            
            Log.d("PopupActivity", "🎉 Enhanced PopupActivity setup complete")
            
        } catch (e: Exception) {
            Log.e("PopupActivity", "❌ Error in enhanced onCreate: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupMainAppButton() {
        // Main App Button
        val mainAppButton = findViewById<Button>(R.id.btn_open_main_app)
        mainAppButton?.setOnClickListener {
            try {
                // เปิดแอปหลัก (MainActivity)
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)
                
                // ปิด popup
                finish()
                
                Log.d("PopupActivity", "✅ Opening main app")
            } catch (e: Exception) {
                Log.e("PopupActivity", "❌ Error opening main app: ${e.message}")
            }
        }
        
        // Price Alerts Quick Button
        val priceAlertsButton = findViewById<Button>(R.id.btn_alerts)
        priceAlertsButton?.setOnClickListener {
            try {
                // เปิดหน้า Price Alerts โดยตรง
                val intent = Intent(this, PriceAlertsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)
                
                // ปิด popup
                finish()
                
                Log.d("PopupActivity", "✅ Opening price alerts")
            } catch (e: Exception) {
                Log.e("PopupActivity", "❌ Error opening price alerts: ${e.message}")
            }
        }
        
        // Settings Quick Button
        val settingsButton = findViewById<Button>(R.id.btn_settings)
        settingsButton?.setOnClickListener {
            try {
                // เปิดหน้า Settings (Refresh Interval)
                val intent = Intent(this, RefreshIntervalActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)
                
                // ปิด popup
                finish()
                
                Log.d("PopupActivity", "✅ Opening settings")
            } catch (e: Exception) {
                Log.e("PopupActivity", "❌ Error opening settings: ${e.message}")
            }
        }
    }
    
    private fun setupBtcPriceClick() {
        // BTC Price Header Click - ปิด popup กลับไป Home Screen
        val btcPriceHeader = findViewById<LinearLayout>(R.id.btc_price_header)
        btcPriceHeader?.setOnClickListener {
            try {
                // ปิด popup กลับไป Home Screen
                finish()
                
                Log.d("PopupActivity", "✅ Closing popup to return to Home Screen")
            } catch (e: Exception) {
                Log.e("PopupActivity", "❌ Error closing popup: ${e.message}")
            }
        }
    }

    private fun setupSwipeRefresh() {
        try {
            swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
            swipeRefreshLayout.setOnRefreshListener {
                Log.d("PopupActivity", "🔄 Pull-to-refresh triggered")
                refreshData()
            }
            
            // Set custom colors
            swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark,
                android.R.color.holo_green_dark
            )
        } catch (e: Exception) {
            Log.w("PopupActivity", "⚠️ SwipeRefreshLayout not found in layout, continuing without pull-to-refresh")
        }
    }
    
    private fun refreshData() {
        if (isRefreshing) {
            Log.d("PopupActivity", "⚠️ Already refreshing, skipping duplicate request")
            return
        }
        
        isRefreshing = true
        loadEnhancedData(forceRefresh = true)
    }
    
    private fun loadEnhancedData(forceRefresh: Boolean = false) {
        CoroutineScope(Dispatchers.Main + SupervisorJob()).launch {
            try {
                // Show loading state
                updateConnectionStatus("Connecting...")
                
                if (!forceRefresh) {
                    // Try to get cached data first for faster display
                    val prefs = getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
                    displayCachedData(prefs)
                }

                // Then fetch fresh data
                val bitcoinData = apiService.fetchBitcoinData()
                displayEnhancedFreshData(bitcoinData)
                
                // Update connection status
                updateConnectionStatus("Connected", bitcoinData.apiStatus)

            } catch (e: Exception) {
                Log.e("PopupActivity", "Error loading enhanced data: ${e.message}")
                updateConnectionStatus("Connection Error")
                displayErrorData()
            } finally {
                isRefreshing = false
                try {
                    if (::swipeRefreshLayout.isInitialized) {
                        swipeRefreshLayout.isRefreshing = false
                    }
                } catch (e: Exception) {
                    // SwipeRefreshLayout not available
                }
            }
        }
    }
    
    private fun updateConnectionStatus(status: String, apiHealth: ApiService.ApiHealthStatus? = null) {
        try {
            val statusText = findViewById<TextView>(R.id.connection_status)
            val healthIndicator = when {
                apiHealth?.overallHealth == "Good" -> "🟢 $status"
                apiHealth?.overallHealth == "Degraded" -> "🟡 $status"
                status.contains("Error") -> "🔴 $status"
                status.contains("Connecting") -> "🔵 $status"
                else -> "⚪ $status"
            }
            statusText?.text = healthIndicator
            Log.d("PopupActivity", "📊 Connection status updated: $healthIndicator")
        } catch (e: Exception) {
            Log.w("PopupActivity", "⚠️ Connection status view not found")
        }
    }
    
    private fun displayEnhancedFreshData(bitcoinData: ApiService.BitcoinData) {
        // Update BTC Price with trend indicator
        bitcoinData.price?.let { price ->
            val priceText = String.format("$%,.0f", price)
            findViewById<TextView>(R.id.btc_price_popup)?.text = priceText
            
            // Add trend indicator
            bitcoinData.priceChangePercent24h?.let { changePercent ->
                val trendIcon = if (changePercent >= 0) "📈" else "📉"
                val changeColor = if (changePercent >= 0) "#4CAF50" else "#F44336"
                val changeText = String.format("%.2f%%", changePercent)
                
                findViewById<TextView>(R.id.price_change_24h)?.apply {
                    text = "$trendIcon $changeText"
                    setTextColor(android.graphics.Color.parseColor(changeColor))
                }
                
                Log.d("PopupActivity", "💹 Price trend updated: $changeText ($trendIcon)")
            }
        }

        // Update Market Data
        bitcoinData.marketCap?.let { marketCap ->
            val marketCapText = String.format("$%.1fB", marketCap / 1_000_000_000)
            findViewById<TextView>(R.id.market_cap)?.text = marketCapText
        }
        
        bitcoinData.dominance?.let { dominance ->
            findViewById<TextView>(R.id.btc_dominance)?.text = String.format("%.1f%%", dominance)
        }
        
        // Update Network Data
        bitcoinData.blockHeight?.let { blockHeight ->
            findViewById<TextView>(R.id.block_height)?.text = String.format("%,d", blockHeight)
        }
        
        bitcoinData.halvingCountdown?.let { countdown ->
            findViewById<TextView>(R.id.halving_countdown)?.text = countdown
        }
        
        // Update Mining Cost vs Price
        bitcoinData.miningCost?.let { miningCost ->
            bitcoinData.price?.let { price ->
                val ratio = price / miningCost
                val statusText = if (price > miningCost) {
                    "🟢 ${String.format("%.1fx", ratio)} above cost"
                } else {
                    "🔴 ${String.format("%.1fx", ratio)} below cost"
                }
                findViewById<TextView>(R.id.mining_cost_status)?.text = statusText
            }
        }
        
        // Update Supply Info
        bitcoinData.circulatingSupply?.let { circulating ->
            val supplyText = String.format("%.0f / 21M BTC", circulating)
            findViewById<TextView>(R.id.supply_info)?.text = supplyText
        }

        // Update existing data (MVRV, Fear & Greed, Fees)
        updateExistingData(bitcoinData)

        // Update enhanced timestamp
        val enhancedTimestamp = SimpleDateFormat("EEE MMM dd, HH:mm:ss", Locale.getDefault()).format(Date())
        findViewById<TextView>(R.id.last_updated)?.text = "Updated: $enhancedTimestamp"
    }
    
    private fun updateExistingData(bitcoinData: ApiService.BitcoinData) {
        // Call original display method for existing data
        displayFreshData(bitcoinData)
    }
    private fun loadData() {
        // Legacy method - redirect to enhanced data loading
        loadEnhancedData()
    }

    private fun displayCachedData(prefs: android.content.SharedPreferences) {
        // Try to find any widget's cached data
        val allPrefs = prefs.all
        var latestPrice: Float? = null
        var fastestFee: Int? = null
        var halfHourFee: Int? = null
        var hourFee: Int? = null
        var mvrvZScore: Float? = null
        var fearGreedIndex: Int? = null
        var fearGreedClassification: String? = null

        // Look for any cached widget data
        for ((key, value) in allPrefs) {
            when {
                key.startsWith("latest_price_") && value is Float -> latestPrice = value
                key.startsWith("fastest_fee_") && value is Int -> fastestFee = value
                key.startsWith("half_hour_fee_") && value is Int -> halfHourFee = value
                key.startsWith("hour_fee_") && value is Int -> hourFee = value
                key.startsWith("mvrv_z_score_") && value is Float -> mvrvZScore = value
                key.startsWith("fear_greed_index_") && value is Int -> fearGreedIndex = value
                key.startsWith("fear_greed_classification_") && value is String -> fearGreedClassification = value
            }
        }

        // Display cached data
        latestPrice?.let {
            findViewById<TextView>(R.id.btc_price_popup)?.text = String.format("$%,.0f", it)
        }

        mvrvZScore?.let {
            findViewById<TextView>(R.id.mvrv_value)?.text = "${String.format("%.2f", it)} - ${getMvrvZScoreStatus(it.toDouble())}"
        }

        fastestFee?.let {
            findViewById<TextView>(R.id.fee_urgent)?.text = "$it sat/"
        }

        halfHourFee?.let {
            findViewById<TextView>(R.id.fee_medium)?.text = "$it sat/"
        }

        hourFee?.let {
            findViewById<TextView>(R.id.fee_low)?.text = "$it sat/"
        }

        // Also update fee_high (fallback to fastest fee)
        fastestFee?.let {
            findViewById<TextView>(R.id.fee_high)?.text = "$it sat/"
        }

        // Display Fear & Greed Index
        fearGreedIndex?.let { index ->
            findViewById<TextView>(R.id.fear_greed_value)?.text = index.toString()
            val emoji = when {
                index < 25 -> "😱"
                index < 45 -> "😰"
                index < 55 -> "😐"
                index < 75 -> "😊"
                else -> "🤑"
            }
            val displayText = fearGreedClassification?.let { "$emoji $it" } ?: "$emoji ${getFearGreedLabel(index)}"
            findViewById<TextView>(R.id.fear_greed_label)?.text = displayText
            
            // Update gauge needle position (optional: could be dynamic based on value)
            updateFearGreedGauge(index)
            
            // Display historical data (mock data for now - could be fetched from API)
            displayFearGreedHistoricalData(index)
        }
    }

    private fun displayFreshData(bitcoinData: ApiService.BitcoinData) {
        // Update BTC Price
        bitcoinData.price?.let { price ->
            findViewById<TextView>(R.id.btc_price_popup)?.text = String.format("$%,.0f", price)
        }

        // Update MVRV Z-Score
        bitcoinData.mvrvZScore?.let { zScore ->
            val status = getMvrvZScoreStatus(zScore)
            findViewById<TextView>(R.id.mvrv_value)?.text = "${String.format("%.2f", zScore)} - $status"
        }

        // Update Mining Fees
        bitcoinData.hourFee?.let { fee ->
            findViewById<TextView>(R.id.fee_low)?.text = "$fee sat/"
        }

        bitcoinData.halfHourFee?.let { fee ->
            findViewById<TextView>(R.id.fee_medium)?.text = "$fee sat/"
        }

        bitcoinData.fastestFee?.let { fee ->
            findViewById<TextView>(R.id.fee_urgent)?.text = "$fee sat/"
        }

        // Also update fee_high if available (fallback to fastest fee)
        bitcoinData.fastestFee?.let { fee ->
            findViewById<TextView>(R.id.fee_high)?.text = "$fee sat/"
        }

        // Update Real Fear & Greed Index
        bitcoinData.fearGreedIndex?.let { index ->
            findViewById<TextView>(R.id.fear_greed_value)?.text = index.toString()

            val emoji = when {
                index < 25 -> "😱"
                index < 45 -> "😰"
                index < 55 -> "😐"
                index < 75 -> "😊"
                else -> "🤑"
            }

            val displayText = bitcoinData.fearGreedClassification?.let {
                "$emoji $it"
            } ?: "$emoji ${getFearGreedLabel(index)}"

            findViewById<TextView>(R.id.fear_greed_label)?.text = displayText
            
            // Update historical data
            bitcoinData.fearGreedYesterday?.let { yesterday ->
                findViewById<TextView>(R.id.fear_greed_yesterday)?.text = yesterday.toString()
            }
            bitcoinData.fearGreedLastWeek?.let { lastWeek ->
                findViewById<TextView>(R.id.fear_greed_last_week)?.text = lastWeek.toString()
            }
        }

        // Update timestamp
        findViewById<TextView>(R.id.last_updated)?.text = "Updated: ${bitcoinData.lastUpdated}"
    }

    private fun getMvrvZScoreStatus(zScore: Double): String {
        return when {
            zScore < -1.0 -> "🟢 Extremely Undervalued"
            zScore < 0.0 -> "🟢 Undervalued"
            zScore < 2.0 -> "🟡 Fair Value"
            zScore < 4.0 -> "🟠 Overvalued"
            zScore < 7.0 -> "🔴 Highly Overvalued"
            else -> "🔴 Extreme Bubble"
        }
    }

    private fun updateFearGreedGauge(currentValue: Int) {
        // Here you could dynamically update the gauge needle position
        // For now, we'll use the static gauge image
        val gaugeImage = findViewById<android.widget.ImageView>(R.id.fear_greed_gauge)
        gaugeImage?.setImageResource(R.drawable.fear_greed_gauge)
    }
    
    private fun displayFearGreedHistoricalData(currentValue: Int) {
        // Mock historical data - in real implementation, this would come from API
        val yesterday = generateMockHistoricalValue(currentValue, -1)
        val lastWeek = generateMockHistoricalValue(currentValue, -7)
        
        findViewById<TextView>(R.id.fear_greed_yesterday)?.text = yesterday.toString()
        findViewById<TextView>(R.id.fear_greed_last_week)?.text = lastWeek.toString()
    }
    
    private fun generateMockHistoricalValue(currentValue: Int, daysOffset: Int): Int {
        // Generate realistic historical values based on current value
        val variation = (-5..5).random()
        val historicalValue = currentValue + variation + (daysOffset * 0.5).toInt()
        return historicalValue.coerceIn(0, 100)
    }

    private fun getFearGreedLabel(index: Int): String {
        return when {
            index < 25 -> "Extreme Fear"
            index < 45 -> "Fear"
            index < 55 -> "Neutral"
            index < 75 -> "Greed"
            else -> "Extreme Greed"
        }
    }

    private fun displayErrorData() {
        findViewById<TextView>(R.id.btc_price_popup)?.text = "Error loading price"
        findViewById<TextView>(R.id.mvrv_value)?.text = "N/A"
        findViewById<TextView>(R.id.fee_low)?.text = "N/A"
        findViewById<TextView>(R.id.fee_medium)?.text = "N/A"
        findViewById<TextView>(R.id.fee_high)?.text = "N/A"
        findViewById<TextView>(R.id.fee_urgent)?.text = "N/A"
        findViewById<TextView>(R.id.fear_greed_value)?.text = "N/A"
        findViewById<TextView>(R.id.fear_greed_label)?.text = "Error"
        findViewById<TextView>(R.id.last_updated)?.text = "Failed to update"
    }
}