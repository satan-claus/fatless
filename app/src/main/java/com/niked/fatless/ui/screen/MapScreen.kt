package com.niked.fatless.ui.screen

import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.R
import com.niked.fatless.domain.model.ActivityType
import com.niked.fatless.ui.theme.AppBackground
import com.niked.fatless.ui.theme.AppSurface
import com.niked.fatless.ui.theme.AppTypography
import com.niked.fatless.ui.viewmodel.MapViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBackClick: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val segments by viewModel.trackSegments.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.map_title), style = AppTypography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppSurface
                )
            )
        }
    ) { paddingValues ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppBackground),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    // Скрываем стандартные кнопки зума, Compose-ом потом свои сделаем если надо
                    zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
                    controller.setZoom(17.0)
                }
            },
            update = { mapView ->
                mapView.overlays.clear()

                if (segments.isNotEmpty()) {
                    // 1. Центрируем карту
                    segments.first().points.firstOrNull()?.let { firstPoint ->
                        mapView.controller.setCenter(firstPoint)
                    }

                    // 2. Рисуем сегменты трека
                    segments.forEach { segment ->
                        if (segment.points.size >= 2 && segment.type != ActivityType.STAY) {
                            val polyline = Polyline(mapView).apply {
                                setPoints(segment.points)

                                // СГЛАЖИВАНИЕ И ВИЗУАЛ
                                outlinePaint.apply {
                                    // Добавляем прозрачность (0.8f), чтобы линии не "давили"
                                    color = segment.type.color.copy(alpha = 0.8f).toArgb()
                                    strokeWidth = segment.type.strokeWidth

                                    // Скругляем концы и соединения (убирает острые углы)
                                    strokeCap = Paint.Cap.ROUND
                                    strokeJoin = Paint.Join.ROUND
                                    // Включаем антиалиасинг
                                    isAntiAlias = true
                                }

                                isGeodesic = true
                            }
                            mapView.overlays.add(polyline)
                        }
                    }

                    // 3. ТОЧКА СТАРТА (Первая точка первого сегмента)
                    segments.first().points.firstOrNull()?.let { startPoint ->
                        val startMarker = Marker(mapView).apply {
                            position = startPoint
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            // Рисуем кружок программно, чтобы не плодить drawable
                            icon = GradientDrawable().apply {
                                shape = GradientDrawable.OVAL
                                setSize(45, 45)
                                setColor(android.graphics.Color.GREEN)
                                setStroke(5, android.graphics.Color.WHITE)
                            }
                            title = context.getString(R.string.map_marker_start)
                        }
                        mapView.overlays.add(startMarker)
                    }

                    // 4. ТОЧКА ФИНИША (Последняя точка последнего сегмента)
                    segments.last().points.lastOrNull()?.let { endPoint ->
                        val endMarker = Marker(mapView).apply {
                            position = endPoint
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            icon = GradientDrawable().apply {
                                shape = GradientDrawable.OVAL
                                setSize(45, 45)
                                setColor(android.graphics.Color.RED)
                                setStroke(5, android.graphics.Color.WHITE)
                            }
                            title = context.getString(R.string.map_marker_finish)
                        }
                        mapView.overlays.add(endMarker)
                    }
                }
                mapView.invalidate()
            }
        )
    }
}
