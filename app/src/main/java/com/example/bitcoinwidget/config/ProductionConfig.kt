package com.example.bitcoinwidget.config

/**
 * Production Configuration for BitcoinWidget2
 * Contains all production-ready settings and URLs
 */
object ProductionConfig {
    
    // App Information
    const val APP_NAME = "Bitcoin Widget"
    const val PACKAGE_NAME = "com.teerayut.bitcoinwidget"
    
    // Update Configuration
    const val GITHUB_REPO_OWNER = "your-username"  // Replace with actual GitHub username
    const val GITHUB_REPO_NAME = "BitcoinWidget2"
    const val GITHUB_RELEASES_URL = "https://api.github.com/repos/$GITHUB_REPO_OWNER/$GITHUB_REPO_NAME/releases/latest"
    
    // Alternative Update Sources
    const val ALTERNATIVE_UPDATE_URL = "https://your-domain.com/api/bitcoin-widget/version"  // Replace with your server
    const val FIREBASE_REMOTE_CONFIG_ENABLED = false  // Set true if using Firebase
    
    // API Endpoints (Production)
    object APIs {
        // Primary Bitcoin Price APIs
        const val BINANCE_24HR_URL = "https://api.binance.com/api/v3/ticker/24hr?symbol=BTCUSDT"
        const val BINANCE_PRICE_URL = "https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT"
        const val COINGECKO_URL = "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=usd&include_24hr_change=true"
        const val COINBASE_URL = "https://api.coinbase.com/v2/exchange-rates?currency=BTC"
        
        // Fallback Price APIs
        const val COINDESK_URL = "https://api.coindesk.com/v1/bpi/currentprice.json"
        const val COINLAYER_URL = "https://api.coinlayer.com/api/live?access_key=demo&symbols=BTC"
        
        // Network and Market Data
        const val MEMPOOL_FEES_URL = "https://mempool.space/api/v1/fees/recommended"
        const val FEAR_GREED_URL = "https://api.alternative.me/fng/?limit=1"
        const val MVRV_ZSCORE_URL = "https://bitcoin-data.com/v1/mvrv-zscore/last"
        
        // Backup APIs
        const val GLASSNODE_MVRV_URL = "https://api.glassnode.com/v1/metrics/market/mvrv_z_score?a=BTC&api_key=demo"
    }
    
    // SSL Certificate Pins (Updated for 2025)
    object CertificatePins {
        // Binance (Valid until 2026)
        const val BINANCE_PRIMARY = "sha256/sE9i7kDJ92hPGqiSRAM6KeXUTL6+wuTu7MFhkuP6eBA="
        const val BINANCE_BACKUP = "sha256/FEzVOUp4dF3gI0ZVPRJhFbSD608VqHnhEpv8R4GgqAo="
        
        // Mempool.space (Valid until 2026)
        const val MEMPOOL_PRIMARY = "sha256/Y9mvm0exBk1JoQ57f9Vm28jKo5lFm/woKcVxrYxu80o="
        const val MEMPOOL_BACKUP = "sha256/sE9i7kDJ92hPGqiSRAM6KeXUTL6+wuTu7MFhkuP6eBA="
        
        // Universal backup pin for other APIs
        const val UNIVERSAL_BACKUP = "sha256/sE9i7kDJ92hPGqiSRAM6KeXUTL6+wuTu7MFhkuP6eBA="
    }
    
    // Performance Settings
    object Performance {
        // Network timeouts (milliseconds)
        const val CONNECT_TIMEOUT = 15_000L
        const val READ_TIMEOUT = 20_000L
        const val WRITE_TIMEOUT = 15_000L
        
        // Update intervals (milliseconds)
        const val NORMAL_UPDATE_INTERVAL = 5 * 60 * 1000L      // 5 minutes
        const val LOW_BATTERY_INTERVAL = 15 * 60 * 1000L       // 15 minutes
        const val CRITICAL_BATTERY_INTERVAL = 30 * 60 * 1000L  // 30 minutes
        const val NIGHT_MODE_INTERVAL = 30 * 60 * 1000L        // 30 minutes
        
        // Battery thresholds
        const val LOW_BATTERY_THRESHOLD = 20
        const val CRITICAL_BATTERY_THRESHOLD = 10
        
        // Night mode hours (24-hour format)
        const val NIGHT_MODE_START_HOUR = 22  // 10 PM
        const val NIGHT_MODE_END_HOUR = 6     // 6 AM
    }
    
    // Price Alert Settings
    object PriceAlerts {
        const val CHECK_INTERVAL_MS = 30_000L  // 30 seconds
        const val COOLDOWN_PERIOD_MS = 30 * 60 * 1000L  // 30 minutes
        const val MAX_ALERTS_PER_USER = 10
    }
    
    // Auto-Update Settings
    object AutoUpdate {
        const val CHECK_INTERVAL_MS = 24 * 60 * 60 * 1000L  // 24 hours
        const val ENABLED_BY_DEFAULT = true
        const val FORCE_UPDATE_THRESHOLD_DAYS = 30
    }
    
    // Security Settings
    object Security {
        const val ENABLE_CERTIFICATE_PINNING = true
        const val ENABLE_REQUEST_VALIDATION = true
        const val ENABLE_RESPONSE_VALIDATION = true
        const val MAX_RESPONSE_SIZE_BYTES = 1024 * 1024  // 1MB
    }
    
    // Logging and Analytics
    object Logging {
        const val ENABLE_DEBUG_LOGGING = false  // Set false for production
        const val ENABLE_CRASH_REPORTING = true
        const val ENABLE_ANALYTICS = false  // Set true if implementing analytics
    }
    
    // Feature Flags
    object Features {
        const val ENABLE_PRICE_ALERTS = true
        const val ENABLE_AUTO_UPDATE = true
        const val ENABLE_PERFORMANCE_OPTIMIZATION = true
        const val ENABLE_DARK_MODE = true
        const val ENABLE_WIDGET_CUSTOMIZATION = true
    }
    
    // User Interface
    object UI {
        const val DEFAULT_THEME = "auto"  // "light", "dark", "auto"
        const val ENABLE_ANIMATIONS = true
        const val ENABLE_HAPTIC_FEEDBACK = true
        const val SHOW_ONBOARDING = true
    }
}
