package com.kafetani.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kafetani.app.data.CartManager
import com.kafetani.app.data.model.Farmer
import com.kafetani.app.data.model.Product
import com.kafetani.app.ui.components.EmptyView
import com.kafetani.app.ui.components.ErrorView
import com.kafetani.app.ui.components.LoadingView
import com.kafetani.app.ui.components.ProductCard
import com.kafetani.app.ui.components.SectionHeader

@Composable
fun MarketplaceScreen(
    uiState: CatalogUiState,
    onRetry: () -> Unit,
    onProductClick: (Int) -> Unit
) {
    when {
        uiState.marketLoading && uiState.marketProducts.isEmpty() -> LoadingView(label = "Memuat marketplace...")
        uiState.marketError != null && uiState.marketProducts.isEmpty() -> ErrorView(uiState.marketError, onRetry = onRetry)
        uiState.marketProducts.isEmpty() -> EmptyView("Belum ada produk tani tersedia.")
        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState.farmers.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Column {
                            SectionHeader("Petani Mitra Kami")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(uiState.farmers, key = { it.id }) { farmer: Farmer ->
                                    FarmerAvatar(farmer)
                                }
                            }
                            SectionHeader("Produk Segar")
                        }
                    }
                }

                items(uiState.marketProducts, key = { it.id }) { product: Product ->
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

@Composable
private fun FarmerAvatar(farmer: Farmer) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = farmer.avatar_url,
                contentDescription = farmer.name,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            farmer.name,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
