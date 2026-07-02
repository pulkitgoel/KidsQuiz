package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.R

enum class Sfx {
    TAP, CORRECT, WRONG, COMBO, ON_FIRE, FANFARE, TICK, HEART_BREAK, MILESTONE, RESCUE
}

/**
 * SoundPool wrapper for the game sound effects. Held as an app-scoped
 * singleton so it survives configuration changes; sounds are eagerly loaded
 * and play() is gated on load completion so an early tap fails silently
 * instead of glitching.
 */
class SoundManager private constructor(context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val loadedIds = mutableSetOf<Int>()
    private val soundIds: Map<Sfx, Int>

    var muted: Boolean = false

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) synchronized(loadedIds) { loadedIds.add(sampleId) }
        }
        soundIds = mapOf(
            Sfx.TAP to soundPool.load(context, R.raw.sfx_tap, 1),
            Sfx.CORRECT to soundPool.load(context, R.raw.sfx_correct, 1),
            Sfx.WRONG to soundPool.load(context, R.raw.sfx_wrong, 1),
            Sfx.COMBO to soundPool.load(context, R.raw.sfx_combo, 1),
            Sfx.ON_FIRE to soundPool.load(context, R.raw.sfx_on_fire, 1),
            Sfx.FANFARE to soundPool.load(context, R.raw.sfx_fanfare, 1),
            Sfx.TICK to soundPool.load(context, R.raw.sfx_tick, 1),
            Sfx.HEART_BREAK to soundPool.load(context, R.raw.sfx_heart_break, 1),
            Sfx.MILESTONE to soundPool.load(context, R.raw.sfx_milestone, 1),
            Sfx.RESCUE to soundPool.load(context, R.raw.sfx_rescue, 1),
        )
    }

    /** @param rate playback rate 0.5–2.0; used to pitch combo dings upward. */
    fun play(sfx: Sfx, rate: Float = 1f, volume: Float = 1f) {
        if (muted) return
        val id = soundIds[sfx] ?: return
        val loaded = synchronized(loadedIds) { id in loadedIds }
        if (loaded) {
            soundPool.play(id, volume, volume, 1, 0, rate.coerceIn(0.5f, 2f))
        }
    }

    /** Rising pitch for consecutive-correct answers. */
    fun correctPitchFor(combo: Int): Float = 1.0f + 0.08f * minOf(combo, 6)

    fun release() {
        soundPool.release()
    }

    companion object {
        @Volatile
        private var instance: SoundManager? = null

        fun get(context: Context): SoundManager =
            instance ?: synchronized(this) {
                instance ?: SoundManager(context.applicationContext).also { instance = it }
            }
    }
}
