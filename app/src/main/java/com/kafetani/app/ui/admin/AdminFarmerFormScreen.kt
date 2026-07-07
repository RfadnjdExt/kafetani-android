package com.kafetani.app.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kafetani.app.data.model.Farmer
import com.kafetani.app.data.network.ApiResult
import com.kafetani.app.data.repository.AdminRepository
import com.kafetani.app.ui.components.FullWidthPrimaryButton
import com.kafetani.app.ui.components.LoadingView
import com.kafetani.app.ui.viewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFarmerFormScreen(
    adminRepository: AdminRepository,
    farmerId: Int?,
    onSaved: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val formViewModel: AdminFarmerFormViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = viewModelFactory { AdminFarmerFormViewModel(adminRepository) }
    )
    val formState by formViewModel.uiState.collectAsState()

    var isLoadingExisting by remember { mutableStateOf(farmerId != null) }
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var existingAvatarUrl by remember { mutableStateOf<String?>(null) }
    var newAvatarUri by remember { mutableStateOf<Uri?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) newAvatarUri = uri
    }

    LaunchedEffect(farmerId) {
        if (farmerId != null) {
            when (val result = adminRepository.getFarmers()) {
                is ApiResult.Success -> {
                    val f: Farmer? = result.data.find { it.id == farmerId }
                    if (f != null) {
                        name = f.name
                        location = f.location
                        contact = f.contact ?: ""
                        bio = f.bio ?: ""
                        existingAvatarUrl = f.avatar_url
                    }
                }
                is ApiResult.Error -> snackbarHostState.showSnackbar(result.message)
            }
            isLoadingExisting = false
        }
    }

    LaunchedEffect(formState.saved) {
        if (formState.saved != null) onSaved()
    }

    LaunchedEffect(formState.error) {
        formState.error?.let {
            snackbarHostState.showSnackbar(it)
            formViewModel.consumeError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (farmerId == null) "Tambah Petani" else "Edit Petani") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isLoadingExisting) {
            LoadingView(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val previewModel: Any? = newAvatarUri ?: existingAvatarUrl
                if (previewModel != null) {
                    AsyncImage(
                        model = previewModel,
                        contentDescription = "Foto petani",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Filled.AddAPhoto, contentDescription = null, modifier = Modifier.size(32.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Petani") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Lokasi (contoh: Gayo, Aceh)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = contact,
                onValueChange = { contact = it },
                label = { Text("Kontak (opsional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio Singkat (opsional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            FullWidthPrimaryButton(
                text = "Simpan Data Petani",
                loading = formState.isSaving,
                onClick = {
                    formViewModel.save(
                        id = farmerId,
                        name = name,
                        location = location,
                        contact = contact,
                        bio = bio,
                        avatarUri = newAvatarUri
                    )
                }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
