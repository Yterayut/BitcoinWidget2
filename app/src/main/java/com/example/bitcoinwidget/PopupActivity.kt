package com.example.bitcoinwidget

import android.app.Activity
import android.os.Bundle
import android.view.Window
import android.widget.TextView

class PopupActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.popup_layout)

        // ตั้งค่าข้อมูล (สมมติค่า)
        findViewById<TextView>(R.id.fear_greed_value)?.text = "72"
        findViewById<TextView>(R.id.mvrv_value)?.text = "2.32"

        // ปิด Activity เมื่อกดนอกพื้นที่
        setFinishOnTouchOutside(true)
    }
}
