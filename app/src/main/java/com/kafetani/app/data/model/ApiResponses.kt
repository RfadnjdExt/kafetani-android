package com.kafetani.app.data.model

// ─── Auth ───────────────────────────────────────────────────────────────────
data class AuthResponse(
    val success: Boolean,
    val message: String?,
    val token: String?,
    val user: User?,
    val errors: Map<String, List<String>>? = null
)

data class MeResponse(val success: Boolean, val user: User?)

// ─── Respons umum (logout, hapus, dll yang cuma perlu success+message) ────────
data class SimpleResponse(
    val success: Boolean,
    val message: String?,
    val errors: Map<String, List<String>>? = null
)

// ─── Katalog ────────────────────────────────────────────────────────────────
data class MenuResponse(
    val success: Boolean,
    val products: List<Product>,
    val categories: List<String>
)

data class MarketplaceResponse(
    val success: Boolean,
    val products: List<Product>,
    val farmers: List<Farmer>
)

data class CategoriesResponse(val success: Boolean, val categories: List<Category>)
data class FarmersResponse(val success: Boolean, val farmers: List<Farmer>)
data class ProductResponse(val success: Boolean, val product: Product)

// ─── Pesanan ────────────────────────────────────────────────────────────────
data class OrdersResponse(val success: Boolean, val orders: List<Order>)
data class OrderResponse(val success: Boolean, val order: Order?, val message: String? = null)
data class OrderCreateResponse(
    val success: Boolean,
    val message: String?,
    val order_id: Int?,
    val errors: Map<String, List<String>>? = null
)

// ─── Admin ──────────────────────────────────────────────────────────────────
data class DashboardStats(
    val total_pendapatan: Long,
    val total_pesanan: Int,
    val total_produk: Int,
    val total_petani: Int
)
data class DashboardResponse(val success: Boolean, val stats: DashboardStats)

data class AdminProductsResponse(val success: Boolean, val products: List<Product>)
data class AdminProductResponse(
    val success: Boolean,
    val message: String?,
    val product: Product?,
    val errors: Map<String, List<String>>? = null
)

data class AdminFarmersResponse(val success: Boolean, val farmers: List<Farmer>)
data class AdminFarmerResponse(
    val success: Boolean,
    val message: String?,
    val farmer: Farmer?,
    val errors: Map<String, List<String>>? = null
)

// ─── Kasir ──────────────────────────────────────────────────────────────────
data class KasirProductsResponse(val success: Boolean, val products: List<Product>)
