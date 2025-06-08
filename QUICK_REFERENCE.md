# 🚀 BitcoinWidget2 - Quick Reference

## 📁 Project Paths
- **Root**: `/Users/teerayutyeerahem/AndroidStudioProjects/BitcoinWidget2`
- **App Module**: `/Users/teerayutyeerahem/AndroidStudioProjects/BitcoinWidget2/app`
- **Source Code**: `/Users/teerayutyeerahem/AndroidStudioProjects/BitcoinWidget2/app/src/main/java/com/example/bitcoinwidget`

## 🏗️ Key Files
- **MainActivity.kt** - หน้าจอหลักของแอป
- **BitcoinPriceWidget.kt** - Widget provider สำหรับแสดงราคา Bitcoin
- **PopupActivity.kt** - Activity สำหรับ popup display
- **RefreshIntervalActivity.kt** - Activity สำหรับตั้งค่าการรีเฟรช

## 📋 Development Logs
- **Main Log**: `PROJECT_DEVELOPMENT_LOG.md`
- **Template**: `LOG_UPDATE_TEMPLATE.md`
- **This File**: `QUICK_REFERENCE.md`

## 🔧 Build Configuration
- **Package**: com.example.bitcoinwidget
- **Min SDK**: 21 (Android 5.0+)
- **Target SDK**: 34 (Android 14)
- **Kotlin**: 1.9.24
- **AGP**: 8.3.2

## 📱 Permissions Required
- INTERNET - สำหรับดึงข้อมูลราคา Bitcoin
- WAKE_LOCK - ป้องกันเครื่องหลับระหว่างอัปเดต
- SCHEDULE_EXACT_ALARM - ตั้งเวลาการอัปเดตอัตโนมัติ
- USE_EXACT_ALARM - ใช้การแจ้งเตือนแม่นยำ

## 🎯 Common Tasks
1. **อ่าน Log การพัฒนา**: `cat PROJECT_DEVELOPMENT_LOG.md`
2. **อัปเดต Log**: ใช้ template ใน `LOG_UPDATE_TEMPLATE.md`
3. **ตรวจสอบ Source Code**: `ls app/src/main/java/com/example/bitcoinwidget/`
4. **Build Project**: `./gradlew build`
5. **Clean Project**: `./gradlew clean`

## 📝 Next Session Checklist
- [ ] อ่าน PROJECT_DEVELOPMENT_LOG.md
- [ ] ตรวจสอบ source code ของแต่ละไฟล์
- [ ] ทดสอบ build process
- [ ] อัปเดต log ด้วย template

---
*Created: 2025-06-07*