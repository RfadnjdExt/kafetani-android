package com.kafetani.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kafetani.app.data.TokenManager

/**
 * onLogout dipanggil langsung tanpa AuthRepository di sini — proses hapus
 * token ke server + navigasi balik ke Login sengaja dipusatkan di NavHost
 * (lihat KafetaniNavHost.kt) supaya satu alur logout dipakai bareng oleh
 * Profile (customer), Admin, & Kasir, tidak diduplikasi di tiap layar.
 */
@Composable
fun ProfileScreen(
    tokenManager: TokenManager,
    onLogout: () -> Unit
) {
    val nama by tokenManager.namaFlow.collectAsState(initial = null)
    val role by tokenManager.roleFlow.collectAsState(initial = null)

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(nama ?: "Pengguna", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            roleLabel(role),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("  Keluar", modifier = Modifier.padding(start = 4.dp))
        }
    }
}

private fun roleLabel(role: String?): String = when (role) {
    "admin" -> "Administrator"
    "kasir" -> "Kasir"
    else -> "Pelanggan"
}
