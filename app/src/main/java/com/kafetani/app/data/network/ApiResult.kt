package com.kafetani.app.data.network

import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

/** Bungkus hasil panggilan API supaya UI tinggal cek Success/Error, tanpa try-catch berulang di tiap ViewModel. */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}

/**
 * Pembungkus aman untuk tiap panggilan Retrofit (suspend function).
 * Menangkap error jaringan & error HTTP (400/401/422/500 dst), lalu
 * mengambil pesan error yang sudah disiapkan backend (field "message" atau
 * "errors") supaya bisa langsung ditampilkan ke pengguna.
 */
suspend fun <T> safeApiCall(call: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(call())
    } catch (e: HttpException) {
        ApiResult.Error(parseErrorMessage(e))
    } catch (e: IOException) {
        ApiResult.Error(
            "Tidak bisa terhubung ke server. Pastikan backend Laravel sedang " +
                "berjalan (php artisan serve) dan HP/emulator ada di jaringan yang sama."
        )
    } catch (e: Exception) {
        ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage ?: "tidak diketahui"}")
    }
}

private fun parseErrorMessage(e: HttpException): String {
    return try {
        val body = e.response()?.errorBody()?.string()
        if (body.isNullOrBlank()) return "Terjadi kesalahan (kode ${e.code()})"

        val json = JSONObject(body)
        when {
            json.has("errors") -> {
                val errors = json.getJSONObject("errors")
                val firstKey = errors.keys().next()
                errors.getJSONArray(firstKey).getString(0)
            }
            json.has("message") -> json.getString("message")
            else -> "Terjadi kesalahan (kode ${e.code()})"
        }
    } catch (parseError: Exception) {
        "Terjadi kesalahan (kode ${e.code()})"
    }
}
