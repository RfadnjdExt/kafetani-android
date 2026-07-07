package com.kafetani.app.data.network

import com.kafetani.app.data.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Menempelkan header "Authorization: Bearer <token>" otomatis ke setiap
 * request, kalau user sedang login. Endpoint publik (menu, marketplace, dll)
 * tidak masalah dapat header ini juga — backend cuma membacanya kalau memang
 * route-nya butuh (middleware auth:api).
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // intercept() dijalankan di background thread milik OkHttp, jadi aman
        // untuk blocking sebentar di sini demi ambil token yang tersimpan.
        val token = runBlocking { tokenManager.getToken() }

        val request = if (!token.isNullOrBlank()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}
