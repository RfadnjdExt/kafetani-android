package com.kafetani.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kafetani.app.data.model.User
import com.kafetani.app.data.network.ApiResult
import com.kafetani.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loggedInUser: User? = null
)

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Email dan password wajib diisi.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.login(email.trim(), password)) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, loggedInUser = result.data) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun register(namaLengkap: String, email: String, password: String, confirmPassword: String) {
        if (namaLengkap.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Semua field wajib diisi.") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(error = "Password minimal 6 karakter.") }
            return
        }
        if (password != confirmPassword) {
            _uiState.update { it.copy(error = "Konfirmasi password tidak cocok.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.register(namaLengkap.trim(), email.trim(), password, confirmPassword)) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, loggedInUser = result.data) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(error = null) }
    }
}
