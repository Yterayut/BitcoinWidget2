package com.example.bitcoinwidget.customization

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Widget Customization Manager
 * Handles theme options, widget sizes, data selection, and update intervals
 */
class WidgetCustomizationManager(private val context: Context) {
    
    companion object {
        private const val TAG = "WidgetCustomization"
        private const val PREFS_NAME = "WidgetCustomization"
        
        // Theme options
        const val THEME_DARK = "dark"
        const val THEME_LIGHT = "light"
        const val THEME_AUTO = "auto"
        const val THEME_BITCOIN_ORANGE = "bitcoin_orange"
        const val THEME_MINIMAL = "minimal"
        
        // Widget sizes
        const val SIZE_SMALL = "small"
        const val SIZE_MEDIUM = "medium"
        const val SIZE_LARGE = "large"
        
        // Data elements
        const val DATA_PRICE = "price"
        const val DATA_CHANGE_24H = "change_24h"
        const val DATA_MARKET_CAP = "market_cap"
        const val DATA_DOMINANCE = "dominance"
        const val DATA_FEES = "fees"
        const val DATA_FEAR_GREED = "fear_greed"
        const val DATA_MVRV = "mvrv"
        const val DATA_BLOCK_HEIGHT = "block_height"
        const val DATA_HALVING = "halving"
        const val DATA_MINING_COST = "mining_cost"
        
        // Update intervals (in milliseconds)
        const val INTERVAL_1_MIN = 60_000L
        const val INTERVAL_2_MIN = 120_000L
        const val INTERVAL_5_MIN = 300_000L
        const val INTERVAL_10_MIN = 600_000L
        const val INTERVAL_15_MIN = 900_000L
        const val INTERVAL_30_MIN = 1_800_000L
        const val INTERVAL_1_HOUR = 3_600_000L
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Add basic theme configuration
    fun getDefaultTheme(): String = THEME_DARK
}
