package com.focus3.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.focus3.app.data.repository.AuthRepository
import com.focus3.app.ui.screens.LoginScreen
import com.focus3.app.ui.screens.MainScreen
import com.focus3.app.ui.theme.Focus3Theme
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.graphics.drawable.ColorDrawable

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission granted or denied
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set dark window background to prevent white flash on launch/transitions
        window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.parseColor("#0D0D1A")))
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != 
                PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        enableEdgeToEdge()
        
        // Handle deep link from notification
        val navigateTo = intent.getStringExtra("NAVIGATE_TO")
        
        setContent {
            Focus3Theme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var showLogin by remember { mutableStateOf(!authRepository.isLoggedIn()) }
                    
                    if (showLogin) {
                        LoginScreen(
                            authRepository = authRepository,
                            onLoginSuccess = { showLogin = false }
                        )
                    } else {
                        MainScreen(
                            initialNavigateTo = navigateTo,
                            onNavigateToLogin = { showLogin = true }
                        )
                    }
                }
            }
        }
    }
}
