package com.kafetani.app.data.repository

import com.kafetani.app.data.model.Category
import com.kafetani.app.data.model.Farmer
import com.kafetani.app.data.model.Product
import com.kafetani.app.data.network.ApiResult
import com.kafetani.app.data.network.ApiService
import com.kafetani.app.data.network.safeApiCall

class CatalogRepository(private val api: ApiService) {

    suspend fun getMenu(): ApiResult<Pair<List<Product>, List<String>>> {
        return when (val result = safeApiCall { api.getMenu() }) {
            is ApiResult.Success -> ApiResult.Success(result.data.products to result.data.categories)
            is ApiResult.Error -> result
        }
    }

    suspend fun getMarketplace(): ApiResult<Pair<List<Product>, List<Farmer>>> {
        return when (val result = safeApiCall { api.getMarketplace() }) {
            is ApiResult.Success -> ApiResult.Success(result.data.products to result.data.farmers)
            is ApiResult.Error -> result
        }
    }

    suspend fun getProductDetail(id: Int): ApiResult<Product> {
        return when (val result = safeApiCall { api.getProduct(id) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.product)
            is ApiResult.Error -> result
        }
    }

    suspend fun getCategories(): ApiResult<List<Category>> {
        return when (val result = safeApiCall { api.getCategories() }) {
            is ApiResult.Success -> ApiResult.Success(result.data.categories)
            is ApiResult.Error -> result
        }
    }
}
