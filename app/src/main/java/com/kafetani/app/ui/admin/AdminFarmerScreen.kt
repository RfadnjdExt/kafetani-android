package com.kafetani.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kafetani.app.data.model.Farmer
import com.kafetani.app.data.repository.AdminRepository
import com.kafetani.app.ui.components.EmptyView
import com.kafetani.app.ui.components.ErrorView
import com.kafetani.app.ui.components.LoadingView
import com.kafetani.app.ui.viewModelFactory

@Composable
fun AdminFarmerTab(adminRepository: AdminRepository, onEditFarmer: (Int) -> Unit) {
    val viewModel: AdminFarmerListViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = viewModelFactory { AdminFarmerListViewModel(adminRepository) }
    )
    val uiState by viewModel.uiState.collectAsState()
    var confirmDeleteId by remember { mutableStateOf<Int?>(null) }

    when {
        uiState.isLoading && uiState.farmers.isEmpty() -> LoadingView(label = "Memuat petani...")
        uiState.error != null && uiState.farmers.isEmpty() -> ErrorView(uiState.error!!, onRetry = { viewModel.load() })
        uiState.farmers.isEmpty() -> EmptyView("Belum ada data petani. Tap tombol + untuk menambah.")
        else -> {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.farmers, key = { it.id }) { farmer ->
                    AdminFarmerRow(
                        farmer = farmer,
                        onEdit = { onEditFarmer(farmer.id) },
                        onDelete = { confirmDeleteId = farmer.id }
                    )
                }
            }
        }
    }

    confirmDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { confirmDeleteId = null },
            title = { Text("Hapus data petani?") },
            text = { Text("Produk yang terhubung ke petani ini tidak ikut terhapus.") },
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
private fun AdminFarmerRow(farmer: Farmer, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = farmer.avatar_url,
                    contentDescription = farmer.name,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(farmer.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(farmer.location, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error) }
        }
    }
}
