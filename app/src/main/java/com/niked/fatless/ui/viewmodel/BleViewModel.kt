package com.niked.fatless.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.core.bluetooth.BleManager
import com.niked.fatless.domain.model.BleDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class BleViewModel @Inject constructor(
    private val bleManager: BleManager
) : ViewModel() {

    private val _devices = MutableStateFlow<List<BleDevice>>(emptyList())
    val devices = _devices.asStateFlow()

    fun startScan() {
        // Очищаем список перед новым поиском
        _devices.value = emptyList()

        bleManager.startScanning()
            .onEach { newDevice ->
                // Добавляем только уникальные устройства по MAC-адресу
                if (_devices.value.none { it.address == newDevice.address }) {
                    _devices.value = _devices.value + newDevice
                }
            }
            .launchIn(viewModelScope)
    }
}
