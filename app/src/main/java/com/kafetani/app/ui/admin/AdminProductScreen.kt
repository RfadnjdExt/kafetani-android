package com.kafetani.app.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import com.kafetani.app.data.model.Product
import com.kafetani.app.data.repository.AdminRepository
import com.kafetani.app.ui.components.EmptyView
import com.kafetani.app.ui.components.ErrorView
import com.kafetani.app.ui.components.LoadingView
import com.kafetani.app.ui.components.formatRupiah
import com.kafetani.app.ui.viewModelFactory

@Composable
fun AdminProductTab(adminRepository: AdminRepository, onEditProduct: (Int) -> Unit) {
    val viewModel: AdminProductListViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = viewModelFactory { AdminProductListViewModel(adminRepository) }
    )
    val uiState by viewModel.uiState.collectAsState()
    var confirmDeleteId by remember { mutableStateOf<Int?>(null) }

    when {
        uiState.isLoading && uiState.products.isEmpty() -> LoadingView(label = "Memuat produk...")
        uiState.error != null && uiState.products.isEmpty() -> ErrorView(uiState.error!!, onRetry = { viewModel.load() })
        uiState.products.isEmpty() -> EmptyView("Belum ada produk. Tap tombol + untuk menambah.")
        else -> {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.products, key = { it.id }) { product ->
                    AdminProductRow(
                        product = product,
                        onEdit = { onEditProduct(product.id) },
                        onDelete = { confirmDeleteId = product.id }
                    )
                }
            }
        }
    }

    confirmDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { confirmDeleteId = null },
            title = { Text("Hapus produk?") },
            text = { Text("Produk yang dihapus tidak bisa dikembalikan.") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(id); confirmDeleteId = null }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteId = null }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun AdminProductRow(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp))) {
                AsyncImage(
                    model = product.gambar_url,
                    contentDescription = product.nama_produk,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(product.nama_produk, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    product.harga_format ?: formatRupiah(product.harga),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Row {
                    AssistChip(onClick = {}, label = { Text(if (product.type == "cafe") "Kafe" else "Marketplace") })
                    androidx.compose.foundation.layout.Spacer(Modifier.padding(horizontal = 4.dp))
                    Text(
                        "Stok: ${product.stok}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error) }
        }
    }
}
