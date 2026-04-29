package com.niked.fatless.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.niked.fatless.R
import com.niked.fatless.core.data.AppSettings
import com.niked.fatless.domain.player.IAudioPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidAudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: AppSettings
) : IAudioPlayer {

    private var soundPool: SoundPool? = null
    private var tickId = 0
    private var nextId = 0
    private var finishId = 0

    init {
        setupSoundPool()
    }

    // 🎯 Метод для внешнего контроля из Активити
    fun checkAndInit() {
        if (soundPool == null) {
            Log.d("AUDIO_PLAYER", "Воскрешаю плеер в onResume")
            setupSoundPool()
        }
    }

    private fun setupSoundPool() {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(attrs)
            .build()

        // Грузим твои кастомные назначения
        tickId = soundPool?.load(context, R.raw.next, 1) ?: 0
        nextId = soundPool?.load(context, R.raw.finish, 1) ?: 0
        finishId = soundPool?.load(context, R.raw.finish, 1) ?: 0
    }

    private fun play(id: Int) {
        if (!settings.isSoundEnabled || id == 0) return

        // На всякий случай: если почему-то пуст - инициализируем
        if (soundPool == null) setupSoundPool()

        val vol = settings.soundVolume
        if (vol < 0.01f) return

        soundPool?.play(id, vol, vol, 1, 0, 1f)
    }

    override fun playTick() = play(tickId)
    override fun playNext() = play(nextId)
    override fun playFinish() = play(finishId)

    override fun release() {
        Log.d("AUDIO_PLAYER", "Освобождаю ресурсы плеера")
        soundPool?.release()
        soundPool = null
    }
}
