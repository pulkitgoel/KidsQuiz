package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Vibration helpers for game feedback. Compose's LocalHapticFeedback only
 * exposes LongPress/TextHandleMove, so we drive the Vibrator directly.
 * minSdk 24 needs the deprecated vibrate() overloads below API 26.
 */
class HapticsManager private constructor(context: Context) {

    private val vibrator: Vibrator? = run {
        val v = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (v?.hasVibrator() == true) v else null
    }

    /** Light 20ms tick — taps, correct answers, score roll-up. */
    fun tick() = oneShot(20, 60)

    /** Double pulse — correct answer confirmation. */
    fun success() = waveform(longArrayOf(0, 30, 50, 30), intArrayOf(0, 120, 0, 180))

    /** Strong 180ms buzz — wrong answer. */
    fun error() = oneShot(180, 255)

    /** Rising pulse train scaled by combo tier (2, 3, 5...). */
    fun combo(tier: Int) {
        val pulses = tier.coerceIn(2, 5)
        val timings = ArrayList<Long>(pulses * 2)
        val amplitudes = ArrayList<Int>(pulses * 2)
        timings.add(0); amplitudes.add(0)
        for (i in 1..pulses) {
            timings.add(35L); amplitudes.add((100 + i * 30).coerceAtMost(255))
            if (i < pulses) { timings.add(45L); amplitudes.add(0) }
        }
        waveform(timings.toLongArray(), amplitudes.toIntArray())
    }

    /** Celebration rumble — results, milestones. */
    fun celebrate() = waveform(longArrayOf(0, 60, 40, 60, 40, 120), intArrayOf(0, 140, 0, 180, 0, 255))

    private fun oneShot(millis: Long, amplitude: Int) {
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(millis, amplitude))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(millis)
        }
    }

    private fun waveform(timings: LongArray, amplitudes: IntArray) {
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(timings, -1)
        }
    }

    companion object {
        @Volatile
        private var instance: HapticsManager? = null

        fun get(context: Context): HapticsManager =
            instance ?: synchronized(this) {
                instance ?: HapticsManager(context.applicationContext).also { instance = it }
            }
    }
}
