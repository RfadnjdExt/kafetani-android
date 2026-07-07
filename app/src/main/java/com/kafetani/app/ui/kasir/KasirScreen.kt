package com.kafetani.app.ui.kasir

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kafetani.app.data.model.KasirCartItem
import com.kafetani.app.data.model.Product
import com.kafetani.app.data.repository.KasirRepository
import com.kafetani.app.ui.components.EmptyView
import com.kafetani.app.ui.components.ErrorView
import com.kafetani.app.ui.components.FullWidthPrimaryButton
import com.kafetani.app.ui.components.LoadingView
import com.kafetani.app.ui.components.QuantityStepper
import com.kafetani.app.ui.components.formatRupiah
import com.kafetani.app.ui.viewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasirScreen(
    kasirRepository: KasirRepository,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel: KasirViewModel = viewModel(factory = viewModelFactory { KasirViewModel(kasirRepository) })
    val uiState by viewModel.uiState.collectAsState()
    var showCartSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.lastOrderId) {
        uiState.lastOrderId?.let {
            showCartSheet = false
            snackbarHostState.showSnackbar("Pesanan #$it berhasil dibuat!")
            viewModel.consumeOrderResult()
        }
    }

    LaunchedEffect(uiState.submitError) {
        uiState.submitError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kasir (POS)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.Logout, contentDescription = "Keluar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isLoadingProducts && uiState.products.isEmpty() -> LoadingView(label = "Memuat menu kafe...")
                    uiState.productsError != null && uiState.products.isEmpty() ->
                        ErrorView(uiState.productsError!!, onRetry = { viewModel.loadProducts() })
                    uiState.products.isEmpty() -> EmptyView("Tidak ada produk kafe yang bisa dijual saat ini.")
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.products, key = { it.id }) { product: Product ->
                                KasirProductCard(product = product, onClick = { viewModel.addToCart(product) })
                            }
                        }
                    }
                }
            }

            Surface(shadowElevation = 12.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${uiState.cart.sumOf { it.qty }} item", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            formatRupiah(uiState.total),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(onClick = { showCartSheet = true }, enabled = uiState.cart.isNotEmpty()) {
                        Text("Proses Pesanan")
                    }
                }
            }
        }
    }

    if (showCartSheet) {
        ModalBottomSheet(onDismissRequest = { showCartSheet = false }, sheetState = rememberModalBottomSheetState()) {
            KasirCartSheetContent(
                uiState = uiState,
                onChangeQty = viewModel::changeQty,
                onCustomerNameChange = viewModel::setCustomerName,
                onOrderTypeChange = viewModel::setOrderType,
                onSubmit = { viewModel.submitOrder() }
            )
        }
    }
}

@Composable
private fun KasirProductCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(product.nama_produk, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(
                product.harga_format ?: formatRupiah(product.harga),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Stok: ${product.stok}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun KasirCartSheetContent(
    uiState: KasirUiState,
    onChangeQty: (Int, Int) -> Unit,
    onCustomerNameChange: (String) -> Unit,
    onOrderTypeChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        Text("Keranjang Kasir", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.height(220.dp)) {
            items(uiState.cart, key = { it.productId }) { item: KasirCartItem ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.namaProduk, maxLines = 1)
                        Text(formatRupiah(item.subtotal), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    QuantityStepper(
                        qty = item.qty,
                        onIncrease = { onChangeQty(item.productId, 1) },
                        onDecrease = { onChangeQty(item.productId, -1) }
                    )
                }
                HorizontalDivider()
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.customerName,
            onValueChange = onCustomerNameChange,
            label = { Text("Nama Pelanggan (opsional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = uiState.orderType == "dine-in", onClick = { onOrderTypeChange("dine-in") }, label = { Text("Makan di Tempat") })
            FilterChip(selected = uiState.orderType == "pickup", onClick = { onOrderTypeChange("pickup") }, label = { Text("Ambil Sendiri") })
        }

        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                formatRupiah(uiState.total),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(16.dp))

        FullWidthPrimaryButton(text = "Buat Pesanan", loading = uiState.isSubmitting, onClick = onSubmit)

        Spacer(Modifier.height(24.dp))
    }
}
