package com.niked.fatless.domain.player

interface IAudioPlayer {
    fun playTick()   // Пик-пик
    fun playNext()   // Смена интервала
    fun playFinish() // Победный гонг
    fun release()    // Очистка памяти
}