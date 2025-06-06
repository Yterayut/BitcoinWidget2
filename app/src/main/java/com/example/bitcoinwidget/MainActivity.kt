package com.example.bitcoinwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

class MainActivity : Activity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create simple layout programmatically
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }
        
        val titleText = TextView(this).apply {
            text = "Bitcoin Widget App"
            textSize = 24f
            setPadding(0, 0, 0, 40)
        }
        
        val infoText = TextView(this).apply {
            text = "Add Bitcoin Widget to your home screen:\n\n1. Long press on home screen\n2. Select 'Widgets'\n3. Find 'Bitcoin Widget'\n4. Drag to home screen"
            textSize = 16f
            setPadding(0, 0, 0, 40)
        }
        
        val updateButton = Button(this).apply {
            text = "Update All Widgets"
            setOnClickListener { updateAllWidgets() }
        }
        
        layout.addView(titleText)
        layout.addView(infoText)
        layout.addView(updateButton)
        
        setContentView(layout)
    }
    
    private fun updateAllWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val thisWidget = ComponentName(this, BitcoinPriceWidget::class.java)
        val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
        
        val intent = Intent(this, BitcoinPriceWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds)
        }
        sendBroadcast(intent)
    }
}
