package com.niked.fatless.core.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import com.niked.fatless.core.utils.AppLogger
import com.niked.fatless.core.utils.Constants.LogLevel
import com.niked.fatless.domain.model.BleDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private var gatt: BluetoothGatt? = null
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Поток для уведомлений о статусе подключения (для UI)
    private val _connectionState = MutableSharedFlow<Int>()
    val connectionState = _connectionState.asSharedFlow()

    fun isBtEnabled(): Boolean = adapter?.isEnabled == true

    /**
     * Сканирование устройств
     */
    @SuppressLint("MissingPermission")
    fun startScanning(): Flow<BleDevice> = callbackFlow {
        if (adapter == null || !adapter.isEnabled) {
            logger.log(LogLevel.ERROR, "BLE", "Bluetooth выключен")
            close()
            return@callbackFlow
        }

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = BleDevice(
                    name = result.device.name,
                    address = result.device.address,
                    rssi = result.rssi
                )
                launch { send(device) }
            }
        }

        logger.log(LogLevel.SYSTEM, "BLE", "Запуск сканера")
        scanner?.startScan(callback)

        awaitClose {
            logger.log(LogLevel.SYSTEM, "BLE", "Остановка сканера")
            scanner?.stopScan(callback)
        }
    }

    /**
     * Подключение к устройству
     */
    @SuppressLint("MissingPermission")
    fun connect(address: String) {
        val device = adapter?.getRemoteDevice(address)
        if (device == null) {
            logger.log(LogLevel.ERROR, "BLE", "Устройство не найдено: $address")
            return
        }

        // Закрываем старое соединение, если оно есть
        closeGatt()

        logger.log(LogLevel.SYSTEM, "BLE", "Попытка подключения к ${device.name ?: address}")
        gatt = device.connectGatt(context, false, gattCallback)
    }

    /**
     * Отключение
     */
    @SuppressLint("MissingPermission")
    fun disconnect() {
        gatt?.disconnect()
        logger.log(LogLevel.SYSTEM, "BLE", "Запрошено отключение")
    }

    @SuppressLint("MissingPermission")
    private fun closeGatt() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
    }

    /**
     * Коллбэк обработки событий GATT
     */
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            managerScope.launch {
                _connectionState.emit(newState)
            }

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    logger.log(LogLevel.INFO, "BLE", "Подключено к ${gatt.device.address}")
                    logger.log(LogLevel.SYSTEM, "BLE", "Запуск поиска сервисов...")
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    logger.log(LogLevel.INFO, "BLE", "Связь разорвана")
                    closeGatt()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                logger.log(LogLevel.INFO, "BLE", "Сервисы устройства обнаружены")
                // Список UUID сервисов для лога (чтобы понять, что датчик умеет)
                gatt.services.forEach { service ->
                    logger.log(LogLevel.DEBUG, "BLE", "Найден сервис: ${service.uuid}")
                }
            } else {
                logger.log(LogLevel.ERROR, "BLE", "Ошибка поиска сервисов: $status")
            }
        }
    }
}
