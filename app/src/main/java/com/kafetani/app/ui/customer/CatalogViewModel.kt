package com.kafetani.app.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kafetani.app.data.model.Farmer
import com.kafetani.app.data.model.Product
import com.kafetani.app.data.network.ApiResult
import com.kafetani.app.data.repository.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogUiState(
    val menuLoading: Boolean = false,
    val menuError: String? = null,
    val menuProducts: List<Product> = emptyList(),
    val menuCategories: List<String> = emptyList(),

    val marketLoading: Boolean = false,
    val marketError: String? = null,
    val marketProducts: List<Product> = emptyList(),
    val farmers: List<Farmer> = emptyList()
)

class CatalogViewModel(private val repo: CatalogRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        loadMenu()
        loadMarketplace()
    }

    fun loadMenu() {
        viewModelScope.launch {
            _uiState.update { it.copy(menuLoading = true, menuError = null) }
            when (val result = repo.getMenu()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(menuLoading = false, menuProducts = result.data.first, menuCategories = result.data.second)
                }
                is ApiResult.Error -> _uiState.update { it.copy(menuLoading = false, menuError = result.message) }
            }
        }
    }

    fun loadMarketplace() {
        viewModelScope.launch {
            _uiState.update { it.copy(marketLoading = true, marketError = null) }
            when (val result = repo.getMarketplace()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(marketLoading = false, marketProducts = result.data.first, farmers = result.data.second)
                }
                is ApiResult.Error -> _uiState.update { it.copy(marketLoading = false, marketError = result.message) }
            }
        }
    }
}
