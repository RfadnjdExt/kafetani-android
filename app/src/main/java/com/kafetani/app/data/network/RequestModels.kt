package com.kafetani.app.data.network

/**
 * Body request JSON — dipisah dari model data biasa (Models.kt) supaya jelas
 * mana yang "dikirim ke server" vs "diterima dari server".
 */

data class RegisterRequest(
    val nama_lengkap: String,
    val email: String,
    val password: String,
    val konfirmasi_password: String,
    val device_name: String
)

data class LoginRequest(
    val email: String,
    val password: String,
    val device_name: String
)

data class CartItemPayload(
    val id: Int,
    val name: String,
    val qty: Int
)

data class CreateOrderRequest(
    val cart: List<CartItemPayload>,
    val total: Long
)

data class KasirItemPayload(
    val id: Int,
    val qty: Int
)

data class KasirOrderRequest(
    val items: List<KasirItemPayload>,
    val order_type: String,
    val customer_name: String?
)

data class UpdateStatusRequest(val status: String)
