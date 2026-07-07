package com.kafetani.app.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kafetani.app.data.model.Category
import com.kafetani.app.data.model.Product
import com.kafetani.app.data.network.ApiResult
import com.kafetani.app.data.repository.AdminRepository
import com.kafetani.app.data.repository.CatalogRepository
import com.kafetani.app.ui.components.LoadingView
import com.kafetani.app.ui.viewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductFormScreen(
    adminRepository: AdminRepository,
    catalogRepository: CatalogRepository,
    productId: Int?,
    onSaved: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val formViewModel: AdminProductFormViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = viewModelFactory { AdminProductFormViewModel(adminRepository) }
    )
    val formState by formViewModel.uiState.collectAsState()

    var isLoadingExisting by remember { mutableStateOf(productId != null) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }

    var namaProduk by remember { mutableStateOf("") }
    var harga by remember { mutableStateOf("") }
    var stok by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var petani by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("cafe") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var existingImageUrl by remember { mutableStateOf<String?>(null) }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) newImageUri = uri
    }

    // Muat kategori & (kalau mode edit) data produk yang sudah ada
    LaunchedEffect(productId) {
        when (val catResult = catalogRepository.getCategories()) {
            is ApiResult.Success -> categories = catResult.data
            is ApiResult.Error -> {}
        }

        if (productId != null) {
            when (val result = catalogRepository.getProductDetail(productId)) {
                is ApiResult.Success -> {
                    val p: Product = result.data
                    namaProduk = p.nama_produk
                    harga = p.harga.toString()
                    stok = p.stok.toString()
                    deskripsi = p.deskripsi ?: ""
                    petani = p.petani ?: ""
                    type = p.type
                    selectedCategoryId = p.category_id
                    existingImageUrl = p.gambar_url
                }
                is ApiResult.Error -> snackbarHostState.showSnackbar(result.message)
            }
            isLoadingExisting = false
        }
    }

    LaunchedEffect(formState.saved) {
        if (formState.saved != null) onSaved()
    }

    LaunchedEffect(formState.error) {
        formState.error?.let {
            snackbarHostState.showSnackbar(it)
            formViewModel.consumeError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (productId == null) "Tambah Produk" else "Edit Produk") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isLoadingExisting) {
            LoadingView(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val previewModel: Any? = newImageUri ?: existingImageUrl
                if (previewModel != null) {
                    AsyncImage(
                        model = previewModel,
                        contentDescription = "Gambar produk",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(36.dp))
                        Text("Tap untuk pilih gambar", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text("Tipe Produk", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = type == "cafe", onClick = { type = "cafe" }, label = { Text("Menu Kafe") })
                FilterChip(selected = type == "market", onClick = { type = "market" }, label = { Text("Marketplace Tani") })
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = namaProduk,
                onValueChange = { namaProduk = it },
                label = { Text("Nama Produk") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = harga,
                    onValueChange = { harga = it.filter { c -> c.isDigit() } },
                    label = { Text("Harga (Rp)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = stok,
                    onValueChange = { stok = it.filter { c -> c.isDigit() } },
                    label = { Text("Stok") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            var categoryMenuExpanded by remember { mutableStateOf(false) }
            val selectedCategoryName = categories.find { it.id == selectedCategoryId }?.name ?: "Pilih kategori (opsional)"
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedCategoryName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    modifier = Modifier.fillMaxWidth()
                )
                // Overlay transparan supaya seluruh area field bisa di-tap untuk buka menu
                // (TextField readOnly tidak otomatis merespons klik untuk membuka dropdown).
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { categoryMenuExpanded = true }
                )
                DropdownMenu(expanded = categoryMenuExpanded, onDismissRequest = { categoryMenuExpanded = false }) {
                    DropdownMenuItem(text = { Text("Tanpa kategori") }, onClick = { selectedCategoryId = null; categoryMenuExpanded = false })
                    categories.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat.name) }, onClick = { selectedCategoryId = cat.id; categoryMenuExpanded = false })
                    }
                }
            }

            if (type == "market") {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = petani,
                    onValueChange = { petani = it },
                    label = { Text("Sumber Petani (contoh: Pak Budi - Gayo, Aceh)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = deskripsi,
                onValueChange = { deskripsi = it },
                label = { Text("Deskripsi") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            com.kafetani.app.ui.components.FullWidthPrimaryButton(
                text = "Simpan Produk",
                loading = formState.isSaving,
                onClick = {
                    formViewModel.save(
                        id = productId,
                        namaProduk = namaProduk,
                        harga = harga,
                        stok = stok,
                        deskripsi = deskripsi,
                        categoryId = selectedCategoryId,
                        type = type,
                        petani = petani,
                        imageUri = newImageUri
                    )
                }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
