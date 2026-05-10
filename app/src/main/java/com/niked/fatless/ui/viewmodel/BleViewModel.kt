package com.niked.fatless.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.core.bluetooth.BleManager
import com.niked.fatless.domain.model.BleDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class BleViewModel @Inject constructor(
    private val bleManager: BleManager
) : ViewModel() {

    private val _devices = MutableStateFlow<List<BleDevice>>(emptyList())
    val devices: StateFlow<List<BleDevice>> = _devices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    // Ссылка на текущую работу сканера для её остановки
    private var scanJob: Job? = null

    // Статус подключения (0 - откл, 1 - коннект, 2 - подкл)
    val connectionState: StateFlow<Int> = bleManager.connectionState
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    // Пробрасываем поток на экран
    val deviceData = bleManager.lastData.stateIn(viewModelScope, SharingStarted.Lazily, "")

    fun startScan() {
        if (_isScanning.value) return

        _devices.value = emptyList()
        _isScanning.value = true

        // Сохраняем Job, чтобы иметь над ним контроль
        scanJob = bleManager.startScanning()
            .onEach { newDevice ->
                val currentList = _devices.value.toMutableList()
                val existingIndex = currentList.indexOfFirst { it.address == newDevice.address }

                if (existingIndex != -1) {
                    currentList[existingIndex] = newDevice
                } else {
                    currentList.add(newDevice)
                }
                _devices.value = currentList.sortedByDescending { it.rssi }
            }
            .launchIn(viewModelScope)
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null
        _isScanning.value = false
    }

    fun connectToDevice(address: String) {
        // BLE требует тишины в эфире при коннекте
        stopScan()
        bleManager.connect(address)
    }

    fun disconnect() {
        bleManager.disconnect()
    }

    fun isBtEnabled() = bleManager.isBtEnabled()

    override fun onCleared() {
        super.onCleared()
        // Подстраховка при закрытии экрана
        stopScan()
    }
}

