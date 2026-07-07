package com.kafetani.app.ui.customer

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kafetani.app.data.CartManager
import com.kafetani.app.data.model.CartItem
import com.kafetani.app.data.network.ApiResult
import com.kafetani.app.data.repository.OrderRepository
import com.kafetani.app.ui.components.EmptyView
import com.kafetani.app.ui.components.FullWidthPrimaryButton
import com.kafetani.app.ui.components.QuantityStepper
import com.kafetani.app.ui.components.formatRupiah
import com.kafetani.app.ui.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private class CheckoutViewModel(private val repo: OrderRepository) : ViewModel() {
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _result = MutableStateFlow<Int?>(null)
    val result: StateFlow<Int?> = _result.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun checkout(items: List<CartItem>, total: Long) {
        viewModelScope.launch {
            _isSubmitting.update { true }
            _error.update { null }
            when (val res = repo.checkout(items, total)) {
                is ApiResult.Success -> {
                    _isSubmitting.update { false }
                    _result.update { res.data }
                }
                is ApiResult.Error -> {
                    _isSubmitting.update { false }
                    _error.update { res.message }
                }
            }
        }
    }

    fun consumeError() { _error.update { null } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    orderRepository: OrderRepository,
    onNavigateBack: () -> Unit,
    onCheckoutSuccess: (Int) -> Unit
) {
    val viewModel: CheckoutViewModel = viewModel(factory = viewModelFactory { CheckoutViewModel(orderRepository) })
    val items by CartManager.items.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val checkoutResult by viewModel.result.collectAsState()
    val error by viewModel.error.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(checkoutResult) {
        checkoutResult?.let {
            CartManager.clear()
            onCheckoutSuccess(it)
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keranjang") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (items.isEmpty()) {
            EmptyView("Keranjang kamu masih kosong.\nYuk pilih menu atau produk tani dulu!", modifier = Modifier.padding(padding))
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items, key = { it.productId }) { item ->
                        CartRow(item)
                    }
                }

                Surface(shadowElevation = 10.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SummaryRow("Subtotal", formatRupiah(CartManager.subtotal))
                        SummaryRow("Biaya Layanan", formatRupiah(2000))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        SummaryRow("Total", formatRupiah(CartManager.total), emphasize = true)

                        Spacer(Modifier.height(16.dp))

                        FullWidthPrimaryButton(
                            text = "Checkout Sekarang",
                            loading = isSubmitting,
                            onClick = { viewModel.checkout(items, CartManager.total) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartRow(item: CartItem) {
    Card(shape = MaterialTheme.shapes.medium) {
        Row(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                AsyncImage(
                    model = item.gambarUrl,
                    contentDescription = item.namaProduk,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(item.namaProduk, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                Text(
                    formatRupiah(item.harga),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            QuantityStepper(
                qty = item.qty,
                onIncrease = { CartManager.changeQty(item.productId, 1) },
                onDecrease = { CartManager.changeQty(item.productId, -1) }
            )

            IconButton(onClick = { CartManager.remove(item.productId) }) {
                Icon(Icons.Filled.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, emphasize: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            value,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal,
            color = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
