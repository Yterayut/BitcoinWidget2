package com.example.bitcoinwidget

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class ExplanationActivity : Activity() {
    
    companion object {
        const val EXTRA_CARD_TYPE = "card_type"
        
        // Card types
        const val CARD_MARKET_OVERVIEW = "market_overview"
        const val CARD_NETWORK_DATA = "network_data"
        const val CARD_NETWORK_FEES = "network_fees"
        const val CARD_FEAR_GREED = "fear_greed"
        const val CARD_MVRV = "mvrv"
        
        fun start(context: Context, cardType: String) {
            val intent = Intent(context, ExplanationActivity::class.java)
            intent.putExtra(EXTRA_CARD_TYPE, cardType)
            context.startActivity(intent)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explanation)
        
        val cardType = intent.getStringExtra(EXTRA_CARD_TYPE) ?: CARD_MARKET_OVERVIEW
        
        setupUI(cardType)
        setupCloseButton()
    }
    
    private fun setupUI(cardType: String) {
        val titleText = findViewById<TextView>(R.id.explanation_title)
        val iconImage = findViewById<ImageView>(R.id.explanation_icon)
        val contentLayout = findViewById<LinearLayout>(R.id.explanation_content)
        
        when (cardType) {
            CARD_MARKET_OVERVIEW -> setupMarketOverviewExplanation(titleText, iconImage, contentLayout)
            CARD_NETWORK_DATA -> setupNetworkDataExplanation(titleText, iconImage, contentLayout)
            CARD_NETWORK_FEES -> setupNetworkFeesExplanation(titleText, iconImage, contentLayout)
            CARD_FEAR_GREED -> setupFearGreedExplanation(titleText, iconImage, contentLayout)
            CARD_MVRV -> setupMvrvExplanation(titleText, iconImage, contentLayout)
        }
    }
    
    private fun setupMarketOverviewExplanation(title: TextView, icon: ImageView, content: LinearLayout) {
        title.text = "📊 Market Overview Explained"
        
        addExplanationSection(content, 
            "💰 Market Cap",
            "Market Capitalization คือมูลค่ารวมของ Bitcoin ทั้งหมดในตลาด",
            listOf(
                "คำนวณจาก: ราคาปัจจุบัน × จำนวน Bitcoin ที่หมุนเวียน",
                "ตัวอย่าง: $105,000 × 19.75M BTC = $2.08 Trillion",
                "ใช้เปรียบเทียบขนาดตลาด Bitcoin กับสินทรัพย์อื่น",
                "Market Cap สูง = ตลาดใหญ่ และมีความเสถียรมากขึ้น"
            )
        )
        
        addExplanationSection(content,
            "👑 Bitcoin Dominance", 
            "เปอร์เซ็นต์ที่ Bitcoin ครองในตลาดคริปโตทั้งหมด",
            listOf(
                "คำนวณจาก: Market Cap ของ Bitcoin ÷ Market Cap ทั้งหมด",
                "63.5% = Bitcoin มีส่วนแบ่งตลาด 63.5% ของคริปโตทั้งหมด",
                "Dominance สูง = Bitcoin แข็งแกร่งกว่า Altcoin",
                "Dominance ต่ำ = Altcoin กำลังโตเร็วกว่า Bitcoin"
            )
        )
        
        addExplanationSection(content,
            "🪙 Circulating Supply",
            "จำนวน Bitcoin ที่ถูกขุดและหมุนเวียนในตลาดแล้ว",
            listOf(
                "ปัจจุบัน: 19.75M BTC จาก 21M BTC ทั้งหมด",
                "เหลือที่ยังไม่ขุด: ~1.25M BTC (ประมาณ 6%)",
                "Bitcoin ใหม่จะหมดในปี 2140 (อีกประมาณ 116 ปี)",
                "Supply จำกัด = ทำให้ Bitcoin มีคุณสมบัติป้องกันเงินเฟ้อ"
            )
        )
    }
    
    private fun setupNetworkDataExplanation(title: TextView, icon: ImageView, content: LinearLayout) {
        title.text = "⛏️ Network Data Explained"
        
        addExplanationSection(content,
            "🧱 Block Height",
            "จำนวนบล็อกทั้งหมดที่ถูกขุดในเครือข่าย Bitcoin",
            listOf(
                "ปัจจุบัน: ~900,000 บล็อก (นับตั้งแต่ปี 2009)",
                "บล็อกใหม่ถูกขุดทุก ~10 นาที",
                "แต่ละบล็อกบรรจุธุรกรรมได้ ~2,000-3,000 รายการ",
                "Block Height = อายุและประวัติของเครือข่าย Bitcoin"
            )
        )
        
        addExplanationSection(content,
            "⏰ Bitcoin Halving",
            "เหตุการณ์ลดรางวัลการขุดครึ่งหนึ่งทุก 4 ปี",
            listOf(
                "เกิดขึ้นทุก 210,000 บล็อก (~4 ปี)",
                "รางวัลปัจจุบัน: 3.125 BTC ต่อบล็อก",
                "Halving ถัดไป: 2028 → รางวัลจะลดเป็น 1.5625 BTC",
                "เป้าหมาย: ควบคุมอุปทานและสร้างความขาดแคลน",
                "ประวัติ: 2012(50→25), 2016(25→12.5), 2020(12.5→6.25), 2024(6.25→3.125)"
            )
        )
        
        addExplanationSection(content,
            "⚡ Mining Cost vs Price",
            "เปรียบเทียบราคา Bitcoin กับต้นทุนการขุด",
            listOf(
                "ต้นทุนการขุด = ค่าไฟ + ค่าฮาร์ดแวร์ + ค่าดำเนินการ",
                "ปัจจุบัน: ~$42,000 ต้นทุนเฉลี่ย",
                "ราคา $105,000 = 2.5x เหนือต้นทุน",
                "เหนือต้นทุน = นักขุดกำไรดี, เครือข่ายแข็งแกร่ง",
                "ใกล้ต้นทุน = นักขุดบางรายอาจหยุดขุด"
            )
        )
    }
    
    private fun setupNetworkFeesExplanation(title: TextView, icon: ImageView, content: LinearLayout) {
        title.text = "💳 Network Fees Explained"
        
        addExplanationSection(content,
            "🤔 ทำไมต้องมีค่าธรรมเนียม?",
            "ค่าธรรมเนียมเป็นแรงจูงใจให้นักขุดยืนยันธุรกรรม",
            listOf(
                "นักขุดเลือกธุรกรรมที่ให้ค่าธรรมเนียมสูงก่อน",
                "ยิ่งจ่ายมาก ยิ่งได้รับการยืนยันเร็ว",
                "ค่าธรรมเนียมป้องกัน spam และทำให้เครือข่ายมีประสิทธิภาพ",
                "เป็นรายได้หลักของนักขุดหลังจาก Halving"
            )
        )
        
        addExplanationSection(content,
            "⏱️ ระดับความเร็ว",
            "เลือกความเร็วตามความต้องการ",
            listOf(
                "🐌 SLOW (1 sat/vB): 1-3 ชั่วโมง - ประหยัดสุด",
                "🚶 STANDARD (2 sat/vB): 30-60 นาที - ใช้ทั่วไป",
                "🏃 FAST (3 sat/vB): 10-30 นาที - เร่งด่วน", 
                "🚀 URGENT (3+ sat/vB): 0-10 นาที - ฉุกเฉิน"
            )
        )
        
        addExplanationSection(content,
            "📊 sat/vB คืออะไร?",
            "หน่วยวัดค่าธรรมเนียม Bitcoin",
            listOf(
                "sat = satoshi = 0.00000001 BTC",
                "vB = virtual byte = ขนาดธุรกรรม",
                "ค่าธรรมเนียมรวม = sat/vB × ขนาดธุรกรรม",
                "ตัวอย่าง: 3 sat/vB × 250 vB = 750 sats = ~$0.08"
            )
        )
    }
    
    private fun setupFearGreedExplanation(title: TextView, icon: ImageView, content: LinearLayout) {
        title.text = "😨 Fear & Greed Index Explained"
        
        addExplanationSection(content,
            "🎭 ดัชนีวัดอารมณ์ตลาด",
            "วัดความรู้สึกของนักลงทุนในตลาดคริปโต",
            listOf(
                "0-24 = Extreme Fear 😱 (ความกลัวสุดขั้ว)",
                "25-49 = Fear 😰 (ความกลัว)",
                "50-54 = Neutral 😐 (เป็นกลาง)",
                "55-74 = Greed 😊 (ความโลภ)",
                "75-100 = Extreme Greed 🤑 (ความโลภสุดขั้ว)"
            )
        )
        
        addExplanationSection(content,
            "📈 การคำนวณค่า",
            "Alternative.me รวบรวมข้อมูลจาก 5 แหล่ง",
            listOf(
                "Volatility (25%): ความผันผวนของราคา",
                "Market Volume (25%): ปริมาณการซื้อขาย", 
                "Social Media (15%): กิจกรรมโซเชียลมีเดีย",
                "Surveys (15%): การสำรวจความเห็น",
                "Bitcoin Dominance (10%): การครองตลาด",
                "Google Trends (10%): ความสนใจค้นหา"
            )
        )
        
        addExplanationSection(content,
            "💡 การใช้ประโยชน์",
            "เครื่องมือช่วยตัดสินใจการลงทุน",
            listOf(
                "Extreme Fear = โอกาสซื้อ (ตลาดขายมากเกินไป)",
                "Extreme Greed = ควรระวัง (ตลาดร้อนมากเกินไป)",
                "Contrarian Strategy: ทำตรงข้ามกับความรู้สึกส่วนใหญ่",
                "ใช้ประกอบการวิเคราะห์ ไม่ใช่ตัดสินใจเพียงอย่างเดียว"
            )
        )
    }
    
    private fun setupMvrvExplanation(title: TextView, icon: ImageView, content: LinearLayout) {
        title.text = "📊 MVRV Z-Score Explained"
        
        addExplanationSection(content,
            "🤓 MVRV Z-Score คืออะไร?",
            "ตัวชี้วัดการประเมินราคา Bitcoin เทียบกับมูลค่าเฉลี่ยในอดีต",
            listOf(
                "MVRV = Market Value to Realized Value",
                "Market Value = ราคาปัจจุบัน × Supply",
                "Realized Value = ราคาเฉลี่ยที่เหรียญแต่ละเหรียญถูกซื้อครั้งสุดท้าย",
                "Z-Score = วัดว่าเบี่ยงเบนจากค่าเฉลี่ยมากแค่ไหน"
            )
        )
        
        addExplanationSection(content,
            "📏 การอ่านค่า Z-Score",
            "เกณฑ์การประเมินราคา Bitcoin",
            listOf(
                "< -0.5 = 🟢 Undervalued (ราคาถูกกว่าที่ควร)",
                "-0.5 ถึง 2.4 = 🟡 Fair Value (ราคาเหมาะสม)",
                "2.4 ถึง 7.0 = 🔴 Overvalued (ราคาแพงกว่าที่ควร)",
                "> 7.0 = 🔴 Extreme Bubble (บับเบิลสุดขั้ว)",
                "ปัจจุบัน: 2.10 = แพงเล็กน้อย แต่ยังไม่ถึงขั้นบับเบิล"
            )
        )
        
        addExplanationSection(content,
            "📈 ประวัติการใช้งาน",
            "Track Record ของ MVRV Z-Score",
            listOf(
                "2017 Bull Run: Z-Score ถึง 7+ ก่อน Crash",
                "2021 Bull Run: Z-Score ถึง 7+ ก่อน Correction",
                "Bear Markets: มักเกิดเมื่อ Z-Score < 0",
                "Best Buy Zones: Z-Score < -1.0",
                "Take Profit Zones: Z-Score > 4.0"
            )
        )
        
        addExplanationSection(content,
            "💰 การใช้ในการลงทุน",
            "กลยุทธ์การใช้ MVRV Z-Score",
            listOf(
                "DCA Strategy: ซื้อน้อยเมื่อ Z-Score สูง, ซื้อมากเมื่อต่ำ",
                "Long-term Hold: ขายบางส่วนเมื่อ Z-Score > 6",
                "Risk Management: เพิ่ม Cash เมื่อ Z-Score สูง",
                "โปรดจำ: ใช้เป็นเครื่องมือประกอบ ไม่ใช่ตัดสินเพียงอย่างเดียว"
            )
        )
    }
    
    private fun addExplanationSection(parent: LinearLayout, title: String, subtitle: String, points: List<String>) {
        // Section container
        val sectionLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = ContextCompat.getDrawable(this@ExplanationActivity, R.drawable.modern_card_bg)
            setPadding(48, 32, 48, 32)
            
            val marginParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 32)
            }
            layoutParams = marginParams
        }
        
        // Title
        val titleView = TextView(this).apply {
            text = title
            textSize = 18f
            setTextColor(ContextCompat.getColor(this@ExplanationActivity, android.R.color.white))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        sectionLayout.addView(titleView)
        
        // Subtitle
        val subtitleView = TextView(this).apply {
            text = subtitle
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@ExplanationActivity, R.color.text_secondary))
            setPadding(0, 16, 0, 24)
            setLineSpacing(1.2f, 1.0f)
        }
        sectionLayout.addView(subtitleView)
        
        // Points
        points.forEach { point ->
            val pointView = TextView(this).apply {
                text = "• $point"
                textSize = 13f
                setTextColor(ContextCompat.getColor(this@ExplanationActivity, R.color.text_primary))
                setPadding(32, 8, 0, 8)
                setLineSpacing(1.3f, 1.0f)
            }
            sectionLayout.addView(pointView)
        }
        
        parent.addView(sectionLayout)
    }
    
    private fun setupCloseButton() {
        findViewById<Button>(R.id.btn_close_explanation).setOnClickListener {
            finish()
        }
    }
}
