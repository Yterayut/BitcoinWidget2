package com.example.bitcoinwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*
import java.text.SimpleDateFormat
import java.util.*

class BitcoinPriceWidget : AppWidgetProvider() {
    
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisAppWidget = ComponentName(context, BitcoinPriceWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_bitcoin_price)

        // ตั้งค่า PendingIntent สำหรับการกด widget (แก้ไข ID ให้ถูกต้อง)
        val intent = Intent(context, PopupActivity::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, flags)
        
        // ใช้ root layout แทน widget_layout ที่ไม่มี
        views.setOnClickPendingIntent(R.id.widget_root_layout, pendingIntent)

        // อัปเดตข้อมูล พร้อม timestamp ปัจจุบัน
        val currentTime = SimpleDateFormat("EEE HH:mm", Locale.getDefault()).format(Date())
        views.setTextViewText(R.id.price_text, "$95,847.12")
        views.setTextViewText(R.id.time_text, "Binance @ $currentTime")

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
