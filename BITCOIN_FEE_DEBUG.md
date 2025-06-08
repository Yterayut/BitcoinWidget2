# 🧪 Bitcoin Fee Testing - Mempool.space API Debug

## 🔍 Direct API Testing

### **mempool.space API Endpoints:**
```bash
# Current recommended fees
curl "https://mempool.space/api/v1/fees/recommended"

# Expected response:
{
  "fastestFee": 1,
  "halfHourFee": 1, 
  "hourFee": 1,
  "economyFee": 1,
  "minimumFee": 1
}
```

### **Thai version:**
```bash
curl "https://mempool.space/th/api/v1/fees/recommended"
```

### **Alternative endpoints:**
```bash
# Blockstream API
curl "https://blockstream.info/api/fee-estimates"

# Mempool status
curl "https://mempool.space/api/mempool"
```

## 🔧 Debug Steps for Bitcoin Widget

### **1. Check API Response:**
```
1. Open Android Studio Logcat
2. Filter: "BTC Fees" or "ApiService"
3. Look for messages like:
   💳 Fetching Bitcoin fees from endpoint 1: https://mempool.space/api/v1/fees/recommended
   📥 Fees response from ...: {"fastestFee":1,"halfHourFee":1,"hourFee":1,"economyFee":1,"minimumFee":1}
   ✅ BTC Fees from mempool.space: Fastest=1, Half=1, Hour=1, Economy=1, Min=1
```

### **2. Widget Display Check:**
```
Expected in widget popup:
- SLOW: 1 sat/vB    (hourFee)
- STANDARD: 1 sat/vB (halfHourFee) 
- FAST: 1 sat/vB    (fastestFee)
- URGENT: 1 sat/vB  (fastestFee)
```

## 🐛 Common Issues & Fixes

### **Issue: Widget shows old cached values**
```
Fix: Force refresh widget
1. Open Bitcoin Widget app
2. Tap "🔄 Force Update Widget"
3. Check popup again
```

### **Issue: API returning cached data**
```
Fix: Clear app cache
Settings → Apps → Bitcoin Widget → Storage → Clear Cache
```

### **Issue: Wrong endpoint being used**
```
Check logs for which endpoint succeeded:
✅ BTC Fees from mempool.space: ...
```

## 📊 Current Expected Values (June 2025)

Based on mempool.space screenshot:
- **Fast**: 1 sat/vB (next block)
- **Standard**: 1 sat/vB (~30 min)
- **Slow**: 1 sat/vB (~1 hour)

*Note: Very low fee period - Bitcoin network is not congested*

## 🚀 Test Procedure

### **Manual API Test:**
1. **Open browser/Postman**
2. **GET**: `https://mempool.space/api/v1/fees/recommended`
3. **Check response matches widget data**

### **Widget Test:**
1. **Open Bitcoin Widget app**
2. **Tap "🌐 Test API"**
3. **Check logs for fee values**
4. **Open widget popup**
5. **Verify fees match API response**

---

**Expected Result**: Widget should now show 1 sat/vB for all fee tiers, matching mempool.space exactly!