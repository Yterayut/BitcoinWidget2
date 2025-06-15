package com.example.bitcoinwidget.ui.animations

import android.animation.*
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

/**
 * CardAnimator - Professional animation utilities for Bitcoin Widget
 * Provides smooth, engaging animations for cards, price updates, and user interactions
 */
class CardAnimator {
    
    companion object {
        private const val ANIMATION_DURATION_SHORT = 300L
        private const val ANIMATION_DURATION_MEDIUM = 500L
        private const val ANIMATION_DURATION_LONG = 800L
        private const val STAGGER_DELAY = 100L
        
        // Animation interpolators for different effects
        private val smoothInterpolator = AccelerateDecelerateInterpolator()
        private val bounceInterpolator = OvershootInterpolator(1.2f)
    }
    
    /**
     * Animate card entrance with staggered appearance
     * @param view The card view to animate
     * @param delay Delay before animation starts (for staggering)
     */
    fun animateCardEntrance(view: View, delay: Long = 0L) {
        // Initial state: invisible and slightly scaled down
        view.alpha = 0f
        view.scaleX = 0.95f
        view.scaleY = 0.95f
        view.translationY = 30f
        
        // Create animator set for smooth combined animation
        val animatorSet = AnimatorSet()
        
        val alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
        val scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f)
        val scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f)
        val translationAnimator = ObjectAnimator.ofFloat(view, "translationY", 30f, 0f)
        
        animatorSet.apply {
            playTogether(alphaAnimator, scaleXAnimator, scaleYAnimator, translationAnimator)
            duration = ANIMATION_DURATION_MEDIUM
            interpolator = bounceInterpolator
            startDelay = delay
        }
        
        animatorSet.start()
    }    
    /**
     * Animate price update with color flash and scale effect
     * @param textView The price TextView to animate
     * @param newPrice The new price to display
     * @param isIncrease Whether the price increased (for color coding)
     */
    fun animatePriceUpdate(textView: TextView, newPrice: String, isIncrease: Boolean? = null) {
        val context = textView.context
        
        // Determine colors based on price movement
        val flashColor = when (isIncrease) {
            true -> ContextCompat.getColor(context, android.R.color.holo_green_light)
            false -> ContextCompat.getColor(context, android.R.color.holo_red_light)
            else -> ContextCompat.getColor(context, android.R.color.white)
        }
        
        val originalColor = textView.currentTextColor
        
        // Create pulse animation
        val pulseAnimator = ObjectAnimator.ofFloat(textView, "scaleX", 1f, 1.05f, 1f)
        val pulseYAnimator = ObjectAnimator.ofFloat(textView, "scaleY", 1f, 1.05f, 1f)
        
        // Create color flash animation
        val colorAnimator = ValueAnimator.ofArgb(originalColor, flashColor, originalColor)
        colorAnimator.addUpdateListener { animator ->
            textView.setTextColor(animator.animatedValue as Int)
        }
        
        // Update text in the middle of animation
        val textUpdateAnimator = ValueAnimator.ofFloat(0f, 1f)
        textUpdateAnimator.addUpdateListener { animator ->
            if (animator.animatedFraction >= 0.5f && textView.text != newPrice) {
                textView.text = newPrice
            }
        }
        
        val animatorSet = AnimatorSet()
        animatorSet.apply {
            playTogether(pulseAnimator, pulseYAnimator, colorAnimator, textUpdateAnimator)
            duration = ANIMATION_DURATION_SHORT
            interpolator = smoothInterpolator
        }
        
        animatorSet.start()
    }    
    /**
     * Animate card press with elevation and scale feedback
     * @param cardView The CardView to animate
     */
    fun animateCardPress(cardView: CardView) {
        val originalElevation = cardView.cardElevation
        val pressedElevation = originalElevation * 1.5f
        
        // Scale down slightly on press
        val scaleDownX = ObjectAnimator.ofFloat(cardView, "scaleX", 1f, 0.98f)
        val scaleDownY = ObjectAnimator.ofFloat(cardView, "scaleY", 1f, 0.98f)
        val elevationUp = ObjectAnimator.ofFloat(cardView, "cardElevation", originalElevation, pressedElevation)
        
        val pressAnimatorSet = AnimatorSet()
        pressAnimatorSet.apply {
            playTogether(scaleDownX, scaleDownY, elevationUp)
            duration = 100L
            interpolator = smoothInterpolator
        }
        
        // Scale back up on release
        val scaleUpX = ObjectAnimator.ofFloat(cardView, "scaleX", 0.98f, 1f)
        val scaleUpY = ObjectAnimator.ofFloat(cardView, "scaleY", 0.98f, 1f)
        val elevationDown = ObjectAnimator.ofFloat(cardView, "cardElevation", pressedElevation, originalElevation)
        
        val releaseAnimatorSet = AnimatorSet()
        releaseAnimatorSet.apply {
            playTogether(scaleUpX, scaleUpY, elevationDown)
            duration = 150L
            interpolator = bounceInterpolator
        }
        
        // Chain the animations
        pressAnimatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                releaseAnimatorSet.start()
            }
        })
        
        pressAnimatorSet.start()
    }    
    /**
     * Animate loading state with shimmer effect
     * @param view The view to apply shimmer effect
     */
    fun animateLoadingShimmer(view: View) {
        val shimmerAnimator = ObjectAnimator.ofFloat(view, "alpha", 0.3f, 1f, 0.3f)
        shimmerAnimator.apply {
            duration = 1500L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = smoothInterpolator
        }
        
        shimmerAnimator.start()
        
        // Store animator reference for cleanup
        view.tag = shimmerAnimator
    }
    
    /**
     * Stop loading animation
     * @param view The view to stop shimmer effect
     */
    fun stopLoadingShimmer(view: View) {
        val animator = view.tag as? ObjectAnimator
        animator?.cancel()
        view.alpha = 1f
        view.tag = null
    }
    
    /**
     * Animate error state with shake effect
     * @param view The view to shake
     */
    fun animateErrorShake(view: View) {
        val shakeAnimator = ObjectAnimator.ofFloat(view, "translationX", 0f, -10f, 10f, -10f, 10f, 0f)
        shakeAnimator.apply {
            duration = ANIMATION_DURATION_SHORT
            interpolator = smoothInterpolator
        }
        
        shakeAnimator.start()
    }
    
    /**
     * Animate success state with gentle bounce
     * @param view The view to animate
     */
    fun animateSuccess(view: View) {
        val bounceAnimator = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f)
        val bounceYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f)
        
        val animatorSet = AnimatorSet()
        animatorSet.apply {
            playTogether(bounceAnimator, bounceYAnimator)
            duration = ANIMATION_DURATION_SHORT
            interpolator = bounceInterpolator
        }
        
        animatorSet.start()
    }
    
    /**
     * Animate multiple cards with staggered entrance
     * @param views List of views to animate
     */
    fun animateCardsSequential(views: List<View>) {
        views.forEachIndexed { index, view ->
            animateCardEntrance(view, index * STAGGER_DELAY)
        }
    }
}