package com.kafetani.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kafetani.app.ui.navigation.KafetaniNavHost
import com.kafetani.app.ui.theme.KafetaniTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as KafetaniApplication).container

        setContent {
            KafetaniRoot {
                KafetaniNavHost(container = container)
            }
        }
    }
}

@Composable
private fun KafetaniRoot(content: @Composable () -> Unit) {
    KafetaniTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
