package com.example.bitcoinwidget

import android.app.Activity
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import java.util.*

class PopupActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.popup_layout)

        // ตั้งค่าข้อมูล (จำลองข้อมูลที่เปลี่ยนแปลงตามเวลา)
        val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        // Fear & Greed Index (40-80)
        val fearGreedValue = 40 + (currentMinute % 40)
        findViewById<TextView>(R.id.fear_greed_value)?.text = fearGreedValue.toString()
        
        // MVRV Ratio (2.0-2.5)
        val mvrv = 2.0 + (currentMinute % 10) * 0.05
        findViewById<TextView>(R.id.mvrv_value)?.text = String.format("%.2f", mvrv)
        
        // Mining Fees (1-5 sat/vB)
        val baseFee = 1 + (currentHour % 5)
        findViewById<TextView>(R.id.fee_low)?.text = baseFee.toString()
        findViewById<TextView>(R.id.fee_medium)?.text = (baseFee + 1).toString()
        findViewById<TextView>(R.id.fee_high)?.text = (baseFee + 2).toString()
        findViewById<TextView>(R.id.fee_urgent)?.text = (baseFee + 3).toString()

        // ปิด Activity เมื่อกดนอกพื้นที่
        setFinishOnTouchOutside(true)
    }
}
