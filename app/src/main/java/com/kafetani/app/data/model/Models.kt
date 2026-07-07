package com.kafetani.app.data.model

/**
 * Semua nama field di bawah SENGAJA disamakan persis dengan key JSON yang
 * dikirim backend Laravel (lihat app/Http/Resources/*.php di project backend),
 * supaya Gson bisa auto-mapping tanpa @SerializedName sama sekali.
 */

data class Product(
    val id: Int,
    val nama_produk: String,
    val harga: Long,
    val harga_format: String?,
    val stok: Int,
    val deskripsi: String?,
    val petani: String?,
    val type: String, // "cafe" atau "market"
    val category_id: Int?,
    val category_name: String?,
    val gambar_url: String?
)

data class Farmer(
    val id: Int,
    val name: String,
    val location: String?,
    val contact: String?,
    val bio: String?,
    val avatar_url: String?,
    val created_at: String?
)

data class Category(
    val id: Int,
    val name: String,
    val slug: String?
)

data class User(
    val id: Int,
    val nama: String,
    val email: String,
    val role: String // "admin" | "kasir" | "user"
)

data class OrderItem(
    val id: Int,
    val product_id: Int,
    val nama_produk: String?,
    val gambar_url: String?,
    val quantity: Int,
    val price: Long,
    val subtotal: Long
)

data class Order(
    val id: Int,
    val total: Long,
    val total_format: String?,
    val type: String?,
    val source: String?,
    val customer_name: String?,
    val status: String,
    val status_label: String?,
    val created_at: String?,
    val user: User?,
    val items: List<OrderItem>?
)

/** Item keranjang belanja — hidup di memori app (lihat CartManager), belum tentu sama dgn OrderItem. */
data class CartItem(
    val productId: Int,
    val namaProduk: String,
    val harga: Long,
    val gambarUrl: String?,
    var qty: Int
) {
    val subtotal: Long get() = harga * qty
}

/** Item keranjang khusus di layar Kasir (POS) — dipisah dari CartItem pelanggan agar tidak tercampur state-nya. */
data class KasirCartItem(
    val productId: Int,
    val namaProduk: String,
    val harga: Long,
    var qty: Int
) {
    val subtotal: Long get() = harga * qty
}
