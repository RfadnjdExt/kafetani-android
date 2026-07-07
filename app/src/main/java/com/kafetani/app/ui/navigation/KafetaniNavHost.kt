package com.kafetani.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kafetani.app.data.AppContainer
import com.kafetani.app.data.CartManager
import com.kafetani.app.data.network.ApiResult
import com.kafetani.app.ui.admin.AdminFarmerFormScreen
import com.kafetani.app.ui.admin.AdminHomeScreen
import com.kafetani.app.ui.admin.AdminProductFormScreen
import com.kafetani.app.ui.auth.LoginScreen
import com.kafetani.app.ui.auth.RegisterScreen
import com.kafetani.app.ui.components.LoadingView
import com.kafetani.app.ui.customer.CartScreen
import com.kafetani.app.ui.customer.CustomerHomeScreen
import com.kafetani.app.ui.customer.OrderDetailScreen
import com.kafetani.app.ui.customer.ProductDetailScreen
import com.kafetani.app.ui.kasir.KasirScreen
import kotlinx.coroutines.launch

@Composable
fun KafetaniNavHost(container: AppContainer) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    fun goToLogin() {
        navController.navigate(Screen.Login.route) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    fun goHomeForRole(role: String) {
        val destination = when (role) {
            "admin" -> Screen.AdminHome.route
            "kasir" -> Screen.Kasir.route
            else -> Screen.CustomerHome.route
        }
        navController.navigate(destination) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    fun doLogout() {
        scope.launch {
            container.authRepository.logout()
            CartManager.clear()
        }
        goToLogin()
    }

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            LaunchedEffect(Unit) {
                val token = container.tokenManager.getToken()
                if (token.isNullOrBlank()) {
                    goToLogin()
                } else {
                    when (val result = container.authRepository.fetchCurrentUser()) {
                        is ApiResult.Success -> goHomeForRole(result.data.role)
                        is ApiResult.Error -> {
                            container.tokenManager.clearSession()
                            goToLogin()
                        }
                    }
                }
            }
            LoadingView(label = "Menyiapkan Kafetani...")
        }

        composable(Screen.Login.route) {
            LoginScreen(
                authRepository = container.authRepository,
                onLoginSuccess = { user -> goHomeForRole(user.role) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authRepository = container.authRepository,
                onRegisterSuccess = { user -> goHomeForRole(user.role) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CustomerHome.route) {
            CustomerHomeScreen(
                catalogRepository = container.catalogRepository,
                orderRepository = container.orderRepository,
                tokenManager = container.tokenManager,
                onProductClick = { id -> navController.navigate(Screen.ProductDetail.create(id)) },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onOrderClick = { id -> navController.navigate(Screen.OrderDetail.create(id)) },
                onLogout = { doLogout() }
            )
        }

        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument(Screen.ProductDetail.ARG_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt(Screen.ProductDetail.ARG_ID) ?: 0
            ProductDetailScreen(
                catalogRepository = container.catalogRepository,
                productId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(
                orderRepository = container.orderRepository,
                onNavigateBack = { navController.popBackStack() },
                onCheckoutSuccess = { orderId ->
                    navController.navigate(Screen.OrderDetail.create(orderId)) {
                        popUpTo(Screen.CustomerHome.route)
                    }
                }
            )
        }

        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(navArgument(Screen.OrderDetail.ARG_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt(Screen.OrderDetail.ARG_ID) ?: 0
            OrderDetailScreen(
                orderRepository = container.orderRepository,
                orderId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminHome.route) {
            AdminHomeScreen(
                adminRepository = container.adminRepository,
                onOpenKasir = { navController.navigate(Screen.Kasir.route) },
                onAddProduct = { navController.navigate(Screen.AdminProductForm.createNew()) },
                onEditProduct = { id -> navController.navigate(Screen.AdminProductForm.createEdit(id)) },
                onAddFarmer = { navController.navigate(Screen.AdminFarmerForm.createNew()) },
                onEditFarmer = { id -> navController.navigate(Screen.AdminFarmerForm.createEdit(id)) },
                onLogout = { doLogout() }
            )
        }

        composable(
            route = Screen.AdminProductForm.route,
            arguments = listOf(navArgument(Screen.AdminProductForm.ARG_ID) { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            val idArg = backStackEntry.arguments?.getInt(Screen.AdminProductForm.ARG_ID) ?: -1
            AdminProductFormScreen(
                adminRepository = container.adminRepository,
                catalogRepository = container.catalogRepository,
                productId = if (idArg == -1) null else idArg,
                onSaved = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AdminFarmerForm.route,
            arguments = listOf(navArgument(Screen.AdminFarmerForm.ARG_ID) { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            val idArg = backStackEntry.arguments?.getInt(Screen.AdminFarmerForm.ARG_ID) ?: -1
            AdminFarmerFormScreen(
                adminRepository = container.adminRepository,
                farmerId = if (idArg == -1) null else idArg,
                onSaved = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Kasir.route) {
            KasirScreen(
                kasirRepository = container.kasirRepository,
                onNavigateBack = { navController.popBackStack() },
                onLogout = { doLogout() }
            )
        }
    }
}
