package com.kafetani.app.data.repository

import com.kafetani.app.data.TokenManager
import com.kafetani.app.data.model.User
import com.kafetani.app.data.network.ApiService
import com.kafetani.app.data.network.ApiResult
import com.kafetani.app.data.network.LoginRequest
import com.kafetani.app.data.network.RegisterRequest
import com.kafetani.app.data.network.safeApiCall
import android.os.Build

class AuthRepository(
    private val api: ApiService,
    private val tokenManager: TokenManager
) {
    private val deviceName: String
        get() = "Android - ${Build.MANUFACTURER} ${Build.MODEL}".take(100)

    suspend fun login(email: String, password: String): ApiResult<User> {
        val result = safeApiCall { api.login(LoginRequest(email, password, deviceName)) }
        return when (result) {
            is ApiResult.Success -> {
                val body = result.data
                if (body.success && body.token != null && body.user != null) {
                    tokenManager.saveSession(body.token, body.user.role, body.user.nama)
                    ApiResult.Success(body.user)
                } else {
                    ApiResult.Error(body.message ?: "Login gagal.")
                }
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun register(
        namaLengkap: String,
        email: String,
        password: String,
        konfirmasiPassword: String
    ): ApiResult<User> {
        val result = safeApiCall {
            api.register(RegisterRequest(namaLengkap, email, password, konfirmasiPassword, deviceName))
        }
        return when (result) {
            is ApiResult.Success -> {
                val body = result.data
                if (body.success && body.token != null && body.user != null) {
                    tokenManager.saveSession(body.token, body.user.role, body.user.nama)
                    ApiResult.Success(body.user)
                } else {
                    ApiResult.Error(body.message ?: "Registrasi gagal.")
                }
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun logout() {
        // Tetap hapus sesi lokal walau request ke server gagal (mis. tidak ada internet) —
        // dari sudut pandang user, yang penting mereka bisa keluar dari app ini.
        safeApiCall { api.logout() }
        tokenManager.clearSession()
    }

    suspend fun fetchCurrentUser(): ApiResult<User> {
        val result = safeApiCall { api.me() }
        return when (result) {
            is ApiResult.Success -> {
                val user = result.data.user
                if (user != null) ApiResult.Success(user) else ApiResult.Error("Sesi tidak valid.")
            }
            is ApiResult.Error -> result
        }
    }
}
