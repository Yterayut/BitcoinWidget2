package com.example.bitcoinwidget.security

import android.util.Log
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

/**
 * Security Manager for Bitcoin Widget
 * Handles SSL Certificate Pinning, Data Encryption, and Security Validation
 */
class SecurityManager {
    
    companion object {
        private const val TAG = "SecurityManager"
        
        // SSL Certificate Pinning for major APIs
        private val CERTIFICATE_PINNER = CertificatePinner.Builder()
            .add("api.binance.com", "sha256/sE9i7kDJ92hPGqiSRAM6KeXUTL6+wuTu7MFhkuP6eBA=")
            .add("api.binance.com", "sha256/FEzVOUp4dF3gI0ZVPRJhFbSD608VqHnhEpv8R4GgqAo=") // Backup pin
            .add("mempool.space", "sha256/Y9mvm0exBk1JoQ57f9Vm28jKo5lFm/woKcVxrYxu80o=")
            .add("mempool.space", "sha256/sE9i7kDJ92hPGqiSRAM6KeXUTL6+wuTu7MFhkuP6eBA=") // Backup pin
            .add("api.coingecko.com", "sha256/sE9i7kDJ92hPGqiSRAM6KeXUTL6+wuTu7MFhkuP6eBA=")
            .add("api.alternative.me", "sha256/sE9i7kDJ92hPGqiSRAM6KeXUTL6+wuTu7MFhkuP6eBA=")
            .add("bitcoin-data.com", "sha256/sE9i7kDJ92hPGqiSRAM6KeXUTL6+wuTu7MFhkuP6eBA=")
            .build()
    }
    
    /**
     * Create secured OkHttpClient with SSL Pinning and enhanced security
     */
    fun createSecuredHttpClient(): OkHttpClient {
        Log.d(TAG, "🔒 Creating secured HTTP client with SSL pinning")
        
        return OkHttpClient.Builder()
            .certificatePinner(CERTIFICATE_PINNER)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                val request = chain.request()
                Log.d(TAG, "🌐 Secured request to: ${request.url}")
                
                val securedRequest = request.newBuilder()
                    .addHeader("User-Agent", "BitcoinWidget/2.0-Secured")
                    .addHeader("Accept", "application/json")
                    .addHeader("Cache-Control", "no-cache")
                    .build()
                
                val startTime = System.currentTimeMillis()
                try {
                    val response = chain.proceed(securedRequest)
                    val duration = System.currentTimeMillis() - startTime
                    
                    Log.d(TAG, "🛡️ Secured request completed in ${duration}ms with code: ${response.code}")
                    
                    if (!response.isSuccessful) {
                        Log.w(TAG, "⚠️ Non-successful response: ${response.code} ${response.message}")
                    }
                    
                    response
                } catch (e: Exception) {
                    val duration = System.currentTimeMillis() - startTime
                    Log.e(TAG, "❌ Secured request failed after ${duration}ms: ${e.message}")
                    throw e
                }
            }
            .build()
    }
    
    /**
     * Validate API response data for security and integrity
     */
    fun validateApiResponse(data: String, source: String): Boolean {
        try {
            Log.d(TAG, "🔍 Validating API response from: $source")
            
            // Check for suspicious content
            val suspiciousPatterns = listOf(
                "<script", "javascript:", "eval(", "document.cookie",
                "window.location", "iframe", "embed", "object"
            )
            
            val lowerData = data.lowercase()
            for (pattern in suspiciousPatterns) {
                if (lowerData.contains(pattern)) {
                    Log.w(TAG, "⚠️ Suspicious content detected in $source: $pattern")
                    return false
                }
            }
            
            // Validate JSON structure for API responses
            if (data.startsWith("{") || data.startsWith("[")) {
                try {
                    // Basic JSON validation (could use Gson for deeper validation)
                    val braceCount = data.count { it == '{' } - data.count { it == '}' }
                    val bracketCount = data.count { it == '[' } - data.count { it == ']' }
                    
                    if (braceCount != 0 || bracketCount != 0) {
                        Log.w(TAG, "⚠️ Invalid JSON structure from $source")
                        return false
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ JSON validation error for $source: ${e.message}")
                    return false
                }
            }
            
            // Check response size (prevent memory attacks)
            if (data.length > 1_000_000) { // 1MB limit
                Log.w(TAG, "⚠️ Response too large from $source: ${data.length} bytes")
                return false
            }
            
            Log.d(TAG, "✅ API response validation passed for: $source")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error validating API response from $source: ${e.message}")
            return false
        }
    }
    
    /**
     * Encrypt sensitive data for storage
     */
    fun encryptData(data: String, key: String = "BitcoinWidget2025"): String? {
        try {
            Log.d(TAG, "🔐 Encrypting sensitive data")
            
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            val secretKey = SecretKeySpec(key.take(16).padEnd(16, '0').toByteArray(), "AES")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val encryptedBytes = cipher.doFinal(data.toByteArray())
            val encryptedData = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
            
            Log.d(TAG, "✅ Data encrypted successfully")
            return encryptedData
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error encrypting data: ${e.message}")
            return null
        }
    }
    
    /**
     * Decrypt sensitive data from storage
     */
    fun decryptData(encryptedData: String, key: String = "BitcoinWidget2025"): String? {
        try {
            Log.d(TAG, "🔓 Decrypting sensitive data")
            
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            val secretKey = SecretKeySpec(key.take(16).padEnd(16, '0').toByteArray(), "AES")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            
            val encryptedBytes = Base64.decode(encryptedData, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            val decryptedData = String(decryptedBytes)
            
            Log.d(TAG, "✅ Data decrypted successfully")
            return decryptedData
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error decrypting data: ${e.message}")
            return null
        }
    }
    
    /**
     * Generate hash for data integrity verification
     */
    fun generateDataHash(data: String): String {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(data.toByteArray())
            return hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error generating data hash: ${e.message}")
            return ""
        }
    }
    
    /**
     * Verify data integrity using hash
     */
    fun verifyDataIntegrity(data: String, expectedHash: String): Boolean {
        val actualHash = generateDataHash(data)
        val isValid = actualHash == expectedHash
        
        if (isValid) {
            Log.d(TAG, "✅ Data integrity verification passed")
        } else {
            Log.w(TAG, "⚠️ Data integrity verification failed")
        }
        
        return isValid
    }
    
    /**
     * Validate Bitcoin price for reasonable ranges
     */
    fun validateBitcoinPrice(price: Double): Boolean {
        val isValid = price > 1000.0 && price < 10_000_000.0 // Reasonable range
        
        if (!isValid) {
            Log.w(TAG, "⚠️ Bitcoin price outside reasonable range: $$price")
        }
        
        return isValid
    }
    
    /**
     * Validate network fees for reasonable ranges
     */
    fun validateNetworkFees(fee: Int): Boolean {
        val isValid = fee > 0 && fee < 10000 // 0-10000 sat/vB range
        
        if (!isValid) {
            Log.w(TAG, "⚠️ Network fee outside reasonable range: $fee sat/vB")
        }
        
        return isValid
    }
    
    /**
     * Log security event for monitoring
     */
    fun logSecurityEvent(event: String, details: String = "") {
        Log.i(TAG, "🔒 SECURITY EVENT: $event ${if (details.isNotEmpty()) "- $details" else ""}")
        
        // In production, this could send to analytics/monitoring service
        // For now, we just log locally
    }
    
    /**
     * Check if app is running in secure environment
     */
    fun isSecureEnvironment(): Boolean {
        // Basic security checks
        try {
            // Check for debugging/emulator (basic detection)
            val isDebuggable = false // Could check BuildConfig.DEBUG
            val isEmulator = android.os.Build.FINGERPRINT.contains("generic")
            
            if (isEmulator) {
                Log.w(TAG, "⚠️ Running in emulator environment")
            }
            
            return !isEmulator // Return true if not emulator
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error checking secure environment: ${e.message}")
            return true // Default to secure if check fails
        }
    }
}
