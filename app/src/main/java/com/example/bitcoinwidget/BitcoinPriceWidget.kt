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
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*

class BitcoinPriceWidget : AppWidgetProvider() {
    
    companion object {
        private const val ACTION_UPDATE_WIDGET = "com.example.bitcoinwidget.UPDATE_WIDGET"
        
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
            val intervalMillis = prefs.getLong("interval_$appWidgetId", 5 * 60_000L) // Default 5 minutes
            
            val views = RemoteViews(context.packageName, R.layout.widget_bitcoin_price)

            // ตั้งค่า PendingIntent สำหรับการกด widget
            val intent = Intent(context, PopupActivity::class.java)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, flags)
            views.setOnClickPendingIntent(R.id.widget_root_layout, pendingIntent)

            // อัปเดตข้อมูล พร้อม timestamp ปัจจุบัน
            val currentTime = SimpleDateFormat("EEE HH:mm", Locale.getDefault()).format(Date())
            
            // Simulate different prices based on time for demo
            val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
            val basePrice = 95000 + (currentMinute * 100) // Price varies by minute
            val formattedPrice = String.format("$%,d", basePrice)
            
            views.setTextViewText(R.id.price_text, formattedPrice)
            views.setTextViewText(R.id.time_text, "Binance @ $currentTime")

            appWidgetManager.updateAppWidget(appWidgetId, views)
            
            // ตั้งค่า alarm สำหรับการอัปเดตครั้งต่อไป
            scheduleNextUpdate(context, appWidgetId, intervalMillis)
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
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME, triggerTime, pendingIntent)
            }
        }
    }
    
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
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
            
            // ลบ preferences
            val prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
            prefs.edit().remove("interval_$appWidgetId").apply()
        }
        super.onDeleted(context, appWidgetIds)
    }
}
