package com.niked.fatless.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.R
import com.niked.fatless.domain.model.BleDevice
import com.niked.fatless.ui.MainActivity
import com.niked.fatless.ui.theme.AppBackground
import com.niked.fatless.ui.theme.AppPrimary
import com.niked.fatless.ui.theme.AppSecondary
import com.niked.fatless.ui.theme.AppSurface
import com.niked.fatless.ui.theme.AppTextPrimary
import com.niked.fatless.ui.theme.AppTextTertiary
import com.niked.fatless.ui.theme.AppTypography
import com.niked.fatless.ui.viewmodel.BleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleScanScreen(
    onBackClick: () -> Unit,
    viewModel: BleViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val devices by viewModel.devices.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val deviceData by viewModel.deviceData.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Реакция на ошибку подключения
    LaunchedEffect(connectionState) {
        if (connectionState == -1) {
            snackbarHostState.showSnackbar(
                message = "Ошибка подключения. Датчик недоступен.",
                duration = SnackbarDuration.Short
            )
        }
    }

    // Первичный запуск
    LaunchedEffect(Unit) {
        if (!viewModel.isBtEnabled()) {
            (context as? MainActivity)?.askToEnableBluetooth()
        }
        viewModel.startScan()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    val titleText = when (connectionState) {
                        2 -> "Подключено"
                        1 -> "Подключение..."
                        -1 -> "Ошибка"
                        else -> stringResource(id = R.string.ble_scan_title)
                    }
                    Text(text = titleText, style = AppTypography.titleMedium)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(size = 24.dp),
                            strokeWidth = 2.dp,
                            color = AppPrimary
                        )
                    } else {
                        IconButton(onClick = { viewModel.startScan() }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = padding)
                .background(color = AppBackground)
        ) {
            if (devices.isEmpty() && !isScanning) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(id = R.string.ble_scan_empty),
                        color = AppTextTertiary
                    )
                }
            }

            // Внутри Column, перед LazyColumn
            AnimatedVisibility(visible = connectionState == 2) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = Color.Black,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Live Data (Hex):",
                            color = AppSecondary,
                            style = AppTypography.labelSmall
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = deviceData,
                            color = Color.Green,
                            style = AppTypography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(all = 16.dp),
                verticalArrangement = Arrangement.spacedBy(space = 12.dp)
            ) {
                items(items = devices, key = { it.address }) { device ->
                    BleDeviceItem(
                        device = device,
                        onClick = { viewModel.connectToDevice(address = device.address) }
                    )
                }
            }
        }
    }
}

@Composable
fun BleDeviceItem(device: BleDevice, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = AppSurface,
        shape = RoundedCornerShape(size = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(size = 40.dp)
                    .background(color = AppSecondary.copy(alpha = 0.1f), shape = RoundedCornerShape(size = 10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sensors_24dp),
                    contentDescription = null,
                    tint = AppSecondary
                )
            }

            Spacer(modifier = Modifier.width(width = 16.dp))

            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(
                    text = device.name ?: stringResource(id = R.string.ble_scan_unknown),
                    style = AppTypography.bodyLarge,
                    color = AppTextPrimary
                )
                Text(
                    text = device.address,
                    style = AppTypography.labelSmall,
                    color = AppTextTertiary
                )
            }

            Text(
                text = "${device.rssi} dBm",
                style = AppTypography.bodySmall,
                color = if (device.rssi > -70) Color(color = 0xFF4CAF50) else AppTextTertiary
            )
        }
    }
}
