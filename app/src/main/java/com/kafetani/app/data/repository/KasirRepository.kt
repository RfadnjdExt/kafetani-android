package com.kafetani.app.data.repository

import com.kafetani.app.data.model.KasirCartItem
import com.kafetani.app.data.model.Product
import com.kafetani.app.data.network.ApiResult
import com.kafetani.app.data.network.ApiService
import com.kafetani.app.data.network.KasirItemPayload
import com.kafetani.app.data.network.KasirOrderRequest
import com.kafetani.app.data.network.safeApiCall

class KasirRepository(private val api: ApiService) {

    suspend fun getProducts(): ApiResult<List<Product>> {
        return when (val result = safeApiCall { api.getKasirProducts() }) {
            is ApiResult.Success -> ApiResult.Success(result.data.products)
            is ApiResult.Error -> result
        }
    }

    suspend fun placeOrder(
        items: List<KasirCartItem>,
        orderType: String,
        customerName: String?
    ): ApiResult<Int> {
        val payload = KasirOrderRequest(
            items = items.map { KasirItemPayload(id = it.productId, qty = it.qty) },
            order_type = orderType,
            customer_name = customerName
        )
        return when (val result = safeApiCall { api.createKasirOrder(payload) }) {
            is ApiResult.Success -> {
                val body = result.data
                if (body.success && body.order_id != null) ApiResult.Success(body.order_id)
                else ApiResult.Error(body.message ?: "Gagal membuat pesanan.")
            }
            is ApiResult.Error -> result
        }
    }
}
