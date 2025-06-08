package com.example.bitcoinwidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import com.example.bitcoinwidget.performance.PerformanceManager
import kotlinx.coroutines.*

class BitcoinPriceWidget : AppWidgetProvider() {

    companion object {
        private const val ACTION_UPDATE_WIDGET = "com.example.bitcoinwidget.UPDATE_WIDGET"
        private const val TAG = "BitcoinPriceWidget"
        private val apiService = ApiService()

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            Log.d(TAG, "🚀 Starting widget update for widget $appWidgetId")

            val prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
            val defaultInterval = prefs.getLong("interval_$appWidgetId", 5 * 60_000L) // Default 5 minutes
            
            val views = RemoteViews(context.packageName, R.layout.widget_bitcoin_price)

            // ตั้งค่า PendingIntent สำหรับการกด widget
            val intent = Intent(context, PopupActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, flags)
            views.setOnClickPendingIntent(R.id.widget_root_layout, pendingIntent)
            
            Log.d(TAG, "🔄 PendingIntent set for widget $appWidgetId")

            // แสดง Loading state ก่อน
            views.setTextViewText(R.id.price_text, "Loading...")
            views.setTextViewText(R.id.time_text, "Fetching data...")
            appWidgetManager.updateAppWidget(appWidgetId, views)
            Log.d(TAG, "📱 Widget $appWidgetId set to loading state")

            // Fetch data from APIs using coroutines
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                val startTime = System.currentTimeMillis()
                Log.d(TAG, "🌐 Starting API calls for widget $appWidgetId")
                
                try {
                    // Force fetch fresh data
                    val bitcoinData = apiService.fetchBitcoinData()
                    val apiCallDuration = System.currentTimeMillis() - startTime
                    Log.d(TAG, "✅ API call completed in ${apiCallDuration}ms for widget $appWidgetId")
                    Log.d(TAG, "📊 Bitcoin data received: price=${bitcoinData.price}, change=${bitcoinData.priceChangePercent24h}")

                    // Switch back to Main dispatcher to update UI
                    withContext(Dispatchers.Main) {
                        // Update widget with real data - be more lenient with validation
                        if (bitcoinData.price != null) {
                            try {
                                val formattedPrice = String.format("$%,.0f", bitcoinData.price)
                                views.setTextViewText(R.id.price_text, formattedPrice)
                                Log.d(TAG, "💰 Widget $appWidgetId updated with price: $formattedPrice")

                                // Save price for popup
                                prefs.edit().putFloat("latest_price_$appWidgetId", bitcoinData.price.toFloat()).apply()
                            } catch (e: Exception) {
                                Log.e(TAG, "❌ Error formatting price: ${e.message}")
                                views.setTextViewText(R.id.price_text, "$ ${bitcoinData.price}")
                            }
                        } else {
                            views.setTextViewText(R.id.price_text, "Loading...")
                            Log.w(TAG, "⚠️ Widget $appWidgetId: No price data available, will retry")
                        }

                        // Update time with actual timestamp
                        val currentTime = java.text.SimpleDateFormat("EEE HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                        views.setTextViewText(R.id.time_text, "Updated @ $currentTime")

                        // Save additional data for popup
                        bitcoinData.fastestFee?.let {
                            prefs.edit().putInt("fastest_fee_$appWidgetId", it).apply()
                        }
                        bitcoinData.halfHourFee?.let {
                            prefs.edit().putInt("half_hour_fee_$appWidgetId", it).apply()
                        }
                        bitcoinData.hourFee?.let {
                            prefs.edit().putInt("hour_fee_$appWidgetId", it).apply()
                        }
                        bitcoinData.mvrvZScore?.let {
                            prefs.edit().putFloat("mvrv_z_score_$appWidgetId", it.toFloat()).apply()
                        }
                        bitcoinData.fearGreedIndex?.let {
                            prefs.edit().putInt("fear_greed_index_$appWidgetId", it).apply()
                        }
                        bitcoinData.fearGreedClassification?.let {
                            prefs.edit().putString("fear_greed_classification_$appWidgetId", it).apply()
                        }

                        // Apply final widget update
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                        Log.d(TAG, "🎯 Widget $appWidgetId final update completed successfully")
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error updating widget $appWidgetId: ${e.message}", e)
                    
                    // Update widget with error state on Main dispatcher
                    withContext(Dispatchers.Main) {
                        // Try to get cached price first
                        val cachedPrice = prefs.getFloat("latest_price_$appWidgetId", -1f)
                        if (cachedPrice > 0) {
                            val formattedPrice = String.format("$%,.0f", cachedPrice)
                            views.setTextViewText(R.id.price_text, "$formattedPrice (cached)")
                            views.setTextViewText(R.id.time_text, "Tap to retry")
                            Log.d(TAG, "📱 Widget $appWidgetId showing cached price: $formattedPrice")
                        } else {
                            views.setTextViewText(R.id.price_text, "Error")
                            views.setTextViewText(R.id.time_text, "Tap to retry")
                            Log.e(TAG, "❌ Widget $appWidgetId: No cached data available")
                        }
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
                
                // Get optimized interval for next update
                val performanceManager = PerformanceManager(context)
                val optimizedInterval = try {
                    performanceManager.getOptimizedInterval(defaultInterval, appWidgetId)
                } catch (e: Exception) {
                    Log.w(TAG, "Performance manager error, using default interval: ${e.message}")
                    defaultInterval
                }
                
                // Schedule next update
                scheduleNextUpdate(context, appWidgetId, optimizedInterval)
                Log.d(TAG, "⏰ Next update scheduled for widget $appWidgetId in ${optimizedInterval/1000/60} minutes")
            }
        }

        private fun scheduleNextUpdate(context: Context, appWidgetId: Int, intervalMillis: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, BitcoinPriceWidget::class.java).apply {
                action = ACTION_UPDATE_WIDGET
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }

            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, flags)

            val triggerTime = SystemClock.elapsedRealtime() + intervalMillis

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME, triggerTime, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME, triggerTime, pendingIntent)
                }
                Log.d(TAG, "Next update scheduled for widget $appWidgetId in ${intervalMillis / 1000} seconds")
            } catch (e: SecurityException) {
                Log.e(TAG, "Failed to schedule exact alarm: ${e.message}")
                // Fallback to inexact alarm
                alarmManager.set(AlarmManager.ELAPSED_REALTIME, triggerTime, pendingIntent)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d(TAG, "onUpdate called for ${appWidgetIds.size} widgets")
        for (appWidgetId in appWidgetIds) {
            Log.d(TAG, "Force updating widget $appWidgetId")
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "🎯 Widget enabled - triggering immediate update for ALL widgets")
        
        // Force immediate update for all widgets
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisWidget = ComponentName(context, BitcoinPriceWidget::class.java)
        val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
        
        Log.d(TAG, "📱 Found ${allWidgetIds.size} widgets to update")
        for (appWidgetId in allWidgetIds) {
            Log.d(TAG, "🔄 Force updating widget $appWidgetId immediately")
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    /**
     * Force update widget immediately (bypasses performance checks)
     */
    fun forceUpdateWidget(context: Context, appWidgetId: Int) {
        Log.d(TAG, "⚡ FORCE UPDATE requested for widget $appWidgetId")
        val appWidgetManager = AppWidgetManager.getInstance(context)
        updateWidget(context, appWidgetManager, appWidgetId)
    }

    /**
     * Update all widgets immediately
     */
    fun updateAllWidgets(context: Context) {
        Log.d(TAG, "🔄 UPDATE ALL WIDGETS requested")
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisWidget = ComponentName(context, BitcoinPriceWidget::class.java)
        val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
        
        for (appWidgetId in allWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_UPDATE_WIDGET -> {
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    updateWidget(context, appWidgetManager, appWidgetId)
                }
            }
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisAppWidget = ComponentName(context, BitcoinPriceWidget::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
                onUpdate(context, appWidgetManager, appWidgetIds)
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        Log.d(TAG, "onDeleted called for ${appWidgetIds.size} widgets")
        // ยกเลิก alarms เมื่อ widget ถูกลบ
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (appWidgetId in appWidgetIds) {
            val intent = Intent(context, BitcoinPriceWidget::class.java).apply {
                action = ACTION_UPDATE_WIDGET
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, flags)
            alarmManager.cancel(pendingIntent)

            // ลบ preferences (updated with new fields)
            val prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
            with(prefs.edit()) {
                remove("interval_$appWidgetId")
                remove("latest_price_$appWidgetId")
                remove("fastest_fee_$appWidgetId")
                remove("half_hour_fee_$appWidgetId")
                remove("hour_fee_$appWidgetId")
                remove("mvrv_z_score_$appWidgetId")  // Updated key name
                remove("fear_greed_index_$appWidgetId")  // New
                remove("fear_greed_classification_$appWidgetId")  // New
                apply()
            }
        }
        super.onDeleted(context, appWidgetIds)
    }
}