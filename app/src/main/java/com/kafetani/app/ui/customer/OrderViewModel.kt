package com.kafetani.app.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kafetani.app.data.model.Order
import com.kafetani.app.data.network.ApiResult
import com.kafetani.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val orders: List<Order> = emptyList()
)

class OrderHistoryViewModel(private val repo: OrderRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(OrderListUiState())
    val uiState: StateFlow<OrderListUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repo.getMyOrders()) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, orders = result.data) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }
}

class OrderDetailViewModel(private val repo: OrderRepository, private val orderId: Int) : ViewModel() {
    private val _order = MutableStateFlow<Order?>(null)
    val order: StateFlow<Order?> = _order.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _isLoading.update { true }
            _error.update { null }
            when (val result = repo.getOrderDetail(orderId)) {
                is ApiResult.Success -> {
                    _order.update { result.data }
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
