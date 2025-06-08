package com.example.bitcoinwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*

class RefreshIntervalActivity : Activity() {

    private var selectedAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refresh_interval)

        // Get widget ID from intent, or allow selection if called from MainActivity
        selectedAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

        setupUI()
    }

    private fun setupUI() {
        val radioGroup = findViewById<RadioGroup>(R.id.interval_radio_group)
        val confirmButton = findViewById<Button>(R.id.confirm_button)
        val cancelButton = findViewById<Button>(R.id.cancel_button)
        val widgetSpinner = findViewById<Spinner>(R.id.widget_selector_spinner)
        val currentIntervalText = findViewById<TextView>(R.id.current_interval_text)

        // Setup widget selector if no specific widget ID provided
        if (selectedAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            setupWidgetSelector(widgetSpinner, currentIntervalText)
        } else {
            // Hide spinner if specific widget ID provided
            widgetSpinner.visibility = View.GONE
            findViewById<TextView>(R.id.widget_selector_label)?.visibility = View.GONE
            showCurrentInterval(selectedAppWidgetId, currentIntervalText)
        }

        // Set default selection to 5 minutes
        radioGroup.check(R.id.interval_5min)

        cancelButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        confirmButton.setOnClickListener {
            val targetWidgetId = if (selectedAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                selectedAppWidgetId
            } else {
                val selectedPosition = widgetSpinner.selectedItemPosition
                if (selectedPosition >= 0) {
                    val widgetIds = getAvailableWidgetIds()
                    if (selectedPosition < widgetIds.size) {
                        widgetIds[selectedPosition]
                    } else {
                        showToast("Please select a widget")
                        return@setOnClickListener
                    }
                } else {
                    showToast("Please select a widget")
                    return@setOnClickListener
                }
            }

            applyIntervalSettings(targetWidgetId, radioGroup)
        }
    }

    private fun setupWidgetSelector(spinner: Spinner, currentIntervalText: TextView) {
        val widgetIds = getAvailableWidgetIds()

        if (widgetIds.isEmpty()) {
            showToast("No widgets found. Please add a Bitcoin widget to your home screen first.")
            finish()
            return
        }

        val widgetNames = widgetIds.mapIndexed { index, id ->
            "Bitcoin Widget #${index + 1} (ID: $id)"
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, widgetNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < widgetIds.size) {
                    showCurrentInterval(widgetIds[position], currentIntervalText)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Show current interval for first widget
        if (widgetIds.isNotEmpty()) {
            showCurrentInterval(widgetIds[0], currentIntervalText)
        }
    }

    private fun getAvailableWidgetIds(): List<Int> {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val thisWidget = ComponentName(this, BitcoinPriceWidget::class.java)
        return appWidgetManager.getAppWidgetIds(thisWidget).toList()
    }

    private fun showCurrentInterval(appWidgetId: Int, textView: TextView) {
        val prefs = getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
        val intervalMillis = prefs.getLong("interval_$appWidgetId", 5 * 60_000L)

        val intervalText = when (intervalMillis) {
            60_000L -> "1 minute"
            5 * 60_000L -> "5 minutes"
            10 * 60_000L -> "10 minutes"
            20 * 60_000L -> "20 minutes"
            30 * 60_000L -> "30 minutes"
            60 * 60_000L -> "1 hour"
            else -> "${intervalMillis / 60_000} minutes"
        }

        textView.text = "Current interval: $intervalText"
    }

    private fun applyIntervalSettings(appWidgetId: Int, radioGroup: RadioGroup) {
        val selectedIntervalId = radioGroup.checkedRadioButtonId
        val intervalMillis = when (selectedIntervalId) {
            R.id.interval_1min -> 60_000L           // 1 minute
            R.id.interval_5min -> 5 * 60_000L       // 5 minutes
            R.id.interval_10min -> 10 * 60_000L     // 10 minutes
            R.id.interval_20min -> 20 * 60_000L     // 20 minutes
            R.id.interval_30min -> 30 * 60_000L     // 30 minutes
            R.id.interval_1hour -> 60 * 60_000L     // 1 hour
            else -> 5 * 60_000L                     // 5 minutes (default)
        }

        // บันทึกค่า interval ลง SharedPreferences
        val prefs = getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("interval_$appWidgetId", intervalMillis).apply()

        // อัปเดต Widget ทันทีด้วย interval ใหม่
        val appWidgetManager = AppWidgetManager.getInstance(this)
        BitcoinPriceWidget.updateWidget(this, appWidgetManager, appWidgetId)

        val intervalText = when (intervalMillis) {
            60_000L -> "1 minute"
            5 * 60_000L -> "5 minutes"
            10 * 60_000L -> "10 minutes"
            20 * 60_000L -> "20 minutes"
            30 * 60_000L -> "30 minutes"
            60 * 60_000L -> "1 hour"
            else -> "${intervalMillis / 60_000} minutes"
        }

        showToast("✅ Refresh interval set to $intervalText for Widget ID: $appWidgetId")
        Log.d("RefreshIntervalActivity", "Interval set to $intervalMillis ms for widget $appWidgetId")

        // ส่งผลลัพธ์กลับ
        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, result)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}