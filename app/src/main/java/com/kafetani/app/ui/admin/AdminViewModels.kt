package com.kafetani.app.ui.admin

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kafetani.app.data.model.DashboardStats
import com.kafetani.app.data.model.Farmer
import com.kafetani.app.data.model.Order
import com.kafetani.app.data.model.Product
import com.kafetani.app.data.network.ApiResult
import com.kafetani.app.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─── Dashboard ──────────────────────────────────────────────────────────────
data class DashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val stats: DashboardStats? = null
)

class AdminDashboardViewModel(private val repo: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repo.getDashboard()) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, stats = result.data) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }
}

// ─── Produk: list ───────────────────────────────────────────────────────────
data class AdminProductListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val products: List<Product> = emptyList(),
    val deleteMessage: String? = null
)

class AdminProductListViewModel(private val repo: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminProductListUiState())
    val uiState: StateFlow<AdminProductListUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repo.getProducts()) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, products = result.data) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            when (val result = repo.deleteProduct(id)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(deleteMessage = result.data) }
                    load()
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    fun consumeMessages() {
        _uiState.update { it.copy(deleteMessage = null, error = null) }
    }
}

// ─── Produk: form tambah/edit ───────────────────────────────────────────────
data class AdminProductFormUiState(
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Product? = null
)

class AdminProductFormViewModel(private val repo: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminProductFormUiState())
    val uiState: StateFlow<AdminProductFormUiState> = _uiState.asStateFlow()

    fun save(
        id: Int?,
        namaProduk: String,
        harga: String,
        stok: String,
        deskripsi: String,
        categoryId: Int?,
        type: String,
        petani: String,
        imageUri: Uri?
    ) {
        val hargaLong = harga.toLongOrNull()
        val stokInt = stok.toIntOrNull()

        if (namaProduk.isBlank()) {
            _uiState.update { it.copy(error = "Nama produk wajib diisi.") }
            return
        }
        if (hargaLong == null || hargaLong <= 0) {
            _uiState.update { it.copy(error = "Harga harus berupa angka lebih dari 0.") }
            return
        }
        if (stokInt == null || stokInt < 0) {
            _uiState.update { it.copy(error = "Stok harus berupa angka 0 atau lebih.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = repo.saveProduct(
                id = id,
                namaProduk = namaProduk.trim(),
                harga = hargaLong,
                stok = stokInt,
                deskripsi = deskripsi.ifBlank { null },
                categoryId = categoryId,
                type = type,
                petani = petani.ifBlank { null },
                imageUri = imageUri
            )
            when (result) {
                is ApiResult.Success -> _uiState.update { it.copy(isSaving = false, saved = result.data) }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, error = result.message) }
            }
        }
    }

    fun consumeError() { _uiState.update { it.copy(error = null) } }
}

// ─── Petani: list ───────────────────────────────────────────────────────────
data class AdminFarmerListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val farmers: List<Farmer> = emptyList()
)

class AdminFarmerListViewModel(private val repo: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminFarmerListUiState())
    val uiState: StateFlow<AdminFarmerListUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repo.getFarmers()) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, farmers = result.data) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            when (val result = repo.deleteFarmer(id)) {
                is ApiResult.Success -> load()
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    fun consumeError() { _uiState.update { it.copy(error = null) } }
}

// ─── Petani: form tambah/edit ───────────────────────────────────────────────
data class AdminFarmerFormUiState(
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Farmer? = null
)

class AdminFarmerFormViewModel(private val repo: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminFarmerFormUiState())
    val uiState: StateFlow<AdminFarmerFormUiState> = _uiState.asStateFlow()

    fun save(id: Int?, name: String, location: String, contact: String, bio: String, avatarUri: Uri?) {
        if (name.isBlank() || location.isBlank()) {
            _uiState.update { it.copy(error = "Nama dan lokasi wajib diisi.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = repo.saveFarmer(
                id = id,
                name = name.trim(),
                location = location.trim(),
                contact = contact.ifBlank { null },
                bio = bio.ifBlank { null },
                avatarUri = avatarUri
            )
            when (result) {
                is ApiResult.Success -> _uiState.update { it.copy(isSaving = false, saved = result.data) }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, error = result.message) }
            }
        }
    }

    fun consumeError() { _uiState.update { it.copy(error = null) } }
}

// ─── Pesanan (admin) ────────────────────────────────────────────────────────
data class AdminOrderUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val orders: List<Order> = emptyList(),
    val statusFilter: String = "all"
)

class AdminOrderViewModel(private val repo: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminOrderUiState())
    val uiState: StateFlow<AdminOrderUiState> = _uiState.asStateFlow()

    init { load() }

    fun setFilter(status: String) {
        _uiState.update { it.copy(statusFilter = status) }
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repo.getOrders(_uiState.value.statusFilter)) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, orders = result.data) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun updateStatus(id: Int, status: String) {
        viewModelScope.launch {
            when (val result = repo.updateOrderStatus(id, status)) {
                is ApiResult.Success -> load()
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    fun consumeError() { _uiState.update { it.copy(error = null) } }
}
