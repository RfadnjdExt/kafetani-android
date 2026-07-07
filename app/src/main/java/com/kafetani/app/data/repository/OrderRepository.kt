package com.kafetani.app.data.repository

import com.kafetani.app.data.model.CartItem
import com.kafetani.app.data.model.Order
import com.kafetani.app.data.network.ApiResult
import com.kafetani.app.data.network.ApiService
import com.kafetani.app.data.network.CartItemPayload
import com.kafetani.app.data.network.CreateOrderRequest
import com.kafetani.app.data.network.safeApiCall

class OrderRepository(private val api: ApiService) {

    /** @return ID pesanan yang baru dibuat, kalau berhasil. */
    suspend fun checkout(items: List<CartItem>, total: Long): ApiResult<Int> {
        val payload = CreateOrderRequest(
            cart = items.map { CartItemPayload(id = it.productId, name = it.namaProduk, qty = it.qty) },
            total = total
        )
        return when (val result = safeApiCall { api.createOrder(payload) }) {
            is ApiResult.Success -> {
                val body = result.data
                if (body.success && body.order_id != null) {
                    ApiResult.Success(body.order_id)
                } else {
                    ApiResult.Error(body.message ?: "Gagal membuat pesanan.")
                }
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun getMyOrders(): ApiResult<List<Order>> {
        return when (val result = safeApiCall { api.getMyOrders() }) {
            is ApiResult.Success -> ApiResult.Success(result.data.orders)
            is ApiResult.Error -> result
        }
    }

    suspend fun getOrderDetail(id: Int): ApiResult<Order> {
        return when (val result = safeApiCall { api.getOrderDetail(id) }) {
            is ApiResult.Success -> {
                val order = result.data.order
                if (order != null) ApiResult.Success(order) else ApiResult.Error("Pesanan tidak ditemukan.")
            }
            is ApiResult.Error -> result
        }
    }
}
