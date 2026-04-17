package com.niked.fatless.data.player

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.niked.fatless.R
import com.niked.fatless.domain.player.IAudioPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidAudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) : IAudioPlayer {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // Загружаем ID звуков (файлы должны лежать в res/raw/tick.mp3 и т.д.)
    // Для Android лучше всего подходят форматы .mp3, .wav или .ogg
    /**
     * Имена файлов: Только строчные буквы (a-z), цифры (0-9) и подчеркивания.
     * Правильно: tick.mp3, next.wav, finish.ogg
     * Ошибка: Tick.mp3, next-sound.mp3
     * Длительность:
     * tick: 0.1 – 0.3 сек (короткий щелчок).
     * next: 0.5 – 1.0 сек (звонкий сигнал).
     * finish: 1.5 – 3.0 сек (финальный гонг или мелодия)
     */
    // Храним ID загруженных звуков
    private var tickId: Int = 0
    private var nextId: Int = 0
    private var finishId: Int = 0

    init {
        // Загружаем звуки из папки res/raw
        tickId = soundPool.load(context, R.raw.next, 1)
        nextId = soundPool.load(context, R.raw.finish, 1)
        finishId = soundPool.load(context, R.raw.finish, 1)
    }

    override fun playTick() {
        if (tickId != 0) soundPool.play(tickId, 1f, 1f, 1, 0, 1f)
    }

    override fun playNext() {
        if (nextId != 0) soundPool.play(nextId, 1f, 1f, 1, 0, 1f)
    }

    override fun playFinish() {
        if (finishId != 0) soundPool.play(finishId, 1f, 1f, 1, 0, 1f)
    }

    override fun release() {
        soundPool.release()
    }
}
