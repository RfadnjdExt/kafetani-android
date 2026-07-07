package com.kafetani.app.data

import com.kafetani.app.data.model.CartItem
import com.kafetani.app.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Keranjang belanja pelanggan. Sengaja object singleton (bukan disimpan di
 * database/DataStore) karena sifatnya sementara — sama seperti localStorage
 * cart di versi web, cukup hidup selama sesi app berjalan.
 *
 * Dipakai bersama oleh MenuScreen (kafe) & MarketplaceScreen (produk tani),
 * sehingga tombol "+" dari kedua tempat itu masuk ke satu keranjang yang sama
 * — persis seperti alur checkout di versi web.
 */
object CartManager {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    private const val SERVICE_FEE = 2000L

    val subtotal: Long get() = _items.value.sumOf { it.subtotal }
    val total: Long get() = if (_items.value.isEmpty()) 0L else subtotal + SERVICE_FEE
    val totalQty: Int get() = _items.value.sumOf { it.qty }

    fun add(product: Product) {
        val current = _items.value.toMutableList()
        val idx = current.indexOfFirst { it.productId == product.id }
        if (idx >= 0) {
            current[idx] = current[idx].copy(qty = current[idx].qty + 1)
        } else {
            current.add(
                CartItem(
                    productId = product.id,
                    namaProduk = product.nama_produk,
                    harga = product.harga,
                    gambarUrl = product.gambar_url,
                    qty = 1
                )
            )
        }
        _items.value = current
    }

    fun changeQty(productId: Int, delta: Int) {
        val current = _items.value.toMutableList()
        val idx = current.indexOfFirst { it.productId == productId }
        if (idx < 0) return

        val newQty = current[idx].qty + delta
        if (newQty <= 0) {
            current.removeAt(idx)
        } else {
            current[idx] = current[idx].copy(qty = newQty)
        }
        _items.value = current
    }

    fun remove(productId: Int) {
        _items.value = _items.value.filterNot { it.productId == productId }
    }

    fun clear() {
        _items.value = emptyList()
    }
}
