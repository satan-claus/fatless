package com.niked.fatless.domain.model

sealed class BleConnectionState {
    data object Idle : BleConnectionState()
    data object Connecting : BleConnectionState()
    data object Connected : BleConnectionState()
    data class Error(val message: String) : BleConnectionState()
}