package com.niked.fatless.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niked.fatless.core.bluetooth.BleManager
import com.niked.fatless.domain.model.BleDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private var scanJob: Job? = null

    fun isBtEnabled(): Boolean {
        return bleManager.isBtEnabled()
    }

    fun startScan() {
        if (_isScanning.value) return

        _devices.value = emptyList()
        _isScanning.value = true

        scanJob = bleManager.startScanning()
            .onEach { newDevice ->
                val currentList = _devices.value.toMutableList()
                val existingIndex = currentList.indexOfFirst { it.address == newDevice.address }

                if (existingIndex != -1) {
                    // ОБНОВЛЯЕМ уровень сигнала у уже найденного
                    currentList[existingIndex] = newDevice
                } else {
                    // Добавляем новое
                    currentList.add(newDevice)
                }

                // СОРТИРУЕМ: самые мощные (близкие) — сверху
                _devices.value = currentList.sortedByDescending { it.rssi }
            }
            .launchIn(viewModelScope)
    }

    fun stopScan() {
        scanJob?.cancel()
        _isScanning.value = false
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
    }
}
