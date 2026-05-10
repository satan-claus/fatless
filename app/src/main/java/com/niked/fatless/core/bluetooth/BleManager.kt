package com.niked.fatless.core.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import com.niked.fatless.core.utils.AppLogger
import com.niked.fatless.core.utils.Constants.LogLevel
import com.niked.fatless.domain.model.BleDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleManager @Inject constructor(
    private val context: Context,
    private val logger: AppLogger
) {
    // Стандартные UUID для работы с BLE датчиками
    private val BATTERY_SERVICE_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
    private val BATTERY_LEVEL_CHAR_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")
    private val HEART_RATE_SERVICE_UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
    private val CLIENT_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter
    private val scanner = adapter?.bluetoothLeScanner

    private var gatt: BluetoothGatt? = null
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _connectionState = MutableSharedFlow<Int>()
    val connectionState = _connectionState.asSharedFlow()

    fun isBtEnabled(): Boolean = adapter?.isEnabled == true

    private val _lastData = MutableStateFlow("")
    val lastData = _lastData.asStateFlow()

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
        scanner?.startScan(callback)
        awaitClose { scanner?.stopScan(callback) }
    }

    @SuppressLint("MissingPermission")
    fun connect(address: String) {
        val device = adapter?.getRemoteDevice(address) ?: return
        closeGatt()

        managerScope.launch {
            delay(500) // Даем время на остановку сканера
            logger.log(LogLevel.SYSTEM, "BLE", "Подключение к ${device.address}")

            gatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            } else {
                device.connectGatt(context, false, gattCallback)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        gatt?.disconnect()
    }

    @SuppressLint("MissingPermission")
    private fun closeGatt() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
    }

    @SuppressLint("MissingPermission")
    private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor = characteristic.getDescriptor(CLIENT_CONFIG_UUID)
        if (descriptor != null) {
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            managerScope.launch { _connectionState.emit(newState) }

            if (status != BluetoothGatt.GATT_SUCCESS) {
                logger.log(LogLevel.ERROR, "BLE", "Ошибка GATT: $status")
                closeGatt()
                return
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                logger.log(LogLevel.INFO, "BLE", "Связь установлена. Поиск сервисов...")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                logger.log(LogLevel.INFO, "BLE", "Отключено")
                closeGatt()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) return

            logger.log(LogLevel.INFO, "BLE", "Глубокий анализ WSH...")

            gatt.services.forEach { service ->
                logger.log(LogLevel.DEBUG, "BLE", "--- Сервис: ${service.uuid}")

                service.characteristics.forEach { char ->
                    val props = mutableListOf<String>()
                    if (char.properties and 0x02 != 0) props.add("READ")
                    if (char.properties and 0x10 != 0) props.add("NOTIFY")

                    logger.log(LogLevel.DEBUG, "BLE", "  |> Хар-ка: ${char.uuid} [${props.joinToString("|")}]")

                    // ЕСЛИ ЕСТЬ NOTIFY — ПОДПИСЫВАЕМСЯ НА ВСЁ ПОДРЯД
                    if (char.properties and 0x10 != 0) {
                        logger.log(LogLevel.SYSTEM, "BLE", "Подписываюсь на ${char.uuid}")
                        enableNotifications(gatt, char)
                    }

                    // Если есть READ и это не батарейка (её уже читали) — тоже пробуем
                    if (char.properties and 0x02 != 0 && char.uuid != BATTERY_LEVEL_CHAR_UUID) {
                        gatt.readCharacteristic(char)
                    }
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val data = characteristic.value
            if (data != null) {
                val hexString = data.joinToString(" ") { String.format("%02X", it) }

                // Обновляем поток для UI
                managerScope.launch {
                    _lastData.emit(hexString)
                }

                logger.log(LogLevel.INFO, "BLE", "ДАННЫЕ [${characteristic.uuid.toString().take(8)}]: $hexString")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (characteristic.uuid) {
                    BATTERY_LEVEL_CHAR_UUID -> {
                        val value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                        logger.log(LogLevel.INFO, "BLE", "Заряд батареи устройства: $value%")
                    }
                }
            }
        }
    }
}
