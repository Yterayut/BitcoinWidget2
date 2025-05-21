package com.example.bitcoinwidget

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class BitcoinPriceWidget : AppWidgetProvider() {
    companion object {
        private const val ACTION_UPDATE_WIDGET = "com.example.bitcoinwidget.ACTION_UPDATE_WIDGET"
        private const val PREFS_NAME = "WidgetPrefs"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "price_change_channel"

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_bitcoin_price)
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            Executors.newSingleThreadExecutor().execute {
                val currentPrice = fetchBitcoinPrice().toDoubleOrNull() ?: 0.0
                val currentTime = SimpleDateFormat("EEE HH:mm", Locale.getDefault()).format(Date())
                val lastPrice = prefs.getFloat("lastPrice_$appWidgetId", currentPrice.toFloat())
                val lastUpdateTime = prefs.getLong("lastUpdateTime_$appWidgetId", 0L)

                // เปรียบเทียบราคากับเมื่อ 30 นาทีที่แล้ว
                val priceColor = if (currentTime != SimpleDateFormat("EEE HH:mm", Locale.getDefault()).format(Date(lastUpdateTime + 30 * 60_000))) {
                    if (currentPrice > lastPrice) "#00FF00" else if (currentPrice < lastPrice) "#FF0000" else "#FFFFFF"
                } else "#FFFFFF"

                // อัปเดต UI
                views.setTextViewText(R.id.price_text, "$${currentPrice.toInt()}")
                views.setTextColor(R.id.price_text, android.graphics.Color.parseColor(priceColor))
                views.setTextViewText(R.id.time_text, "Binance @ $currentTime")
                views.setViewVisibility(R.id.time_text, View.VISIBLE)

                appWidgetManager.updateAppWidget(appWidgetId, views)

                // ตรวจสอบการเปลี่ยนแปลงราคาเกิน 1%
                val priceChangePercent = if (lastPrice != 0f) {
                    ((currentPrice - lastPrice) / lastPrice * 100)
                } else 0.0
                if (kotlin.math.abs(priceChangePercent) > 1) {
                    showNotification(context, priceChangePercent)
                }

                // บันทึกราคาและเวลาปัจจุบัน
                prefs.edit()
                    .putFloat("lastPrice_$appWidgetId", currentPrice.toFloat())
                    .putLong("lastUpdateTime_$appWidgetId", System.currentTimeMillis())
                    .apply()
            }
        }

        private fun fetchBitcoinPrice(): String {
            return try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT")
                    .build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    val price = JSONObject(json).getString("price")
                    price
                } else {
                    Log.e("BitcoinPriceWidget", "Failed to fetch price: ${response.code}")
                    "Error"
                }
            } catch (e: Exception) {
                Log.e("BitcoinPriceWidget", "Error fetching price: ${e.message}")
                "Error"
            }
        }

        internal fun setupNextUpdate(context: Context, appWidgetId: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val intervalMillis = prefs.getLong("interval_$appWidgetId", 30 * 60_000L) // Default 30 นาที

            val intent = Intent(context, BitcoinPriceWidget::class.java).apply {
                action = ACTION_UPDATE_WIDGET
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerAtMillis = System.currentTimeMillis() + intervalMillis
            Log.d("BitcoinPriceWidget", "Setting up next update for widget $appWidgetId at ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(triggerAtMillis))}")
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                intervalMillis,
                pendingIntent
            )
        }

        private fun cancelUpdate(context: Context, appWidgetId: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, BitcoinPriceWidget::class.java).apply {
                action = ACTION_UPDATE_WIDGET
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d("BitcoinPriceWidget", "Cancelled updates for widget $appWidgetId")
        }

        private fun showNotification(context: Context, priceChangePercent: Double) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    setShowBadge(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, RefreshIntervalActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val contentText = if (priceChangePercent > 0) {
                context.getString(R.string.price_increased, priceChangePercent)
            } else {
                context.getString(R.string.price_decreased, kotlin.math.abs(priceChangePercent))
            }

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("BTC Price Change")
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d("BitcoinPriceWidget", "onUpdate called with ${appWidgetIds.size} widgets")
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
            setupNextUpdate(context, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)

        val minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

        val views = RemoteViews(context.packageName, R.layout.widget_bitcoin_price)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentPrice = fetchBitcoinPrice().toDoubleOrNull() ?: 0.0
        val currentTime = SimpleDateFormat("EEE HH:mm", Locale.getDefault()).format(Date())
        val lastPrice = prefs.getFloat("lastPrice_$appWidgetId", currentPrice.toFloat())
        val lastUpdateTime = prefs.getLong("lastUpdateTime_$appWidgetId", 0L)

        val priceColor = if (currentTime != SimpleDateFormat("EEE HH:mm", Locale.getDefault()).format(Date(lastUpdateTime + 30 * 60_000))) {
            if (currentPrice > lastPrice) "#00FF00" else if (currentPrice < lastPrice) "#FF0000" else "#FFFFFF"
        } else "#FFFFFF"

        if (minWidth < 150 || minHeight < 70) {
            views.setTextViewText(R.id.price_text, "$${currentPrice.toInt()}")
            views.setTextColor(R.id.price_text, android.graphics.Color.parseColor(priceColor))
            views.setFloat(R.id.price_text, "setTextSize", 18f)
            views.setViewVisibility(R.id.time_text, View.GONE)
        } else {
            views.setTextViewText(R.id.price_text, "$${currentPrice.toInt()}")
            views.setTextColor(R.id.price_text, android.graphics.Color.parseColor(priceColor))
            views.setFloat(R.id.price_text, "setTextSize", 24f)
            views.setTextViewText(R.id.time_text, "Binance @ $currentTime")
            views.setViewVisibility(R.id.time_text, View.VISIBLE)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            cancelUpdate(context, appWidgetId)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove("interval_$appWidgetId")
                .remove("lastPrice_$appWidgetId")
                .remove("lastUpdateTime_$appWidgetId")
                .apply()
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                Log.d("BitcoinPriceWidget", "Received update action for widget $appWidgetId")
                val appWidgetManager = AppWidgetManager.getInstance(context)
                updateWidget(context, appWidgetManager, appWidgetId)
                setupNextUpdate(context, appWidgetId)
            }
        }
    }
}