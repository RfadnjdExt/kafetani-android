package com.kafetani.app.data.network

import android.content.Context
import com.kafetani.app.BuildConfig
import com.kafetani.app.data.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Membangun instance ApiService (Retrofit) sekali saja (singleton).
 *
 * BuildConfig.BASE_URL diatur di app/build.gradle.kts — WAJIB diakhiri "/"
 * dan JANGAN menyertakan "/api" di situ, karena "api/" ditambahkan otomatis
 * di sini (baseUrl final = BASE_URL + "api/").
 */
object NetworkModule {
    @Volatile private var apiService: ApiService? = null

    fun provideApiService(context: Context): ApiService {
        return apiService ?: synchronized(this) {
            apiService ?: buildApiService(context.applicationContext).also { apiService = it }
        }
    }

    private fun buildApiService(context: Context): ApiService {
        val tokenManager = TokenManager(context)

        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL + "api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
