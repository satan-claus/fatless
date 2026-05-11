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

    private val _dataLog = MutableStateFlow<List<String>>(emptyList())
    val dataLog = _dataLog.asStateFlow()

    private val _accelerometerData = MutableSharedFlow<Triple<Int, Int, Int>>()
    val accelerometerData = _accelerometerData.asSharedFlow()

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
        clearDataLog()
        resetConnectionState()

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

    fun resetConnectionState() {
        managerScope.launch {
            _connectionState.emit(0)
        }
    }

    @SuppressLint("MissingPermission")
    fun sendMagicKick() {
        val currentGatt = gatt ?: return
        logger.log(LogLevel.SYSTEM, "BLE", "Раздаю пендели...")

        managerScope.launch {
            currentGatt.services.forEach { service ->
                service.characteristics.forEach { char ->
                    // Если в характеристику можно писать (WRITE или WRITE_NO_RESPONSE)
                    if (char.properties and 0x08 != 0 || char.properties and 0x04 != 0) {
                        logger.log(LogLevel.DEBUG, "BLE", "Пендель в ${char.uuid.toString().take(8)}")
                        char.value = byteArrayOf(0x01)
                        currentGatt.writeCharacteristic(char)
                        delay(500) // Между ручными пенделями пауза поменьше
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun sendCustomKick(value: Byte) {
        // Используем 12b1 — она чаще всего бывает управляющей
        val charUuid = UUID.fromString("a0d812b1-515e-4596-b280-dfe639929552")
        val serviceUuid = UUID.fromString("a0d812b0-515e-4596-b280-dfe639929552")

        val char = gatt?.getService(serviceUuid)?.getCharacteristic(charUuid)
        if (char != null) {
            logger.log(LogLevel.SYSTEM, "BLE", "Пробую команду: ${String.format("%02X", value)}")
            char.value = byteArrayOf(value)
            gatt?.writeCharacteristic(char)
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        gatt?.disconnect()
    }

    fun clearDataLog() {
        managerScope.launch {
            _dataLog.value = emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    private fun closeGatt() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
    }

    @SuppressLint("MissingPermission")
    private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        // 1. Говорим Android-системе: "Слушай этот канал"
        gatt.setCharacteristicNotification(characteristic, true)

        // 2. Говорим железке: "Шли данные" (через дескриптор 2902)
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

                // ГАРАНТИРОВАННЫЙ ПРОБРОС ОШИБКИ
                managerScope.launch {
                    _connectionState.emit(0)  // Сначала сброс
                    _connectionState.emit(-1) // Потом ошибка
                }

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

            managerScope.launch {
                // 1. Только БАТАРЕЙКА (самый легкий запрос)
                gatt.getService(BATTERY_SERVICE_UUID)?.getCharacteristic(BATTERY_LEVEL_CHAR_UUID)?.let {
                    gatt.readCharacteristic(it)
                    delay(1000)
                }

                // 2. Только ПОДПИСКИ (слушаем, что датчик сам готов отдать)
                gatt.services.forEach { service ->
                    service.characteristics.forEach { char ->
                        if (char.properties and 0x10 != 0) { // NOTIFY
                            logger.log(LogLevel.SYSTEM, "BLE", "Слушаю канал: ${char.uuid.toString().take(8)}")
                            enableNotifications(gatt, char)
                            delay(1000)
                        }
                    }
                }
                logger.log(LogLevel.SYSTEM, "BLE", "Готов. Жду твоего пенделя.")
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                logger.log(LogLevel.DEBUG, "BLE", "Датчик подтвердил подписку на ${descriptor.characteristic.uuid.toString().take(8)}")
            } else {
                logger.log(LogLevel.ERROR, "BLE", "Датчик ОТКЛОНИЛ подписку: $status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val data = characteristic.value ?: return
            val uuid = characteristic.uuid.toString().take(8)

            // Формируем HEX и DEC (для первого байта)
            val hexString = data.joinToString(" ") { String.format("%02X", it) }
            val decValue = if (data.isNotEmpty()) data[0].toInt() and 0xFF else 0

            val logLine = "[$uuid] HEX: $hexString | DEC: $decValue"

            // Пишем в системный лог для истории
            logger.log(LogLevel.INFO, "BLE_DATA", logLine)

            // Обновляем UI-поток
            managerScope.launch {
                val currentList = _dataLog.value.toMutableList()
                currentList.add(0, logLine)
                _dataLog.value = currentList.take(30) // Держим побольше строк для сравнения
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
