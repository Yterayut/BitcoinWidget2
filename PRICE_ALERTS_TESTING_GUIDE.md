# 🔔 Price Alerts - Quick Testing Guide

## 🧪 วิธีทดสอบ Price Alerts อย่างรวดเร็ว

### **ขั้นตอนการทดสอบ (5 นาที):**

#### **1. 📱 เปิดแอป Bitcoin Widget**
```
🏠 Main Screen → กด "🔔 Alerts" button
```

#### **2. 🧪 ทดสอบ Notification System**
```
📍 ใน Price Alerts Screen:
1. กดปุ่ม "🧪 TEST" 
2. ควรได้รับ notification ทันที
3. ถ้าไม่ได้รับ → ตรวจสอบ permissions
```

#### **3. ⚙️ ตั้งค่า Alert จริง**
```
🎯 ตั้งราคาใกล้ราคาปัจจุบัน:
- ราคาปัจจุบัน: $105,524
- Upper Alert: 106,000 (ขาย)
- Lower Alert: 105,000 (ซื้อ)
- เปิด "Enable Alerts" switch
```

#### **4. 🔍 ตรวจสอบการทำงาน**
```
📋 Check List:
✅ Enable Alerts switch เปิดอยู่
✅ ตั้งราคาเป้าหมายแล้ว
✅ ได้รับ test notification
✅ เห็น status "🔔 Active alerts" 
```

---

## 🐛 การแก้ปัญหา

### **❌ ไม่ได้รับ Test Notification**

#### **🔧 Android Settings Check:**
```
Settings → Apps → Bitcoin Widget → Notifications
- ตรวจสอบว่า "Allow notifications" เปิดอยู่
- ตรวจสอบ notification categories ทั้งหมด
```

#### **📱 System Settings:**
```
- ปิด "Do Not Disturb" mode
- ตรวจสอบ notification volume
- ลอง restart แอป
```

### **❌ Monitoring ไม่เริ่ม**

#### **🔄 Steps to Fix:**
```
1. เปิด PriceAlertsActivity
2. ปิด "Enable Alerts" switch
3. ตั้งราคาใหม่
4. เปิด "Enable Alerts" switch อีกครั้ง
5. ดู status message เปลี่ยนเป็น "🔔 Active alerts"
```

---

## 📊 Log Messages ที่ควรเห็น

### **✅ Normal Operation:**
```
🔍 Started price monitoring for alerts - Upper: 106000.0, Lower: 105000.0
🚀 Price monitoring job started successfully
💰 Current price: $105,524 (Upper: 106000.0, Lower: 105000.0)
✅ Price check complete - no alerts triggered
```

### **🔔 Alert Triggered:**
```
🚀 UPPER ALERT TRIGGERED! Price $106,100 >= $106,000
🔔 Alert triggered: 🚀 Bitcoin Price Alert - Bitcoin reached...
```

---

## 📝 Expected User Experience

### **เมื่อตั้ง Upper Alert ที่ $106,000:**
```
📈 เมื่อราคา Bitcoin ≥ $106,000:
📱 Notification: "🚀 Bitcoin Price Alert"
💬 Message: "Bitcoin reached your target price of $106,000!
           Current price: $106,100
           
           Time to consider selling! 💰"
```

### **เมื่อตั้ง Lower Alert ที่ $105,000:**
```
📉 เมื่อราคา Bitcoin ≤ $105,000:
📱 Notification: "📉 Bitcoin Price Alert"
💬 Message: "Bitcoin dropped to your target price of $105,000!
           Current price: $104,900
           
           Time to consider buying! 🛒"
```

---

## 🎯 Quick Success Indicators

### **🟢 Everything Working:**
- ✅ Test notification มาทันที
- ✅ Status แสดง "🔔 Active alerts"
- ✅ ไม่มี error messages
- ✅ Switch เปิดค้างไว้ได้

### **🔴 Something Wrong:**
- ❌ Test notification ไม่มา
- ❌ Status แสดง "🔕 Alerts disabled"
- ❌ Switch ปิดเอง
- ❌ ไม่สามารถตั้งราคาได้

---

## 🚀 Pro Tips

### **💡 Best Practices:**
1. **ตั้งราคาห่างจากราคาปัจจุบัน 1-5%** (เพื่อทดสอบ)
2. **ใช้ Test button ก่อนตั้งราคาจริง**
3. **ตรวจสอบ battery optimization** (ปิด battery optimization สำหรับแอป)
4. **ตั้งเพียง 1-2 alerts** (เพื่อไม่ให้งงกับ notifications)

### **⚡ Quick Test (30 seconds):**
```
1. เปิดแอป → Alerts
2. กด TEST → ได้ notification ✅
3. ตั้ง Upper: [ราคาปัจจุบัน + 500]
4. เปิด Enable switch
5. รอ notification เมื่อราคาขยับ
```

---

## 📞 หากยังมีปัญหา

### **🔍 Debug Steps:**
1. **ลองติดตั้ง APK ใหม่**
2. **ตรวจสอบ Android version** (ต้องเป็น 5.0+)
3. **ลองเปลี่ยนราคาเป้าหมาย**
4. **Restart เครื่อง**

### **📋 Information to Check:**
- **Android Version**: _______
- **App Version**: 1.0.0
- **Notification Permission**: Allowed/Denied
- **Test Notification**: Working/Not Working
- **Current BTC Price**: $_______
- **Alert Settings**: Upper $______ / Lower $______

---

**🎉 หลังจากแก้ไขแล้ว Price Alerts ควรทำงานได้ 100%!**

*หากยังมีปัญหา แสดงว่าอาจมีปัญหาเฉพาะเครื่อง เช่น battery optimization หรือ custom ROM settings*