package com.kafetani.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory generik: cukup kasih lambda cara bikin ViewModel-nya, tidak perlu
 * bikin class Factory baru tiap ViewModel. Dipakai seperti ini di Composable:
 *
 *   val vm: LoginViewModel = viewModel(
 *       factory = viewModelFactory { LoginViewModel(container.authRepository) }
 *   )
 */
class SimpleViewModelFactory<T : ViewModel>(private val creator: () -> T) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = creator() as VM
}

fun <T : ViewModel> viewModelFactory(creator: () -> T): SimpleViewModelFactory<T> =
    SimpleViewModelFactory(creator)
