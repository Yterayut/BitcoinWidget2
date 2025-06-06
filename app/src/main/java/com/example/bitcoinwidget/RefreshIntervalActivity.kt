package com.example.bitcoinwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup

class RefreshIntervalActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refresh_interval)

        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val radioGroup = findViewById<RadioGroup>(R.id.interval_radio_group)
        val confirmButton = findViewById<Button>(R.id.confirm_button)

        confirmButton.setOnClickListener {
            val selectedIntervalId = radioGroup.checkedRadioButtonId
            val intervalMillis = when (selectedIntervalId) {
                R.id.interval_1min -> 60_000L         // 1 นาที
                R.id.interval_2min -> 2 * 60_000L     // 2 นาที
                R.id.interval_5min -> 5 * 60_000L     // 5 นาที
                R.id.interval_10min -> 10 * 60_000L   // 10 นาที
                else -> 30 * 60_000L                  // 30 นาที (default)
            }

            // บันทึกค่า interval ลง SharedPreferences
            val prefs = getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
            prefs.edit().putLong("interval_$appWidgetId", intervalMillis).apply()

            // อัปเดต Widget ผ่าน Intent
            val appWidgetManager = AppWidgetManager.getInstance(this)
            val intent = Intent(this, BitcoinPriceWidget::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
            sendBroadcast(intent)

            // ส่งผลลัพธ์กลับ
            val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, result)
            finish()
        }
    }
}