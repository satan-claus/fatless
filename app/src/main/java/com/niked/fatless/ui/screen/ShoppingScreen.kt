package com.niked.fatless.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.niked.fatless.R
import com.niked.fatless.domain.model.Shop
import com.niked.fatless.domain.model.ShoppingItem
import com.niked.fatless.ui.component.WorkoutTopBar
import com.niked.fatless.ui.viewmodel.ShoppingViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(
    onBackClick: () -> Unit,
    viewModel: ShoppingViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf(
        stringResource(R.string.tab_title_products),
        stringResource(R.string.tab_title_shops)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            WorkoutTopBar(
                title = stringResource(R.string.shopping_screen_title),
                subTitle = stringResource(R.string.shopping_screen_subtitle),
                onBackClick = onBackClick,
                actions = {}
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(text = title, style = MaterialTheme.typography.titleSmall) }
                    )
                }
            }

            when (selectedTab) {
                0 -> ProductsTabContent(viewModel = viewModel)
                1 -> ShopsTabContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun ProductsTabContent(viewModel: ShoppingViewModel) {
    val items by viewModel.shoppingItems.collectAsState()

    if (items.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.shopping_list_empty), color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(items = items, key = { it.id }) { item ->
                ShoppingItemRow(
                    item = item,
                    onCheckedChange = { viewModel.toggleItemCompletion(item) },
                    onDeleteClick = { viewModel.deleteItem(item) }
                )
            }
        }
    }
}

@Composable
private fun ShopsTabContent(viewModel: ShoppingViewModel) {
    val shops by viewModel.shopItems.collectAsState()

    if (shops.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.shops_list_empty), color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(items = shops, key = { it.id }) { shop ->
                ShopItemRow(
                    shop = shop,
                    onDeleteClick = { viewModel.deleteShop(shop) }
                )
            }
        }
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingItem,
    onCheckedChange: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val completedTimeText = remember(item.completedAt) {
        item.completedAt?.let { timestamp ->
            java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(Date(timestamp))
        } ?: ""
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { _ -> onCheckedChange() }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (item.isCompleted) Color.Gray else Color.Unspecified,
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )

                Text(
                    text = item.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                if (item.isCompleted && completedTimeText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.purchased_at_time, completedTimeText),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.content_description_delete_shopping_item),
                    tint = Color.Gray.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ShopItemRow(
    shop: Shop,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = shop.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = stringResource(R.string.shop_info_subtitle, shop.category, shop.radius.toInt()),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "GPS: ${shop.latitude}, ${shop.longitude}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.content_description_delete_shop),
                    tint = Color.Gray.copy(alpha = 0.7f)
                )
            }
        }
    }
}
