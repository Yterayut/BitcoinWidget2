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
import com.example.bitcoinwidget.performance.PerformanceManager

class RefreshIntervalActivity : Activity() {

    private var selectedAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var performanceManager: PerformanceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refresh_interval)

        // Initialize performance manager
        performanceManager = PerformanceManager(this)

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
        val powerStatusText = findViewById<TextView>(R.id.power_status_text) // New view for power status

        // Setup widget selector if no specific widget ID provided
        if (selectedAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            setupWidgetSelector(widgetSpinner, currentIntervalText, powerStatusText)
        } else {
            // Hide spinner if specific widget ID provided
            widgetSpinner.visibility = View.GONE
            findViewById<TextView>(R.id.widget_selector_label)?.visibility = View.GONE
            showCurrentInterval(selectedAppWidgetId, currentIntervalText, powerStatusText)
        }

        // Set default selection to 5 minutes
        radioGroup.check(R.id.interval_5min)

        // Add power status listener for radio group changes
        radioGroup.setOnCheckedChangeListener { _, _ ->
            updatePowerSavingPreview(powerStatusText)
        }

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

    private fun setupWidgetSelector(spinner: Spinner, currentIntervalText: TextView, powerStatusText: TextView) {
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
                    showCurrentInterval(widgetIds[position], currentIntervalText, powerStatusText)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Show current interval for first widget
        if (widgetIds.isNotEmpty()) {
            showCurrentInterval(widgetIds[0], currentIntervalText, powerStatusText)
        }
    }

    private fun getAvailableWidgetIds(): List<Int> {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val thisWidget = ComponentName(this, BitcoinPriceWidget::class.java)
        return appWidgetManager.getAppWidgetIds(thisWidget).toList()
    }

    private fun showCurrentInterval(appWidgetId: Int, textView: TextView, powerStatusText: TextView) {
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

        textView.text = "Current: $intervalText"
        
        // Show power saving status
        updatePowerStatus(intervalMillis, powerStatusText)
    }

    private fun updatePowerSavingPreview(powerStatusText: TextView) {
        val radioGroup = findViewById<RadioGroup>(R.id.interval_radio_group)
        val selectedIntervalId = radioGroup.checkedRadioButtonId
        val intervalMillis = when (selectedIntervalId) {
            R.id.interval_1min -> 60_000L
            R.id.interval_5min -> 5 * 60_000L
            R.id.interval_10min -> 10 * 60_000L
            R.id.interval_20min -> 20 * 60_000L
            R.id.interval_30min -> 30 * 60_000L
            R.id.interval_1hour -> 60 * 60_000L
            else -> 5 * 60_000L
        }
        
        updatePowerStatus(intervalMillis, powerStatusText)
    }

    private fun updatePowerStatus(userInterval: Long, powerStatusText: TextView) {
        try {
            val powerStatus = performanceManager.getPowerSavingStatus()
            val optimizedInterval = performanceManager.getOptimizedInterval(userInterval, 0)
            val batterySavings = performanceManager.getEstimatedBatterySavings(userInterval)
            
            val statusText = StringBuilder()
            statusText.append("⚡ $powerStatus\n")
            
            if (optimizedInterval > userInterval) {
                statusText.append("📱 Actual: ${optimizedInterval/1000/60}min (${batterySavings}% longer)")
            } else {
                statusText.append("📱 Actual: ${userInterval/1000/60}min (as set)")
            }
            
            powerStatusText.text = statusText.toString()
            powerStatusText.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.w("RefreshIntervalActivity", "Error updating power status: ${e.message}")
            powerStatusText.text = "⚡ Power optimization available"
            powerStatusText.visibility = View.VISIBLE
        }
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

        // Show power optimization info
        val optimizedInterval = performanceManager.getOptimizedInterval(intervalMillis, appWidgetId)
        val message = if (optimizedInterval > intervalMillis) {
            val savings = ((optimizedInterval.toDouble() / intervalMillis.toDouble()) - 1.0) * 100
            "✅ Set to $intervalText\n⚡ Will extend to ${optimizedInterval/1000/60}min for power saving (${savings.toInt()}% longer)"
        } else {
            "✅ Set to $intervalText\n⚡ Normal interval (optimal conditions)"
        }

        showToast(message)
        Log.d("RefreshIntervalActivity", "Interval set to $intervalMillis ms for widget $appWidgetId")

        // ส่งผลลัพธ์กลับ
        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, result)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}