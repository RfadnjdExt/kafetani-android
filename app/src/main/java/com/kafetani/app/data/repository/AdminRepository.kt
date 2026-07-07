package com.kafetani.app.data.repository

import android.content.Context
import android.net.Uri
import com.kafetani.app.data.model.DashboardStats
import com.kafetani.app.data.model.Farmer
import com.kafetani.app.data.model.Order
import com.kafetani.app.data.model.Product
import com.kafetani.app.data.network.ApiResult
import com.kafetani.app.data.network.ApiService
import com.kafetani.app.data.network.UpdateStatusRequest
import com.kafetani.app.data.network.safeApiCall
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AdminRepository(
    private val api: ApiService,
    private val appContext: Context
) {
    // ─── Dashboard ──────────────────────────────────────────────────────────
    suspend fun getDashboard(): ApiResult<DashboardStats> {
        return when (val result = safeApiCall { api.getDashboard() }) {
            is ApiResult.Success -> ApiResult.Success(result.data.stats)
            is ApiResult.Error -> result
        }
    }

    // ─── Produk ─────────────────────────────────────────────────────────────
    suspend fun getProducts(type: String = "all"): ApiResult<List<Product>> {
        return when (val result = safeApiCall { api.getAdminProducts(type) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.products)
            is ApiResult.Error -> result
        }
    }

    /** [id] null = tambah produk baru; [id] terisi = update produk itu. */
    suspend fun saveProduct(
        id: Int?,
        namaProduk: String,
        harga: Long,
        stok: Int,
        deskripsi: String?,
        categoryId: Int?,
        type: String,
        petani: String?,
        imageUri: Uri?
    ): ApiResult<Product> {
        val gambarPart = imageUri?.let { uriToMultipart(it, "gambar") }

        val result = safeApiCall {
            if (id == null) {
                api.createProduct(
                    namaProduk = namaProduk.toPlainBody(),
                    harga = harga.toString().toPlainBody(),
                    stok = stok.toString().toPlainBody(),
                    deskripsi = deskripsi.toPlainBodyOrNull(),
                    categoryId = categoryId?.toString().toPlainBodyOrNull(),
                    type = type.toPlainBody(),
                    petani = petani.toPlainBodyOrNull(),
                    gambar = gambarPart
                )
            } else {
                api.updateProduct(
                    id = id,
                    namaProduk = namaProduk.toPlainBody(),
                    harga = harga.toString().toPlainBody(),
                    stok = stok.toString().toPlainBody(),
                    deskripsi = deskripsi.toPlainBodyOrNull(),
                    categoryId = categoryId?.toString().toPlainBodyOrNull(),
                    type = type.toPlainBody(),
                    petani = petani.toPlainBodyOrNull(),
                    gambar = gambarPart
                )
            }
        }

        return when (result) {
            is ApiResult.Success -> {
                val body = result.data
                if (body.success && body.product != null) ApiResult.Success(body.product)
                else ApiResult.Error(body.message ?: "Gagal menyimpan produk.")
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun deleteProduct(id: Int): ApiResult<String> {
        return when (val result = safeApiCall { api.deleteProduct(id) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.message ?: "Produk terhapus.")
            is ApiResult.Error -> result
        }
    }

    // ─── Petani ─────────────────────────────────────────────────────────────
    suspend fun getFarmers(): ApiResult<List<Farmer>> {
        return when (val result = safeApiCall { api.getAdminFarmers() }) {
            is ApiResult.Success -> ApiResult.Success(result.data.farmers)
            is ApiResult.Error -> result
        }
    }

    suspend fun saveFarmer(
        id: Int?,
        name: String,
        location: String,
        contact: String?,
        bio: String?,
        avatarUri: Uri?
    ): ApiResult<Farmer> {
        val avatarPart = avatarUri?.let { uriToMultipart(it, "avatar") }

        val result = safeApiCall {
            if (id == null) {
                api.createFarmer(
                    name = name.toPlainBody(),
                    location = location.toPlainBody(),
                    contact = contact.toPlainBodyOrNull(),
                    bio = bio.toPlainBodyOrNull(),
                    avatar = avatarPart
                )
            } else {
                api.updateFarmer(
                    id = id,
                    name = name.toPlainBody(),
                    location = location.toPlainBody(),
                    contact = contact.toPlainBodyOrNull(),
                    bio = bio.toPlainBodyOrNull(),
                    avatar = avatarPart
                )
            }
        }

        return when (result) {
            is ApiResult.Success -> {
                val body = result.data
                if (body.success && body.farmer != null) ApiResult.Success(body.farmer)
                else ApiResult.Error(body.message ?: "Gagal menyimpan data petani.")
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun deleteFarmer(id: Int): ApiResult<String> {
        return when (val result = safeApiCall { api.deleteFarmer(id) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.message ?: "Data petani terhapus.")
            is ApiResult.Error -> result
        }
    }

    // ─── Pesanan ────────────────────────────────────────────────────────────
    suspend fun getOrders(status: String = "all"): ApiResult<List<Order>> {
        return when (val result = safeApiCall { api.getAdminOrders(status) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.orders)
            is ApiResult.Error -> result
        }
    }

    suspend fun updateOrderStatus(id: Int, status: String): ApiResult<Order> {
        val result = safeApiCall { api.updateOrderStatus(id, UpdateStatusRequest(status)) }
        return when (result) {
            is ApiResult.Success -> {
                val order = result.data.order
                if (order != null) ApiResult.Success(order)
                else ApiResult.Error(result.data.message ?: "Gagal memperbarui status.")
            }
            is ApiResult.Error -> result
        }
    }

    // ─── Helper ─────────────────────────────────────────────────────────────
    private fun String.toPlainBody(): RequestBody =
        this.toRequestBody("text/plain".toMediaTypeOrNull())

    /** Field opsional: string kosong/null dikirim sebagai part yang di-omit sepenuhnya (bukan string ""), supaya validasi `nullable` di backend tidak ambigu. */
    private fun String?.toPlainBodyOrNull(): RequestBody? =
        if (this.isNullOrBlank()) null else this.toRequestBody("text/plain".toMediaTypeOrNull())

    private fun uriToMultipart(uri: Uri, partName: String): MultipartBody.Part? {
        return try {
            val resolver = appContext.contentResolver
            val mimeType = resolver.getType(uri) ?: "image/jpeg"
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null

            val ext = when {
                mimeType.contains("png") -> "png"
                mimeType.contains("webp") -> "webp"
                else -> "jpg"
            }

            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, "upload.$ext", requestBody)
        } catch (e: Exception) {
            null
        }
    }
}
