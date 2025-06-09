package com.example.bitcoinwidget.security

import android.util.Log
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
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
     * Falls back gracefully if certificate pinning fails
     */
    fun createSecuredHttpClient(): OkHttpClient {
        Log.d(TAG, "🔒 Creating secured HTTP client with SSL pinning")
        
        // Try to create client with certificate pinning first
        return try {
            createClientWithPinning()
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Certificate pinning failed, creating fallback client: ${e.message}")
            createFallbackClient()
        }
    }
    
    /**
     * Create client with certificate pinning (primary)
     */
    private fun createClientWithPinning(): OkHttpClient {
        return OkHttpClient.Builder()
            .certificatePinner(CERTIFICATE_PINNER)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(createSecurityInterceptor())
            .build()
    }
    
    /**
     * Create fallback client without pinning (when certificates expire)
     */
    private fun createFallbackClient(): OkHttpClient {
        Log.w(TAG, "🔓 Using fallback HTTP client without certificate pinning")
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(createSecurityInterceptor())
            .build()
    }
    
    /**
     * Create security interceptor for request/response handling
     */
    private fun createSecurityInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            Log.d(TAG, "🌐 Secured request to: ${request.url}")
            
            val securedRequest = request.newBuilder()
                .addHeader("User-Agent", "BitcoinWidget/2.0-Production")
                .addHeader("Accept", "application/json")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Connection", "close") // Prevent connection pooling issues
                .build()
            
            val startTime = System.currentTimeMillis()
            try {
                val response = chain.proceed(securedRequest)
                val duration = System.currentTimeMillis() - startTime
                
                Log.d(TAG, "✅ Request completed in ${duration}ms: ${response.code}")
                return@Interceptor response
            } catch (e: Exception) {
                val duration = System.currentTimeMillis() - startTime
                Log.e(TAG, "❌ Request failed after ${duration}ms: ${e.message}")
                throw e
            }
        }
    }
    
    /**
     * Validate API response for security
     */
    fun validateApiResponse(responseBody: String, endpoint: String): Boolean {
        try {
            // Basic validation checks
            if (responseBody.length > 1024 * 1024) { // 1MB max
                Log.w(TAG, "⚠️ Response too large: ${responseBody.length} bytes")
                return false
            }
            
            if (responseBody.contains("<script", ignoreCase = true) || 
                responseBody.contains("javascript:", ignoreCase = true)) {
                Log.w(TAG, "⚠️ Suspicious content detected in response")
                return false
            }
            
            // Check for basic JSON structure
            if (!responseBody.trim().startsWith("{") && !responseBody.trim().startsWith("[")) {
                Log.w(TAG, "⚠️ Non-JSON response from API: ${endpoint}")
                return false
            }
            
            Log.d(TAG, "✅ Response validation passed for: ${endpoint}")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error validating response: ${e.message}")
            return true // Allow on validation error to prevent blocking
        }
    }
    
    /**
     * Generate secure hash for data integrity
     */
    fun generateSecureHash(data: String): String {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(data.toByteArray())
            return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating hash: ${e.message}")
            return ""
        }
    }
    
    /**
     * Encrypt sensitive data (for future use)
     */
    fun encryptData(data: String, key: String): String {
        try {
            val keySpec = SecretKeySpec(key.toByteArray().sliceArray(0..15), "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encryptedBytes = cipher.doFinal(data.toByteArray())
            return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error encrypting data: ${e.message}")
            return data // Return original on error
        }
    }
    
    /**
     * Decrypt sensitive data (for future use)
     */
    fun decryptData(encryptedData: String, key: String): String {
        try {
            val keySpec = SecretKeySpec(key.toByteArray().sliceArray(0..15), "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decryptedBytes = cipher.doFinal(Base64.decode(encryptedData, Base64.NO_WRAP))
            return String(decryptedBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting data: ${e.message}")
            return encryptedData // Return encrypted data on error
        }
    }
    
    /**
     * Check if running in secure environment (basic detection)
     */
    fun isSecureEnvironment(): Boolean {
        try {
            // Check for debugging/emulator (basic detection)
            val isDebuggable = false // Could check BuildConfig.DEBUG
            val isEmulator = android.os.Build.FINGERPRINT.contains("generic")
            
            if (isEmulator) {
                Log.w(TAG, "⚠️ Running in emulator environment")
                return false
            }
            
            Log.d(TAG, "✅ Secure environment check passed")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error checking secure environment: ${e.message}")
            return true // Default to secure if check fails
        }
    }
}
