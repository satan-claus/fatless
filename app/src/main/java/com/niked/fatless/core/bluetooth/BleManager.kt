package com.niked.fatless.core.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import com.niked.fatless.core.utils.AppLogger
import com.niked.fatless.core.utils.Constants.LogLevel
import com.niked.fatless.domain.model.BleDevice
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleManager @Inject constructor(
    private val context: Context,
    private val logger: AppLogger
) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter
    private val scanner = adapter?.bluetoothLeScanner

    /**
     * Запуск сканирования и возврат потока найденных устройств
     */
    @SuppressLint("MissingPermission")
    fun startScanning(): Flow<BleDevice> = callbackFlow {
        if (adapter == null || !adapter.isEnabled) {
            logger.log(LogLevel.ERROR, "BLE", "Bluetooth выключен или не поддерживается")
            close()
            return@callbackFlow
        }

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                // Маппим системный результат в нашу доменную модель
                val device = BleDevice(
                    name = result.device.name,
                    address = result.device.address,
                    rssi = result.rssi
                )
                // Отправляем в поток
                launch { send(device) }
            }

            override fun onScanFailed(errorCode: Int) {
                logger.log(LogLevel.ERROR, "BLE", "Ошибка сканирования: $errorCode")
                close()
            }
        }

        logger.log(LogLevel.SYSTEM, "BLE", "Запуск сканера...")
        scanner?.startScan(callback)

        // Ждем закрытия потока (например, когда юзер ушел с экрана)
        awaitClose {
            logger.log(LogLevel.SYSTEM, "BLE", "Остановка сканера")
            scanner?.stopScan(callback)
        }
    }
}
