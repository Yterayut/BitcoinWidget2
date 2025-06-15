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
            val userDefinedInterval = prefs.getLong("interval_$appWidgetId", 5 * 60_000L)
            
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

            // แสดง Loading state ก่อน
            views.setTextViewText(R.id.price_text, "Loading...")
            views.setTextViewText(R.id.time_text, "Fetching data...")
            views.setTextColor(R.id.price_text, android.graphics.Color.parseColor("#FFFFFF"))
            appWidgetManager.updateAppWidget(appWidgetId, views)

            // Initialize performance manager
            val performanceManager = PerformanceManager(context)
            
            // Check if we should skip this update for performance reasons
            if (performanceManager.shouldSkipUpdate(appWidgetId)) {
                Log.d(TAG, "⏭️ Skipping update for widget $appWidgetId due to performance conditions")
                val optimizedInterval = performanceManager.getOptimizedInterval(userDefinedInterval, appWidgetId)
                scheduleNextUpdate(context, appWidgetId, optimizedInterval)
                return
            }

            // Fetch data from APIs using coroutines
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                val startTime = System.currentTimeMillis()
                val batteryBefore = performanceManager.getBatteryLevel()
                
                try {
                    val bitcoinData = apiService.fetchBitcoinData()
                    val apiCallDuration = System.currentTimeMillis() - startTime
                    val batteryAfter = performanceManager.getBatteryLevel()
                    
                    performanceManager.logPerformanceMetrics(appWidgetId, apiCallDuration, batteryBefore, batteryAfter)

                    withContext(Dispatchers.Main) {
                        if (bitcoinData.price != null) {
                            val formattedPrice = String.format("$%,.0f", bitcoinData.price)
                            views.setTextViewText(R.id.price_text, formattedPrice)
                            
                            val priceColor = when {
                                bitcoinData.priceChangePercent24h != null && bitcoinData.priceChangePercent24h > 0 -> 
                                    android.graphics.Color.parseColor("#4CAF50")
                                bitcoinData.priceChangePercent24h != null && bitcoinData.priceChangePercent24h < 0 -> 
                                    android.graphics.Color.parseColor("#F44336")
                                else -> android.graphics.Color.parseColor("#FFFFFF")
                            }
                            views.setTextColor(R.id.price_text, priceColor)
                            
                            prefs.edit().putFloat("latest_price_$appWidgetId", bitcoinData.price.toFloat()).apply()
                            bitcoinData.priceChangePercent24h?.let {
                                prefs.edit().putFloat("price_change_24h_$appWidgetId", it.toFloat()).apply()
                            }
                        }

                        // Update time with power saving status
                        val currentTime = java.text.SimpleDateFormat("EEE HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                        val powerStatus = performanceManager.getPowerSavingStatus()
                        val isOptimized = !powerStatus.contains("Normal")
                        
                        val timeText = if (isOptimized) {
                            "⚡ $currentTime (Power Saving)"
                        } else {
                            "Updated @ $currentTime"
                        }
                        views.setTextViewText(R.id.time_text, timeText)

                        // Save additional data for popup
                        bitcoinData.fastestFee?.let { prefs.edit().putInt("fastest_fee_$appWidgetId", it).apply() }
                        bitcoinData.halfHourFee?.let { prefs.edit().putInt("half_hour_fee_$appWidgetId", it).apply() }
                        bitcoinData.hourFee?.let { prefs.edit().putInt("hour_fee_$appWidgetId", it).apply() }
                        bitcoinData.mvrvZScore?.let { prefs.edit().putFloat("mvrv_z_score_$appWidgetId", it.toFloat()).apply() }
                        bitcoinData.fearGreedIndex?.let { prefs.edit().putInt("fear_greed_index_$appWidgetId", it).apply() }
                        bitcoinData.fearGreedClassification?.let { prefs.edit().putString("fear_greed_classification_$appWidgetId", it).apply() }

                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error updating widget $appWidgetId: ${e.message}", e)
                    
                    withContext(Dispatchers.Main) {
                        val cachedPrice = prefs.getFloat("latest_price_$appWidgetId", -1f)
                        if (cachedPrice > 0) {
                            val formattedPrice = String.format("$%,.0f", cachedPrice)
                            views.setTextViewText(R.id.price_text, "$formattedPrice (cached)")
                            views.setTextViewText(R.id.time_text, "Tap to retry")
                            
                            val cached24hChange = prefs.getFloat("price_change_24h_$appWidgetId", 0f)
                            val priceColor = when {
                                cached24hChange > 0 -> android.graphics.Color.parseColor("#4CAF50")
                                cached24hChange < 0 -> android.graphics.Color.parseColor("#F44336")
                                else -> android.graphics.Color.parseColor("#FFA726")
                            }
                            views.setTextColor(R.id.price_text, priceColor)
                        } else {
                            views.setTextViewText(R.id.price_text, "Error")
                            views.setTextViewText(R.id.time_text, "Tap to retry")
                            views.setTextColor(R.id.price_text, android.graphics.Color.parseColor("#FFFFFF"))
                        }
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
                
                // Schedule next update with optimized interval
                val optimizedInterval = try {
                    performanceManager.getOptimizedInterval(userDefinedInterval, appWidgetId)
                } catch (e: Exception) {
                    Log.w(TAG, "Performance manager error, using user interval: ${e.message}")
                    userDefinedInterval
                }
                
                scheduleNextUpdate(context, appWidgetId, optimizedInterval)
                
                Log.d(TAG, "⏰ Next update scheduled for widget $appWidgetId:")
                Log.d(TAG, "   User setting: ${userDefinedInterval/1000/60} minutes")
                Log.d(TAG, "   Optimized to: ${optimizedInterval/1000/60} minutes")
                if (optimizedInterval > userDefinedInterval) {
                    val savings = ((optimizedInterval.toDouble() / userDefinedInterval.toDouble()) - 1.0) * 100
                    Log.d(TAG, "   Power saving: ${savings.toInt()}% longer interval")
                }
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
                alarmManager.set(AlarmManager.ELAPSED_REALTIME, triggerTime, pendingIntent)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d(TAG, "onUpdate called for ${appWidgetIds.size} widgets")
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "🎯 Widget enabled - triggering immediate update for ALL widgets")
        
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

            val prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
            with(prefs.edit()) {
                remove("interval_$appWidgetId")
                remove("latest_price_$appWidgetId")
                remove("fastest_fee_$appWidgetId")
                remove("half_hour_fee_$appWidgetId")
                remove("hour_fee_$appWidgetId")
                remove("mvrv_z_score_$appWidgetId")
                remove("fear_greed_index_$appWidgetId")
                remove("fear_greed_classification_$appWidgetId")
                apply()
            }
        }
        super.onDeleted(context, appWidgetIds)
    }
}
