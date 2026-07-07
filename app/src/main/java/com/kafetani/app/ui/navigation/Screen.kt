package com.kafetani.app.ui.navigation

/**
 * Semua route navigasi didaftarkan di satu tempat ini supaya gampang dilacak.
 * Layar dengan tab (customer home & admin home) TIDAK pakai nested NavHost —
 * cukup state tab lokal di dalam layarnya sendiri — supaya lebih sederhana.
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")

    data object CustomerHome : Screen("customer_home")
    data object Cart : Screen("cart")

    data object AdminHome : Screen("admin_home")
    data object Kasir : Screen("kasir")

    data object ProductDetail : Screen("product_detail/{productId}") {
        const val ARG_ID = "productId"
        fun create(id: Int) = "product_detail/$id"
    }

    data object OrderDetail : Screen("order_detail/{orderId}") {
        const val ARG_ID = "orderId"
        fun create(id: Int) = "order_detail/$id"
    }

    data object AdminProductForm : Screen("admin_product_form?productId={productId}") {
        const val ARG_ID = "productId"
        fun createNew() = "admin_product_form"
        fun createEdit(id: Int) = "admin_product_form?productId=$id"
    }

    data object AdminFarmerForm : Screen("admin_farmer_form?farmerId={farmerId}") {
        const val ARG_ID = "farmerId"
        fun createNew() = "admin_farmer_form"
        fun createEdit(id: Int) = "admin_farmer_form?farmerId=$id"
    }
}
