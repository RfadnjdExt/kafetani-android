package com.kafetani.app.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kafetani.app.data.model.Order
import com.kafetani.app.data.repository.AdminRepository
import com.kafetani.app.ui.components.EmptyView
import com.kafetani.app.ui.components.ErrorView
import com.kafetani.app.ui.components.LoadingView
import com.kafetani.app.ui.components.StatusBadge
import com.kafetani.app.ui.components.formatRupiah
import com.kafetani.app.ui.viewModelFactory

private val STATUS_OPTIONS = listOf(
    "all" to "Semua",
    "pending" to "Menunggu",
    "processing" to "Diproses",
    "ready" to "Siap",
    "completed" to "Selesai",
    "cancelled" to "Dibatalkan"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderTab(adminRepository: AdminRepository) {
    val viewModel: AdminOrderViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = viewModelFactory { AdminOrderViewModel(adminRepository) }
    )
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(STATUS_OPTIONS) { (value, label) ->
                FilterChip(
                    selected = uiState.statusFilter == value,
                    onClick = { viewModel.setFilter(value) },
                    label = { Text(label) }
                )
            }
        }

        when {
            uiState.isLoading && uiState.orders.isEmpty() -> LoadingView(label = "Memuat pesanan...")
            uiState.error != null && uiState.orders.isEmpty() -> ErrorView(uiState.error!!, onRetry = { viewModel.load() })
            uiState.orders.isEmpty() -> EmptyView("Tidak ada pesanan untuk status ini.")
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.orders, key = { it.id }) { order ->
                        AdminOrderRow(order = order, onStatusChange = { newStatus -> viewModel.updateStatus(order.id, newStatus) })
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminOrderRow(order: Order, onStatusChange: (String) -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Pesanan #${order.id} • ${order.customer_name ?: "-"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    order.total_format ?: formatRupiah(order.total),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                "${order.type ?: "-"} • ${order.source ?: "-"} • ${order.created_at?.take(16)?.replace("T", " ") ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(10.dp))

            Box {
                Box(modifier = Modifier.clickable { menuExpanded = true }) {
                    StatusBadge(status = order.status, label = order.status_label ?: order.status)
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    STATUS_OPTIONS.filter { it.first != "all" }.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = { onStatusChange(value); menuExpanded = false }
                        )
                    }
                }
            }
        }
    }
}
