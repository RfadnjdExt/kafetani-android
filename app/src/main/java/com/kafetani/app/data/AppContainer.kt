package com.kafetani.app.data

import android.content.Context
import com.kafetani.app.data.network.NetworkModule
import com.kafetani.app.data.repository.AdminRepository
import com.kafetani.app.data.repository.AuthRepository
import com.kafetani.app.data.repository.CatalogRepository
import com.kafetani.app.data.repository.KasirRepository
import com.kafetani.app.data.repository.OrderRepository

/**
 * Dependency injection manual & sederhana (tanpa Hilt/Dagger) supaya mudah
 * dibaca dan di-debug. Satu instance dibuat sekali di KafetaniApplication,
 * lalu dipakai bareng oleh semua ViewModel lewat ViewModelFactory.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val tokenManager = TokenManager(appContext)
    private val apiService = NetworkModule.provideApiService(appContext)

    val authRepository = AuthRepository(apiService, tokenManager)
    val catalogRepository = CatalogRepository(apiService)
    val orderRepository = OrderRepository(apiService)
    val adminRepository = AdminRepository(apiService, appContext)
    val kasirRepository = KasirRepository(apiService)
}
