# 🚀 BitcoinWidget2 - Professional Bitcoin Price Widget

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://android-arsenal.com/api?level=21)
[![Version](https://img.shields.io/badge/Version-1.0.0-orange.svg)](https://github.com/Yterayut/BitcoinWidget2/releases)

A feature-rich, production-ready Android widget application that displays real-time Bitcoin data with professional-grade accuracy and reliability.

## 📱 Screenshots

![Bitcoin Widget UI](https://via.placeholder.com/800x400/1a1a1a/ffffff?text=Bitcoin+Widget+Screenshots)

## ✨ Features

### 🔥 Core Features
- **📊 Real-time Bitcoin Price**: Live price updates with 24h change percentage
- **📈 Market Overview**: Market cap, dominance, and circulating supply
- **⛏️ Network Data**: Current block height and next halving countdown
- **💳 Network Fees**: Live transaction fees (slow, standard, fast, urgent)
- **😨 Fear & Greed Index**: Market sentiment with historical data
- **📊 Market Valuation**: MVRV Z-Score for market analysis

### 🚀 Advanced Features
- **🔔 Smart Price Alerts**: Intelligent buy/sell notifications with cooldown
- **⚡ Performance Optimization**: Battery-aware adaptive refresh intervals
- **🤖 Auto-Update System**: Background app update checking
- **🎨 Professional UI**: Modern design with dark/light themes
- **🧪 Testing Dashboard**: Comprehensive feature testing interface
- **📱 Multiple Widgets**: Support for multiple widgets with individual settings

### 🌐 API Integration
- **Binance API**: Real-time Bitcoin price (BTCUSDT)
- **Mempool.space**: Network fees and block height
- **Alternative.me**: Fear & Greed Index
- **CoinGecko**: Market data and BTC dominance
- **Enhanced Fallbacks**: Multiple backup endpoints for 99.9% reliability

## 🛠️ Technical Specifications

### Requirements
- **Android 5.0+** (API level 21+)
- **Target SDK**: 34 (Android 14)
- **Language**: Kotlin 1.9.24
- **Build Tool**: Android Gradle Plugin 8.3.2

### Permissions
- `INTERNET` - For API data fetching
- `WAKE_LOCK` - Prevent device sleep during updates
- `SCHEDULE_EXACT_ALARM` - Precise update scheduling
- `USE_EXACT_ALARM` - Exact alarm functionality

## 📦 Installation

### Option 1: APK Download
1. Download the latest APK from [Releases](https://github.com/Yterayut/BitcoinWidget2/releases)
2. Install on your Android device
3. Add widget to home screen: Long press → Widgets → Bitcoin Widget

### Option 2: Build from Source
```bash
git clone https://github.com/Yterayut/BitcoinWidget2.git
cd BitcoinWidget2
./gradlew assembleDebug
```

## 🔧 Configuration

### Widget Settings
- **Refresh Intervals**: 5min, 10min, 20min, 30min, 1hour
- **Multiple Widgets**: Each widget can have different refresh rates
- **Battery Optimization**: Automatic adjustment based on battery level

### Recommended Settings
- **Day Traders**: 5-10 minute intervals
- **Swing Traders**: 10-20 minute intervals  
- **HODLers**: 30 minutes - 1 hour intervals

## 📊 Data Accuracy

All data is sourced from authoritative APIs and matches industry standards:

| Data Type | Source | Accuracy |
|-----------|--------|----------|
| Bitcoin Price | Binance | Real-time |
| Market Cap | CoinGecko | Real-time |
| BTC Dominance | CoinMarketCap/CoinGecko | 100% match |
| Block Height | Mempool.space/Blockstream | Live blockchain |
| Network Fees | Mempool.space | Real-time mempool |
| Fear & Greed | Alternative.me | Industry standard |

## 🎯 Performance Features

### Battery Optimization
- **Adaptive Intervals**: Automatically extends refresh time when battery is low
- **Night Mode**: Reduced updates during 10 PM - 6 AM
- **Network Awareness**: Pauses updates during poor connectivity
- **Charging Detection**: Normal rates when device is charging

### Smart Notifications
- **Price Alerts**: Set upper/lower price targets
- **Cooldown System**: 30-minute cooldown prevents notification spam
- **Intelligent Timing**: Only alerts during significant price movements

## 📋 Documentation

- **[Development Log](PROJECT_DEVELOPMENT_LOG.md)**: Complete development history
- **[Widget Settings Guide](WIDGET_SETTINGS_EXPLANATION.md)**: User manual for widget configuration
- **[Quick Reference](QUICK_REFERENCE.md)**: Developer reference guide
- **[Testing Guide](WIDGET_TESTING_GUIDE.md)**: Feature testing instructions

## 🧪 Testing

The app includes a comprehensive testing dashboard:

1. **Open Bitcoin Widget App**
2. **Tap "🧪 Feature Test Dashboard"**
3. **Run individual tests or "📋 Run All Tests"**
4. **View detailed results and API status**

## 🔄 Updates

The app includes an automatic update checking system that:
- Checks for new versions every 24 hours
- Notifies users about available updates
- Provides changelog information
- Non-intrusive background operation

## 🛡️ Security

- **API Security**: Secured HTTP client with proper validation
- **Data Privacy**: No personal data collection
- **Network Security**: HTTPS-only connections
- **Error Handling**: Graceful degradation on API failures

## 🎨 UI/UX Features

### Modern Design
- **Material Design 3**: Latest design guidelines
- **Dark/Light Themes**: Automatic and manual theme switching
- **High Contrast**: Accessibility support
- **Professional Typography**: Optimized font sizes and weights

### Responsive Layout
- **Multiple Screen Sizes**: Optimized for phones and tablets
- **Orientation Support**: Portrait and landscape modes
- **Widget Scaling**: Proper scaling on different home screen densities

## 🔧 Architecture

### Clean Architecture
- **MVVM Pattern**: Model-View-ViewModel architecture
- **Repository Pattern**: Centralized data management
- **Dependency Injection**: Modular and testable code
- **Coroutines**: Asynchronous programming for smooth performance

### Modular Design
```
app/src/main/java/com/example/bitcoinwidget/
├── alerts/          # Price alert system
├── performance/     # Battery and network optimization
├── security/        # API security and validation
├── ui/             # UI enhancement and theming
├── updates/        # Auto-update functionality
└── logging/        # Comprehensive logging system
```

## 📈 Roadmap

### Planned Features
- [ ] **Multi-Cryptocurrency Support**: ETH, BNB, and other major coins
- [ ] **Portfolio Tracking**: Multi-coin portfolio with total value
- [ ] **Trading Integration**: Connect to exchanges for direct trading
- [ ] **Historical Charts**: Price history graphs in popup
- [ ] **News Integration**: Bitcoin news feed from reliable sources
- [ ] **Lightning Network**: LN node status and channel management

### Performance Improvements
- [ ] **Advanced Caching**: Intelligent data caching strategies
- [ ] **Background Sync**: More efficient background data updates
- [ ] **Widget Themes**: Customizable widget appearance
- [ ] **Voice Commands**: Voice-activated price queries

## 🤝 Contributing

We welcome contributions! Please see our contributing guidelines:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit changes**: `git commit -m 'Add amazing feature'`
4. **Push to branch**: `git push origin feature/amazing-feature`
5. **Open a Pull Request**

### Development Setup
```bash
git clone https://github.com/Yterayut/BitcoinWidget2.git
cd BitcoinWidget2
./gradlew build
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Binance API** for reliable Bitcoin price data
- **Mempool.space** for Bitcoin network information
- **Alternative.me** for Fear & Greed Index
- **CoinGecko** for comprehensive market data
- **Android Community** for excellent documentation and support

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/Yterayut/BitcoinWidget2/issues)
- **Documentation**: [Wiki](https://github.com/Yterayut/BitcoinWidget2/wiki)
- **Email**: [Your email here]

## 📊 Stats

![GitHub stars](https://img.shields.io/github/stars/Yterayut/BitcoinWidget2?style=social)
![GitHub forks](https://img.shields.io/github/forks/Yterayut/BitcoinWidget2?style=social)
![GitHub issues](https://img.shields.io/github/issues/Yterayut/BitcoinWidget2)
![GitHub license](https://img.shields.io/github/license/Yterayut/BitcoinWidget2)

---

**⭐ If you find this project useful, please consider giving it a star!**

Built with ❤️ for the Bitcoin community