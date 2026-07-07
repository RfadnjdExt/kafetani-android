package com.kafetani.app.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kafetani.app.data.repository.AdminRepository
import com.kafetani.app.ui.components.formatRupiah

private enum class AdminTab(val label: String, val icon: ImageVector) {
    Dashboard("Dashboard", Icons.Filled.Dashboard),
    Produk("Produk", Icons.Filled.Restaurant),
    Petani("Petani", Icons.Filled.Agriculture),
    Pesanan("Pesanan", Icons.Filled.Receipt)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    adminRepository: AdminRepository,
    onOpenKasir: () -> Unit,
    onAddProduct: () -> Unit,
    onEditProduct: (Int) -> Unit,
    onAddFarmer: () -> Unit,
    onEditFarmer: (Int) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(AdminTab.Dashboard) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin • ${selectedTab.label}") },
                actions = {
                    IconButton(onClick = onOpenKasir) {
                        Icon(Icons.Filled.PointOfSale, contentDescription = "Buka Kasir")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.Logout, contentDescription = "Keluar")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                AdminTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                AdminTab.Produk -> FloatingActionButton(onClick = onAddProduct) { Icon(Icons.Filled.Add, contentDescription = "Tambah Produk") }
                AdminTab.Petani -> FloatingActionButton(onClick = onAddFarmer) { Icon(Icons.Filled.Add, contentDescription = "Tambah Petani") }
                else -> {}
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                AdminTab.Dashboard -> AdminDashboardTab(adminRepository)
                AdminTab.Produk -> AdminProductTab(adminRepository, onEditProduct)
                AdminTab.Petani -> AdminFarmerTab(adminRepository, onEditFarmer)
                AdminTab.Pesanan -> AdminOrderTab(adminRepository)
            }
        }
    }
}

@Composable
private fun AdminDashboardTab(adminRepository: AdminRepository) {
    val viewModel: AdminDashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = com.kafetani.app.ui.viewModelFactory { AdminDashboardViewModel(adminRepository) }
    )
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading && uiState.stats == null -> com.kafetani.app.ui.components.LoadingView(label = "Memuat dashboard...")
        uiState.error != null && uiState.stats == null -> com.kafetani.app.ui.components.ErrorView(uiState.error!!, onRetry = { viewModel.load() })
        uiState.stats != null -> {
            val stats = uiState.stats!!
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    listOf(
                        Triple("Total Pendapatan", formatRupiah(stats.total_pendapatan), MaterialTheme.colorScheme.primary),
                        Triple("Total Pesanan", "${stats.total_pesanan}", MaterialTheme.colorScheme.secondary),
                        Triple("Total Produk", "${stats.total_produk}", MaterialTheme.colorScheme.tertiary),
                        Triple("Total Petani", "${stats.total_petani}", MaterialTheme.colorScheme.error)
                    )
                ) { (label, value, color) ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
                        }
                    }
                }
            }
        }
    }
}
