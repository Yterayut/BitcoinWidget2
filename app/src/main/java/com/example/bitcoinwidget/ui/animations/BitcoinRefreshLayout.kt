package com.example.bitcoinwidget.ui.animations

import android.content.Context
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.core.content.ContextCompat

/**
 * BitcoinRefreshLayout - Custom SwipeRefreshLayout with Bitcoin-themed refresh animation
 * Provides a professional, branded refresh experience
 */
class BitcoinRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SwipeRefreshLayout(context, attrs) {
    
    private var isCustomRefreshEnabled = true
    
    init {
        setupBitcoinRefresh()
    }
    
    private fun setupBitcoinRefresh() {
        // Set Bitcoin-themed colors for the refresh indicator
        setColorSchemeColors(
            ContextCompat.getColor(context, android.R.color.holo_orange_light),
            ContextCompat.getColor(context, android.R.color.holo_orange_dark),
            ContextCompat.getColor(context, android.R.color.holo_orange_light),
            ContextCompat.getColor(context, android.R.color.white)
        )
        
        // Set the background color for the refresh circle
        setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(context, android.R.color.black)
        )
        
        // Configure refresh behavior  
        setDistanceToTriggerSync(150)
        setSlingshotDistance(200)
    }
    
    /**
     * Start custom refresh animation
     */
    fun startCustomRefresh() {
        if (!isRefreshing && isCustomRefreshEnabled) {
            isRefreshing = true
            // Additional custom animation can be added here
        }
    }
    
    /**
     * Stop custom refresh animation
     */
    fun stopCustomRefresh() {
        if (isRefreshing) {
            isRefreshing = false
        }
    }
    
    /**
     * Enable or disable custom refresh animations
     */
    fun setCustomRefreshEnabled(enabled: Boolean) {
        isCustomRefreshEnabled = enabled
    }
    
    /**
     * Set refresh listener with animation support
     */
    fun setOnBitcoinRefreshListener(listener: () -> Unit) {
        setOnRefreshListener {
            listener()
        }
    }
}
