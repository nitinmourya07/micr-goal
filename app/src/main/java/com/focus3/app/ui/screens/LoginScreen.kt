package com.focus3.app.ui.screens

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focus3.app.data.repository.AuthRepository
import com.focus3.app.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

/**
 * 🔐 Premium Login Screen
 * Beautiful dark-themed sign-in with Google, matching the Focus3 aesthetic.
 */
@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onLoginSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Floating orb animation
    val infiniteTransition = rememberInfiniteTransition(label = "login_bg")
    val orbOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Google Sign-In launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        val authResult = authRepository.firebaseAuthWithGoogle(idToken)
                        isLoading = false
                        authResult.fold(
                            onSuccess = { onLoginSuccess() },
                            onFailure = { e ->
                                errorMessage = e.localizedMessage ?: "Sign-in failed"
                            }
                        )
                    }
                } else {
                    errorMessage = "Could not retrieve ID token"
                }
            } catch (e: ApiException) {
                Log.e("LoginScreen", "Google sign-in failed: ${e.statusCode}")
                errorMessage = "Google Sign-In failed (code: ${e.statusCode})"
            }
        } else {
            // User cancelled or back pressed
            isLoading = false
        }
    }

    // Show error via snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            errorMessage = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ─── Animated background orbs ───
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(
                        x = (-50 + orbOffset * 100).dp,
                        y = (100 + orbOffset * 50).dp
                    )
                    .blur(120.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PrimaryTeal.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .offset(
                        x = (200 - orbOffset * 80).dp,
                        y = (500 - orbOffset * 60).dp
                    )
                    .blur(100.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AccentPurple.copy(alpha = 0.20f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // ─── Content ───
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(0.8f))

                // App Icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(PrimaryTeal, SecondaryBlue)
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    GlassBorderLight,
                                    GlassBorder
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎯",
                        fontSize = 44.sp
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // App Name
                Text(
                    text = "Focus3",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    letterSpacing = (-1).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tagline
                Text(
                    text = "3 goals. Every day.\nYour streak starts now.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                // ─── Google Sign-In Button ───
                Button(
                    onClick = {
                        isLoading = true
                        val signInIntent = authRepository.getSignInIntent()
                        launcher.launch(signInIntent)
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1F1F1F),
                        disabledContainerColor = Color.White.copy(alpha = 0.5f),
                        disabledContentColor = Color(0xFF1F1F1F).copy(alpha = 0.5f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                            color = Color(0xFF1F1F1F)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    // Google "G" as text (avoids needing a drawable)
                    Text(
                        text = "G",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4285F4)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isLoading) "Signing in..." else "Continue with Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Skip / Continue without login
                TextButton(
                    onClick = { onLoginSuccess() }
                ) {
                    Text(
                        text = "Skip for now",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Skip",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
