package com.niked.fatless.data.player

import android.content.Context
import android.util.Log
import com.niked.fatless.domain.player.IAudioPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidIAudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) : IAudioPlayer {
    override fun playTick() { Log.d("AUDIO", "TICK") }
    override fun playNext() { Log.d("AUDIO", "NEXT") }
    override fun playFinish() { Log.d("AUDIO", "FINISH") }
    override fun release() { }
}