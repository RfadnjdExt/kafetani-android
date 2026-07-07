package com.kafetani.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kafetani.app.data.CartManager
import com.kafetani.app.data.TokenManager
import com.kafetani.app.data.repository.AuthRepository
import com.kafetani.app.data.repository.CatalogRepository
import com.kafetani.app.data.repository.OrderRepository
import com.kafetani.app.ui.viewModelFactory

private enum class CustomerTab(val label: String, val icon: ImageVector) {
    Menu("Menu", Icons.Filled.Restaurant),
    Marketplace("Marketplace", Icons.Filled.Storefront),
    Orders("Riwayat", Icons.Filled.Receipt),
    Profile("Profil", Icons.Outlined.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    catalogRepository: CatalogRepository,
    orderRepository: OrderRepository,
    tokenManager: TokenManager,
    onProductClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    onOrderClick: (Int) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(CustomerTab.Menu) }

    val catalogViewModel: CatalogViewModel = viewModel(factory = viewModelFactory { CatalogViewModel(catalogRepository) })
    val catalogState by catalogViewModel.uiState.collectAsState()

    val orderHistoryViewModel: OrderHistoryViewModel = viewModel(factory = viewModelFactory { OrderHistoryViewModel(orderRepository) })
    val orderHistoryState by orderHistoryViewModel.uiState.collectAsState()

    val cartItems by CartManager.items.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titleFor(selectedTab)) },
                actions = {
                    if (selectedTab == CustomerTab.Menu || selectedTab == CustomerTab.Marketplace) {
                        Box {
                            IconButton(onClick = onCartClick) {
                                Icon(Icons.Filled.ShoppingCart, contentDescription = "Keranjang")
                            }
                            val totalQty = cartItems.sumOf { it.qty }
                            if (totalQty > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-6).dp, y = 6.dp)
                                        .background(MaterialTheme.colorScheme.error, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        if (totalQty > 9) "9+" else "$totalQty",
                                        color = MaterialTheme.colorScheme.onError,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                CustomerTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = {
                            selectedTab = tab
                            if (tab == CustomerTab.Orders) orderHistoryViewModel.load()
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                CustomerTab.Menu -> MenuScreen(
                    uiState = catalogState,
                    onRetry = { catalogViewModel.loadMenu() },
                    onProductClick = onProductClick
                )
                CustomerTab.Marketplace -> MarketplaceScreen(
                    uiState = catalogState,
                    onRetry = { catalogViewModel.loadMarketplace() },
                    onProductClick = onProductClick
                )
                CustomerTab.Orders -> OrderHistoryScreen(
                    uiState = orderHistoryState,
                    onRetry = { orderHistoryViewModel.load() },
                    onOrderClick = onOrderClick
                )
                CustomerTab.Profile -> ProfileScreen(
                    tokenManager = tokenManager,
                    onLogout = onLogout
                )
            }
        }
    }
}

private fun titleFor(tab: CustomerTab): String = when (tab) {
    CustomerTab.Menu -> "Menu Kafe"
    CustomerTab.Marketplace -> "Marketplace Tani"
    CustomerTab.Orders -> "Riwayat Pesanan"
    CustomerTab.Profile -> "Profil Saya"
}
