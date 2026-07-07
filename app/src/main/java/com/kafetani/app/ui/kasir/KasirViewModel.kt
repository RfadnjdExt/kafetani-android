package com.kafetani.app.ui.kasir

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kafetani.app.data.model.KasirCartItem
import com.kafetani.app.data.model.Product
import com.kafetani.app.data.network.ApiResult
import com.kafetani.app.data.repository.KasirRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KasirUiState(
    val isLoadingProducts: Boolean = false,
    val productsError: String? = null,
    val products: List<Product> = emptyList(),

    val cart: List<KasirCartItem> = emptyList(),
    val customerName: String = "",
    val orderType: String = "dine-in",

    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val lastOrderId: Int? = null
) {
    val total: Long get() = cart.sumOf { it.subtotal }
}

class KasirViewModel(private val repo: KasirRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(KasirUiState())
    val uiState: StateFlow<KasirUiState> = _uiState.asStateFlow()

    init { loadProducts() }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProducts = true, productsError = null) }
            when (val result = repo.getProducts()) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoadingProducts = false, products = result.data) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoadingProducts = false, productsError = result.message) }
            }
        }
    }

    fun addToCart(product: Product) {
        _uiState.update { state ->
            val existing = state.cart.find { it.productId == product.id }
            val newCart = if (existing != null) {
                state.cart.map { if (it.productId == product.id) it.copy(qty = it.qty + 1) else it }
            } else {
                state.cart + KasirCartItem(product.id, product.nama_produk, product.harga, 1)
            }
            state.copy(cart = newCart)
        }
    }

    fun changeQty(productId: Int, delta: Int) {
        _uiState.update { state ->
            val newCart = state.cart.mapNotNull {
                if (it.productId == productId) {
                    val newQty = it.qty + delta
                    if (newQty <= 0) null else it.copy(qty = newQty)
                } else it
            }
            state.copy(cart = newCart)
        }
    }

    fun setCustomerName(name: String) {
        _uiState.update { it.copy(customerName = name) }
    }

    fun setOrderType(type: String) {
        _uiState.update { it.copy(orderType = type) }
    }

    fun submitOrder() {
        val state = _uiState.value
        if (state.cart.isEmpty()) {
            _uiState.update { it.copy(submitError = "Keranjang masih kosong.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }
            when (val result = repo.placeOrder(state.cart, state.orderType, state.customerName.ifBlank { null })) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isSubmitting = false, lastOrderId = result.data, cart = emptyList(), customerName = "")
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, submitError = result.message) }
            }
        }
    }

    fun consumeOrderResult() {
        _uiState.update { it.copy(lastOrderId = null) }
    }

    fun consumeError() {
        _uiState.update { it.copy(submitError = null) }
    }
}
