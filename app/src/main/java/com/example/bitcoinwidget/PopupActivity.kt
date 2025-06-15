package com.example.bitcoinwidget

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bitcoinwidget.ui.animations.CardAnimator
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class PopupActivity : Activity() {

    private val apiService = ApiService()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val cardAnimator = CardAnimator()
    private var isRefreshing = false
    private var allCards = mutableListOf<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("PopupActivity", "🔥 Enhanced PopupActivity with Animations onCreate() called")
        
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.popup_layout)
            
            Log.d("PopupActivity", "✅ Enhanced layout set successfully")

            // Collect all card views for animation
            collectCardViews()
            
            // Setup SwipeRefreshLayout if available
            setupSwipeRefresh()
            
            // Setup main app button
            setupMainAppButton()
            
            // Setup BTC price header click
            setupBtcPriceClick()
            
            // Setup card click listeners for explanations
            setupCardClickListeners()

            // Load and display data with entrance animations
            loadDataWithAnimations()

            // ปิด Activity เมื่อกดนอกพื้นที่
            setFinishOnTouchOutside(true)
            
            Log.d("PopupActivity", "🎉 Enhanced PopupActivity with animations setup complete")
            
        } catch (e: Exception) {
            Log.e("PopupActivity", "❌ Error in enhanced onCreate: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun collectCardViews() {
        // Collect all card views for coordinated animations
        allCards.clear()
        
        // Add cards in display order for staggered animation
        findViewById<View>(R.id.btc_price_header)?.let { allCards.add(it) }
        
        // Find cards by their child elements (since we don't have direct card IDs)
        findCardByChildId(R.id.market_cap)?.let { allCards.add(it) }
        findCardByChildId(R.id.block_height)?.let { allCards.add(it) }
        findCardByChildId(R.id.fee_low)?.let { allCards.add(it) }
        findCardByChildId(R.id.fear_greed_value)?.let { allCards.add(it) }
        findCardByChildId(R.id.mvrv_score)?.let { allCards.add(it) }
        
        Log.d("PopupActivity", "📋 Collected ${allCards.size} cards for animation")
    }
    
    private fun findCardByChildId(childId: Int): View? {
        val child = findViewById<View>(childId)
        var parent = child?.parent
        while (parent != null) {
            if (parent is CardView) {
                return parent as View
            }
            parent = parent.parent
        }
        return null
    }
    
    private fun loadDataWithAnimations() {
        // Start entrance animations
        runOnUiThread {
            cardAnimator.animateCardsSequential(allCards)
        }
        
        // Load actual data
        loadEnhancedData()
        
        Log.d("PopupActivity", "🚀 Loading data with entrance animations")
    }
    
    private fun updatePriceWithAnimation(textView: TextView, newPrice: String, isIncrease: Boolean? = null) {
        cardAnimator.animatePriceUpdate(textView, newPrice, isIncrease)
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
    
    private fun setupCardClickListeners() {
        try {
            Log.d("PopupActivity", "🎯 Setting up card click listeners for explanations")
            
            // เนื่องจากเราไม่มี ID เฉพาะสำหรับ card แต่ละตัว เราจะใช้ทางอ้อม
            // Market Overview Card - หา LinearLayout ที่มี market_cap เป็นลูก
            setupMarketOverviewCardClick()
            
            // Network Data Card - หา LinearLayout ที่มี block_height เป็นลูก
            setupNetworkDataCardClick()
            
            // Network Fees Card - หา LinearLayout ที่มี fee_low เป็นลูก
            setupNetworkFeesCardClick()
            
            // Fear & Greed Card - หา LinearLayout ที่มี fear_greed_value เป็นลูก
            setupFearGreedCardClick()
            
            // MVRV Card - หา LinearLayout ที่มี mvrv_score เป็นลูก
            setupMvrvCardClick()
            
        } catch (e: Exception) {
            Log.e("PopupActivity", "❌ Error setting up card click listeners: ${e.message}")
        }
    }
    
    private fun setupMarketOverviewCardClick() {
        try {
            // หา parent card ของ market_cap
            val marketCapView = findViewById<TextView>(R.id.market_cap)
            val cardView = findParentCard(marketCapView)
            cardView?.setOnClickListener {
                Log.d("PopupActivity", "🎯 Market Overview card clicked")
                ExplanationActivity.start(this, ExplanationActivity.CARD_MARKET_OVERVIEW)
            }
        } catch (e: Exception) {
            Log.e("PopupActivity", "❌ Error setting up market overview click: ${e.message}")
        }
    }
    
    private fun setupNetworkDataCardClick() {
        try {
            val blockHeightView = findViewById<TextView>(R.id.block_height)
            val cardView = findParentCard(blockHeightView)
            cardView?.setOnClickListener {
                Log.d("PopupActivity", "🎯 Network Data card clicked")
                ExplanationActivity.start(this, ExplanationActivity.CARD_NETWORK_DATA)
            }
        } catch (e: Exception) {
            Log.e("PopupActivity", "❌ Error setting up network data click: ${e.message}")
        }
    }
    
    private fun setupNetworkFeesCardClick() {
        try {
            val feeLowView = findViewById<TextView>(R.id.fee_low)
            val cardView = findParentCard(feeLowView)
            cardView?.setOnClickListener {
                Log.d("PopupActivity", "🎯 Network Fees card clicked")
                ExplanationActivity.start(this, ExplanationActivity.CARD_NETWORK_FEES)
            }
        } catch (e: Exception) {
            Log.e("PopupActivity", "❌ Error setting up network fees click: ${e.message}")
        }
    }
    
    private fun setupFearGreedCardClick() {
        try {
            val fearGreedView = findViewById<TextView>(R.id.fear_greed_value)
            val cardView = findParentCard(fearGreedView)
            cardView?.setOnClickListener {
                Log.d("PopupActivity", "🎯 Fear & Greed card clicked")
                ExplanationActivity.start(this, ExplanationActivity.CARD_FEAR_GREED)
            }
        } catch (e: Exception) {
            Log.e("PopupActivity", "❌ Error setting up fear greed click: ${e.message}")
        }
    }
    
    private fun setupMvrvCardClick() {
        try {
            val mvrvView = findViewById<TextView>(R.id.mvrv_score)
            val cardView = findParentCard(mvrvView)
            cardView?.setOnClickListener {
                Log.d("PopupActivity", "🎯 MVRV card clicked")
                ExplanationActivity.start(this, ExplanationActivity.CARD_MVRV)
            }
        } catch (e: Exception) {
            Log.e("PopupActivity", "❌ Error setting up MVRV click: ${e.message}")
        }
    }
    
    private fun findParentCard(childView: android.view.View?): android.view.View? {
        if (childView == null) return null
        
        var currentParent: android.view.ViewParent? = childView.parent
        // หา LinearLayout ที่มี background เป็น modern_card_bg (Card Container)
        while (currentParent != null && currentParent is android.view.View) {
            val view = currentParent as android.view.View
            if (view is LinearLayout) {
                // ตรวจสอบว่าเป็น card หรือไม่จาก background
                try {
                    val background = view.background
                    if (background != null) {
                        // เป็น card ที่เราต้องการ
                        return view
                    }
                } catch (e: Exception) {
                    // ไม่สำคัญ ให้ลองต่อ
                }
            }
            currentParent = (currentParent as android.view.View).parent
        }
        return null
    }

    private fun setupSwipeRefresh() {
        try {
            swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
            swipeRefreshLayout.setOnRefreshListener {
                Log.d("PopupActivity", "🔄 Pull-to-refresh triggered with animation")
                refreshDataWithAnimation()
            }
            
            // Set Bitcoin-themed colors
            swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_orange_light,
                android.R.color.holo_orange_dark,
                android.R.color.white
            )
        } catch (e: Exception) {
            Log.w("PopupActivity", "⚠️ SwipeRefreshLayout not found in layout, continuing without pull-to-refresh")
        }
    }
    
    private fun refreshDataWithAnimation() {
        if (isRefreshing) {
            Log.d("PopupActivity", "⚠️ Already refreshing, skipping duplicate request")
            return
        }
        
        isRefreshing = true
        
        // Animate all cards during refresh
        allCards.forEach { card ->
            cardAnimator.animateLoadingShimmer(card)
        }
        
        // Load data with animation completion
        CoroutineScope(Dispatchers.Main).launch {
            try {
                loadEnhancedData(forceRefresh = true)
                
                // Stop loading animations and show success
                allCards.forEach { card ->
                    cardAnimator.stopLoadingShimmer(card)
                    cardAnimator.animateSuccess(card)
                }
                
                Log.d("PopupActivity", "✅ Animated refresh complete")
                
            } catch (e: Exception) {
                Log.e("PopupActivity", "❌ Error during animated refresh: ${e.message}")
                
                // Show error state
                allCards.forEach { card ->
                    cardAnimator.stopLoadingShimmer(card)
                    cardAnimator.animateErrorShake(card)
                }
            } finally {
                swipeRefreshLayout.isRefreshing = false
                isRefreshing = false
            }
        }
    }
    
    private fun refreshData() {
        refreshDataWithAnimation()
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
            findViewById<TextView>(R.id.mvrv_score)?.text = String.format("%.2f", it)
            findViewById<TextView>(R.id.mvrv_status)?.text = getMvrvStatusWithIcon(it.toDouble())
        }

        // Display cached fees with proper sat/vB unit
        fastestFee?.let {
            findViewById<TextView>(R.id.fee_urgent)?.text = "$it sat/vB"
            findViewById<TextView>(R.id.fee_high)?.text = "$it sat/vB"
        }

        halfHourFee?.let {
            findViewById<TextView>(R.id.fee_medium)?.text = "$it sat/vB"
        }

        hourFee?.let {
            findViewById<TextView>(R.id.fee_low)?.text = "$it sat/vB"
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
            findViewById<TextView>(R.id.mvrv_score)?.text = String.format("%.2f", zScore)
            findViewById<TextView>(R.id.mvrv_status)?.text = getMvrvStatusWithIcon(zScore)
        }

        // Update Mining Fees with better labeling to match mempool.space
        bitcoinData.hourFee?.let { fee ->
            // Low priority / Economy fee (~1 hour)
            findViewById<TextView>(R.id.fee_low)?.text = "$fee sat/vB"
        }

        bitcoinData.halfHourFee?.let { fee ->
            // Standard fee (~30 minutes)
            findViewById<TextView>(R.id.fee_medium)?.text = "$fee sat/vB"
        }

        bitcoinData.fastestFee?.let { fee ->
            // High priority / Fast fee (next block)
            findViewById<TextView>(R.id.fee_high)?.text = "$fee sat/vB"
            // Also update urgent (same as fastest for now)
            findViewById<TextView>(R.id.fee_urgent)?.text = "$fee sat/vB"
        }

        Log.d("PopupActivity", "💳 Updated fees: Low=${bitcoinData.hourFee}, Medium=${bitcoinData.halfHourFee}, Fast=${bitcoinData.fastestFee}")

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
            zScore < -0.5 -> "🟢 Undervalued"
            zScore < 2.4 -> "🟡 Fair Value"
            zScore < 7.0 -> "🔴 Overvalued"
            else -> "🔴 Extreme Bubble"
        }
    }
    
    private fun getMvrvStatusWithIcon(zScore: Double): String {
        return when {
            zScore < -0.5 -> "🟢 Undervalued"
            zScore < 2.4 -> "🟡 Fair Value"
            zScore < 7.0 -> "🔴 Overvalued"
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
        findViewById<TextView>(R.id.mvrv_score)?.text = "N/A"
        findViewById<TextView>(R.id.mvrv_status)?.text = "🔴 Error"
        findViewById<TextView>(R.id.fee_low)?.text = "N/A"
        findViewById<TextView>(R.id.fee_medium)?.text = "N/A"
        findViewById<TextView>(R.id.fee_high)?.text = "N/A"
        findViewById<TextView>(R.id.fee_urgent)?.text = "N/A"
        findViewById<TextView>(R.id.fear_greed_value)?.text = "N/A"
        findViewById<TextView>(R.id.fear_greed_label)?.text = "Error"
        findViewById<TextView>(R.id.last_updated)?.text = "Failed to update"
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up any running animations
        allCards.forEach { card ->
            cardAnimator.stopLoadingShimmer(card)
        }
        Log.d("PopupActivity", "🧹 Cleaned up animations on destroy")
    }
}