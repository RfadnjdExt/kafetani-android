package com.kafetani.app.data.network

import com.kafetani.app.data.model.AdminFarmerResponse
import com.kafetani.app.data.model.AdminFarmersResponse
import com.kafetani.app.data.model.AdminProductResponse
import com.kafetani.app.data.model.AdminProductsResponse
import com.kafetani.app.data.model.AuthResponse
import com.kafetani.app.data.model.CategoriesResponse
import com.kafetani.app.data.model.DashboardResponse
import com.kafetani.app.data.model.FarmersResponse
import com.kafetani.app.data.model.KasirProductsResponse
import com.kafetani.app.data.model.MarketplaceResponse
import com.kafetani.app.data.model.MeResponse
import com.kafetani.app.data.model.MenuResponse
import com.kafetani.app.data.model.OrderCreateResponse
import com.kafetani.app.data.model.OrderResponse
import com.kafetani.app.data.model.OrdersResponse
import com.kafetani.app.data.model.ProductResponse
import com.kafetani.app.data.model.SimpleResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ─── Auth ───────────────────────────────────────────────────────────────
    @POST("register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("logout")
    suspend fun logout(): SimpleResponse

    @GET("me")
    suspend fun me(): MeResponse

    // ─── Katalog publik ─────────────────────────────────────────────────────
    @GET("menu")
    suspend fun getMenu(): MenuResponse

    @GET("marketplace")
    suspend fun getMarketplace(): MarketplaceResponse

    @GET("categories")
    suspend fun getCategories(): CategoriesResponse

    @GET("farmers")
    suspend fun getFarmers(): FarmersResponse

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): ProductResponse

    // ─── Pesanan pelanggan ──────────────────────────────────────────────────
    @POST("orders")
    suspend fun createOrder(@Body body: CreateOrderRequest): OrderCreateResponse

    @GET("orders")
    suspend fun getMyOrders(): OrdersResponse

    @GET("orders/{id}")
    suspend fun getOrderDetail(@Path("id") id: Int): OrderResponse

    // ─── Admin: dashboard ───────────────────────────────────────────────────
    @GET("admin/dashboard")
    suspend fun getDashboard(): DashboardResponse

    // ─── Admin: produk ──────────────────────────────────────────────────────
    @GET("admin/products")
    suspend fun getAdminProducts(@Query("type") type: String = "all"): AdminProductsResponse

    @Multipart
    @POST("admin/products")
    suspend fun createProduct(
        @Part("nama_produk") namaProduk: RequestBody,
        @Part("harga") harga: RequestBody,
        @Part("stok") stok: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody?,
        @Part("category_id") categoryId: RequestBody?,
        @Part("type") type: RequestBody,
        @Part("petani") petani: RequestBody?,
        @Part gambar: MultipartBody.Part?
    ): AdminProductResponse

    @Multipart
    @POST("admin/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Part("nama_produk") namaProduk: RequestBody,
        @Part("harga") harga: RequestBody,
        @Part("stok") stok: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody?,
        @Part("category_id") categoryId: RequestBody?,
        @Part("type") type: RequestBody,
        @Part("petani") petani: RequestBody?,
        @Part gambar: MultipartBody.Part?
    ): AdminProductResponse

    @DELETE("admin/products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): SimpleResponse

    // ─── Admin: petani ──────────────────────────────────────────────────────
    @GET("admin/farmers")
    suspend fun getAdminFarmers(): AdminFarmersResponse

    @Multipart
    @POST("admin/farmers")
    suspend fun createFarmer(
        @Part("name") name: RequestBody,
        @Part("location") location: RequestBody,
        @Part("contact") contact: RequestBody?,
        @Part("bio") bio: RequestBody?,
        @Part avatar: MultipartBody.Part?
    ): AdminFarmerResponse

    @Multipart
    @POST("admin/farmers/{id}")
    suspend fun updateFarmer(
        @Path("id") id: Int,
        @Part("name") name: RequestBody,
        @Part("location") location: RequestBody,
        @Part("contact") contact: RequestBody?,
        @Part("bio") bio: RequestBody?,
        @Part avatar: MultipartBody.Part?
    ): AdminFarmerResponse

    @DELETE("admin/farmers/{id}")
    suspend fun deleteFarmer(@Path("id") id: Int): SimpleResponse

    // ─── Admin: pesanan ─────────────────────────────────────────────────────
    @GET("admin/orders")
    suspend fun getAdminOrders(@Query("status") status: String = "all"): OrdersResponse

    @POST("admin/orders/{id}/status")
    suspend fun updateOrderStatus(@Path("id") id: Int, @Body body: UpdateStatusRequest): OrderResponse

    // ─── Kasir ──────────────────────────────────────────────────────────────
    @GET("kasir/products")
    suspend fun getKasirProducts(): KasirProductsResponse

    @POST("kasir/orders")
    suspend fun createKasirOrder(@Body body: KasirOrderRequest): OrderCreateResponse
}
