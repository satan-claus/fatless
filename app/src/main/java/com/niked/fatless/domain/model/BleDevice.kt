package com.niked.fatless.domain.model

/**
 * Доменная модель Bluetooth устройства
 * @property name Имя устройства (может быть null, если девайс не отдает имя сразу)
 * @property address MAC-адрес (уникальный идентификатор, например: 00:11:22:33:FF:EE)
 * @property rssi Уровень сигнала (Received Signal Strength Indicator). Чем ближе к 0, тем лучше связь.
 */
data class BleDevice(
    val name: String?,
    val address: String,
    val rssi: Int
)
