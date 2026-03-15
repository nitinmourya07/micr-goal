package com.focus3.app.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Haptic Feedback Utility
 * Provides smooth, premium haptic feedback for key interactions
 */
object HapticUtils {

    private enum class HapticPreset {
        TICK,
        CLICK,
        HEAVY_CLICK,
        DOUBLE_CLICK
    }
    
    /**
     * Light tap - for button clicks, toggles
     */
    fun lightTap(context: Context) {
        vibrate(context, 10L, HapticPreset.TICK)
    }
    
    /**
     * Medium tap - for completing tasks
     */
    fun mediumTap(context: Context) {
        vibrate(context, 20L, HapticPreset.CLICK)
    }
    
    /**
     * Strong tap - for important actions like goal completion
     */
    fun strongTap(context: Context) {
        vibrate(context, 30L, HapticPreset.HEAVY_CLICK)
    }
    
    /**
     * Success pattern - for completing all goals
     */
    fun successPattern(context: Context) {
        val pattern = longArrayOf(0, 50, 50, 50, 50, 100)
        vibratePattern(context, pattern)
    }
    
    /**
     * Double tap - for confirmations
     */
    fun doubleTap(context: Context) {
        val pattern = longArrayOf(0, 20, 80, 20)
        vibratePattern(context, pattern)
    }
    
    /**
     * Error/Warning - for invalid actions
     */
    fun errorTap(context: Context) {
        vibrate(context, 50L, HapticPreset.DOUBLE_CLICK)
    }

    private fun vibrate(context: Context, duration: Long, preset: HapticPreset) {
        try {
            val vibrator = getVibrator(context) ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = when (preset) {
                    HapticPreset.TICK -> VibrationEffect.EFFECT_TICK
                    HapticPreset.CLICK -> VibrationEffect.EFFECT_CLICK
                    HapticPreset.HEAVY_CLICK -> VibrationEffect.EFFECT_HEAVY_CLICK
                    HapticPreset.DOUBLE_CLICK -> VibrationEffect.EFFECT_DOUBLE_CLICK
                }
                vibrator.vibrate(VibrationEffect.createPredefined(effect))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        } catch (e: Exception) {
            // Silently fail if vibration not available
        }
    }
    
    private fun vibratePattern(context: Context, pattern: LongArray) {
        try {
            val vibrator = getVibrator(context) ?: return
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            // Silently fail
        }
    }
    
    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
    
    /**
     * Use View's built-in haptic feedback (most efficient)
     */
    fun performHapticFeedback(view: View, feedbackType: Int = HapticFeedbackConstants.CONTEXT_CLICK) {
        view.performHapticFeedback(feedbackType)
    }
}
