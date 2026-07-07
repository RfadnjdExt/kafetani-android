package com.kafetani.app.ui.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kafetani.app.data.CartManager
import com.kafetani.app.data.model.Product
import com.kafetani.app.ui.components.EmptyView
import com.kafetani.app.ui.components.ErrorView
import com.kafetani.app.ui.components.LoadingView
import com.kafetani.app.ui.components.ProductCard

@Composable
fun MenuScreen(
    uiState: CatalogUiState,
    onRetry: () -> Unit,
    onProductClick: (Int) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Semua") }

    when {
        uiState.menuLoading && uiState.menuProducts.isEmpty() -> LoadingView(label = "Memuat menu...")
        uiState.menuError != null && uiState.menuProducts.isEmpty() -> ErrorView(uiState.menuError, onRetry = onRetry)
        uiState.menuProducts.isEmpty() -> EmptyView("Belum ada menu kafe tersedia.")
        else -> {
            val filtered = if (selectedCategory == "Semua") {
                uiState.menuProducts
            } else {
                uiState.menuProducts.filter { it.category_name == selectedCategory }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.menuCategories) { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category) }
                                )
                            }
                        }
                    }

                    items(filtered, key = { it.id }) { product: Product ->
                        ProductCard(
                            product = product,
                            onClick = { onProductClick(product.id) },
                            onAddToCart = { CartManager.add(product) }
                        )
                    }
                }
            }
        }
    }
}
