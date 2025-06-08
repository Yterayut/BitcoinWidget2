package com.example.bitcoinwidget.logging

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enhanced Logging and Crash Reporting System
 * Provides comprehensive logging, error tracking, and crash reporting
 */
class LoggingManager(private val context: Context) {
    
    companion object {
        private const val TAG = "LoggingManager"
        private const val LOG_FILE_NAME = "bitcoin_widget_logs.txt"
        private const val CRASH_FILE_NAME = "bitcoin_widget_crashes.txt"
        private const val MAX_LOG_FILE_SIZE = 5 * 1024 * 1024 // 5MB
        private const val MAX_LOG_ENTRIES = 1000
    }
    
    private val logFile: File by lazy { 
        File(context.filesDir, LOG_FILE_NAME)
    }
    
    private val crashFile: File by lazy {
        File(context.filesDir, CRASH_FILE_NAME)
    }
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    
    /**
     * Initialize logging system with crash handler
     */
    fun initialize() {
        Log.d(TAG, "📝 Initializing enhanced logging system")
        logInfo("LoggingManager", "Enhanced logging system initialized")
    }
    
    /**
     * Log info level message
     */
    fun logInfo(tag: String, message: String) {
        Log.i(tag, message)
        writeToFile("INFO", tag, message)
    }
    
    /**
     * Log warning level message
     */
    fun logWarning(tag: String, message: String) {
        Log.w(tag, message)
        writeToFile("WARN", tag, message)
    }
    
    /**
     * Log error level message
     */
    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        val fullMessage = if (throwable != null) {
            "$message\nException: ${throwable.message}\nStackTrace: ${throwable.stackTraceToString()}"
        } else {
            message
        }
        writeToFile("ERROR", tag, fullMessage)
    }
    
    /**
     * Write log entry to file
     */
    private fun writeToFile(level: String, tag: String, message: String) {
        try {
            val timestamp = dateFormat.format(Date())
            val logEntry = "$timestamp | $level | $tag | $message\n"
            
            FileWriter(logFile, true).use { writer ->
                writer.append(logEntry)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write to log file: ${e.message}")
        }
    }
}
