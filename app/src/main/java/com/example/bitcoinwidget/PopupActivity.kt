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
        val fearGreedValue = 40 + (currentMinute % 40) // วนระหว่าง 40-80
        val mvrv = 2.0 + (currentMinute % 10) * 0.05 // วนระหว่าง 2.0-2.5
        
        findViewById<TextView>(R.id.fear_greed_value)?.text = fearGreedValue.toString()
        findViewById<TextView>(R.id.mvrv_value)?.text = String.format("%.2f", mvrv)

        // ปิด Activity เมื่อกดนอกพื้นที่
        setFinishOnTouchOutside(true)
    }
}
