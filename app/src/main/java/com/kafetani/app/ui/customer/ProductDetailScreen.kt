package com.kafetani.app.ui.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kafetani.app.data.CartManager
import com.kafetani.app.data.model.Product
import com.kafetani.app.data.network.ApiResult
import com.kafetani.app.data.repository.CatalogRepository
import com.kafetani.app.ui.components.ErrorView
import com.kafetani.app.ui.components.FullWidthPrimaryButton
import com.kafetani.app.ui.components.LoadingView
import com.kafetani.app.ui.components.formatRupiah
import com.kafetani.app.ui.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private class ProductDetailViewModel(
    private val repo: CatalogRepository,
    private val productId: Int
) : ViewModel() {
    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _isLoading.update { true }
            _error.update { null }
            when (val result = repo.getProductDetail(productId)) {
                is ApiResult.Success -> {
                    _product.update { result.data }
                    _isLoading.update { false }
                }
                is ApiResult.Error -> {
                    _error.update { result.message }
                    _isLoading.update { false }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    catalogRepository: CatalogRepository,
    productId: Int,
    onNavigateBack: () -> Unit
) {
    val viewModel: ProductDetailViewModel = viewModel(
        key = "product_detail_$productId",
        factory = viewModelFactory { ProductDetailViewModel(catalogRepository, productId) }
    )
    val product by viewModel.product.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var qty by remember { mutableStateOf(1) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product?.nama_produk ?: "Detail Produk") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading && product == null -> LoadingView()
                error != null && product == null -> ErrorView(error!!, onRetry = { viewModel.load() })
                product != null -> {
                    val p = product!!
                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.3f)
                            ) {
                                AsyncImage(
                                    model = p.gambar_url,
                                    contentDescription = p.nama_produk,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(p.nama_produk, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    p.harga_format ?: formatRupiah(p.harga),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )

                                if (!p.petani.isNullOrBlank()) {
                                    Spacer(Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Dari: ${p.petani}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }

                                Spacer(Modifier.height(4.dp))
                                Text(
                                    if (p.stok > 0) "Stok tersedia: ${p.stok}" else "Stok habis",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (p.stok > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                                )

                                if (!p.deskripsi.isNullOrBlank()) {
                                    Spacer(Modifier.height(16.dp))
                                    Text("Deskripsi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    Text(p.deskripsi, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }

                        Surface(shadowElevation = 8.dp) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                com.kafetani.app.ui.components.QuantityStepper(
                                    qty = qty,
                                    onIncrease = { if (qty < p.stok) qty++ },
                                    onDecrease = { if (qty > 1) qty-- }
                                )
                                Box(modifier = Modifier.weight(1f)) {
                                    FullWidthPrimaryButton(
                                        text = if (p.stok > 0) "Tambah ke Keranjang" else "Stok Habis",
                                        enabled = p.stok > 0,
                                        onClick = {
                                            repeat(qty) { CartManager.add(p) }
                                            scope.launch { snackbarHostState.showSnackbar("${p.nama_produk} ditambahkan ke keranjang") }
                                            qty = 1
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
