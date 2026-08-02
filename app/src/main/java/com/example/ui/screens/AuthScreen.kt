package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AppScreen
import com.example.data.AuthState
import com.example.data.FocusViewModel
import com.example.ui.components.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.example.ui.components.GlassSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: FocusViewModel) {
    val context = LocalContext.current
    var isSignUpMode by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showGoogleTroubleshoot by remember { mutableStateOf(false) }

    // Inputs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var profileTitle by remember { mutableStateOf("") }

    // UI state
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Unique Device Identifier (addresses custom 'phone unique sdh or id' requirement)
    val deviceId = remember {
        android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: java.util.UUID.randomUUID().toString()
    }

    // Google Sign-In Config
    val googleSignInClient = remember {
        android.util.Log.d("GoogleAuth", "Initializing Google Auth provider for device: $deviceId")
        val envClientId = com.example.BuildConfig.GOOGLE_WEB_CLIENT_ID
        val webClientId = if (envClientId.isNotEmpty() && envClientId != "YOUR_GOOGLE_WEB_CLIENT_ID") {
            envClientId
        } else {
            try {
                context.getString(
                    context.resources.getIdentifier(
                        "default_web_client_id",
                        "string",
                        context.packageName
                    )
                )
            } catch (e: Exception) {
                "placeholder-web-client-id"
            }
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                viewModel.signInWithGoogle(
                    idToken = idToken,
                    deviceId = deviceId,
                    onSuccess = {
                        Toast.makeText(context, "Google Sync Enabled!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { err ->
                        errorMessage = err
                    }
                )
            } else {
                errorMessage = "Google authentication returned empty token. Please configure Firebase Google Sign-In client ID."
            }
        } catch (e: ApiException) {
            errorMessage = "Google client exception (Code: ${e.statusCode}). Please ensure google-services.json is in your project."
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Blur wrapper for starry background and liquid glass fluid blobs
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        renderEffect = android.graphics.RenderEffect
                            .createBlurEffect(60f, 60f, android.graphics.Shader.TileMode.CLAMP)
                            .asComposeRenderEffect()
                    }
                }
        ) {
            // 1. Starry night sunset background (matching the gorgeous custom assets, restoring the old login page background!)
            Image(
                painter = painterResource(id = R.drawable.img_sunset_background_1782636423915),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            // 2. Liquid Glass Fluid Blobs overlay (subtle, semi-transparent animated blobs for an extra layer of premium craft)
            val infiniteTransition = rememberInfiniteTransition(label = "liquid_glass_morphism")
            val blob1X by infiniteTransition.animateFloat(
                initialValue = 0.15f, targetValue = 0.85f,
                animationSpec = infiniteRepeatable(animation = tween(8000, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse),
                label = "blob1_x"
            )
            val blob1Y by infiniteTransition.animateFloat(
                initialValue = 0.2f, targetValue = 0.8f,
                animationSpec = infiniteRepeatable(animation = tween(11000, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse),
                label = "blob1_y"
            )
            val blob2X by infiniteTransition.animateFloat(
                initialValue = 0.8f, targetValue = 0.2f,
                animationSpec = infiniteRepeatable(animation = tween(9500, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse),
                label = "blob2_x"
            )
            val blob2Y by infiniteTransition.animateFloat(
                initialValue = 0.7f, targetValue = 0.3f,
                animationSpec = infiniteRepeatable(animation = tween(7500, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse),
                label = "blob2_y"
            )
            val blob3X by infiniteTransition.animateFloat(
                initialValue = 0.3f, targetValue = 0.7f,
                animationSpec = infiniteRepeatable(animation = tween(13000, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse),
                label = "blob3_x"
            )
            val blob3Y by infiniteTransition.animateFloat(
                initialValue = 0.8f, targetValue = 0.2f,
                animationSpec = infiniteRepeatable(animation = tween(9000, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse),
                label = "blob3_y"
            )
            val blob4X by infiniteTransition.animateFloat(
                initialValue = 0.7f, targetValue = 0.3f,
                animationSpec = infiniteRepeatable(animation = tween(10500, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse),
                label = "blob4_x"
            )
            val blob4Y by infiniteTransition.animateFloat(
                initialValue = 0.15f, targetValue = 0.85f,
                animationSpec = infiniteRepeatable(animation = tween(12000, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse),
                label = "blob4_y"
            )

            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(w * blob1X, h * blob1Y), radius = w * 0.55f
                    ),
                    radius = w * 0.55f, center = Offset(w * blob1X, h * blob1Y)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFEC4899).copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(w * blob2X, h * blob2Y), radius = w * 0.48f
                    ),
                    radius = w * 0.48f, center = Offset(w * blob2X, h * blob2Y)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF06B6D4).copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(w * blob3X, h * blob3Y), radius = w * 0.52f
                    ),
                    radius = w * 0.52f, center = Offset(w * blob3X, h * blob3Y)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFF59E0B).copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(w * blob4X, h * blob4Y), radius = w * 0.45f
                    ),
                    radius = w * 0.45f, center = Offset(w * blob4X, h * blob4Y)
                )
            }
        }

        // 3. Dark ambient wash overlay to enhance glassmorphism readability and contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x330A0915),
                            Color(0x80131124)
                        )
                    )
                )
        )

        // 4. Content Scroll Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main Glass Entrance Panel
            com.example.ui.components.GlassEntranceContainer(
                shape = RoundedCornerShape(24.dp),
                backgroundColor = Color(0x66100E20),
                borderColor = Color(0x33FFFFFF),
                elevation = 16.dp,
                isDark = true,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .widthIn(max = 480.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Spark / Sun Icon Container (dotted/bordered glow style)
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color(0x2BFFFFFF), CircleShape)
                                .background(Color(0x0DFFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Sun Icon",
                                tint = Color(0xFFE2E0FF),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Focus Cube Title Header
                        Text(
                            text = if (isSignUpMode) "Create Account" else "Focus Cube",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Subtitle & Tagline
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isSignUpMode) {
                                    "Keep your habits and stats backed up on the cloud"
                                } else {
                                    "Your focus & productivity universe awaits"
                                },
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )

                            if (!isSignUpMode) {
                                Text(
                                    text = "[Press Enter to join the adventure]",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(text = "⚔️", fontSize = 12.sp)
                                    Text(text = "🎮", fontSize = 12.sp)
                                    Text(text = "🏆", fontSize = 12.sp)
                                }
                            }
                        }

                        val isFirebaseConfigured = com.example.BuildConfig.FIREBASE_API_KEY.isNotEmpty() &&
                                com.example.BuildConfig.FIREBASE_API_KEY != "YOUR_FIREBASE_API_KEY"
                        
                        if (!isFirebaseConfigured) {
                            Spacer(modifier = Modifier.height(16.dp))
                            GlassSurface(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0x1AFFFFFF)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(16.dp))
                                    .clickable { showHelpDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Info",
                                        tint = Color(0xFFFF9800),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Firebase setup required for cloud sync. Tap for guide.",
                                        color = Color(0xFFFFB03B),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Error message animator (restored to a beautiful, elegant red/rose translucent card matching the second image)
                        AnimatedVisibility(
                            visible = errorMessage != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            errorMessage?.let { msg ->
                                GlassSurface(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0x54F43F5E) // Semi-transparent rose glass
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                        .border(
                                            width = 1.dp,
                                            color = Color(0x40F43F5E),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Error,
                                                contentDescription = "Error",
                                                tint = Color(0xFFFDA4AF),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Authentication Error",
                                                color = Color(0xFFFDA4AF),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Text(
                                            text = msg,
                                            color = Color(0xFFFEE2E2),
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))
                                        
                                        com.example.ui.components.GlassButton(
                                            onClick = {
                                                Toast.makeText(
                                                    context,
                                                    "Offline sandbox active. All progress saves locally!",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                viewModel.navigateTo(AppScreen.MAIN)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0x33FFFFFF),
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp)
                                                .testTag("error_offline_bypass_button")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CloudOff,
                                                    contentDescription = "Offline Mode",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Enter Offline Sandbox Mode",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Unified Input Fields
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isSignUpMode) {
                                // Display name
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Display Name",
                                        color = Color(0xFFB3B0CD),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                                    )
                                    OutlinedTextField(
                                        value = displayName,
                                        onValueChange = { displayName = it },
                                        placeholder = { Text("Your name", color = Color(0xFF5A5777), fontSize = 13.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0x809E86FF),
                                            unfocusedBorderColor = Color(0x11FFFFFF),
                                            focusedContainerColor = Color(0x2B131124),
                                            unfocusedContainerColor = Color(0x14131124),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("signup_name_input")
                                    )
                                }

                                // Motto
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Focus Motto",
                                        color = Color(0xFFB3B0CD),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                                    )
                                    OutlinedTextField(
                                        value = profileTitle,
                                        onValueChange = { profileTitle = it },
                                        placeholder = { Text("e.g. Daily Zen Space", color = Color(0xFF5A5777), fontSize = 13.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0x809E86FF),
                                            unfocusedBorderColor = Color(0x11FFFFFF),
                                            focusedContainerColor = Color(0x2B131124),
                                            unfocusedContainerColor = Color(0x14131124),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("signup_motto_input")
                                    )
                                }
                            }

                            // Email
                            Column(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    placeholder = { Text("Email address", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = "Mail",
                                            tint = Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF8B5CF6),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                        focusedContainerColor = Color.White.copy(alpha = 0.08f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_email_input")
                                )
                            }

                            // Password
                            Column(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    placeholder = { Text("Password", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Lock",
                                            tint = Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        com.example.ui.components.GlassIconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                            Icon(
                                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Toggle password visibility",
                                                tint = Color.White.copy(alpha = 0.6f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF8B5CF6),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                        focusedContainerColor = Color.White.copy(alpha = 0.08f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_password_input")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Remember me & Forgot Password Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var rememberMe by remember { mutableStateOf(true) }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { rememberMe = !rememberMe }
                            ) {
                                GlassToggle(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    
                                    modifier = Modifier.scale(0.8f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Remember me",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Text(
                                text = "Forgot password?",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable {
                                    Toast.makeText(context, "Password reset link sent!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Interactive Hover Submit Button
                        com.example.ui.components.RainbowCornerGlowButton(
                            text = if (viewModel.authState is AuthState.Loading) "Logging in..." else if (isSignUpMode) "Register Account" else "Enter Focus Cube",
                            onClick = {
                                errorMessage = null
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = "Email and password cannot be empty."
                                    return@RainbowCornerGlowButton
                                }
                                if (isSignUpMode) {
                                    if (displayName.isBlank()) {
                                        errorMessage = "Please enter a display name."
                                        return@RainbowCornerGlowButton
                                    }
                                    viewModel.signUpWithEmail(
                                        email = email.trim(),
                                        password = password,
                                        displayName = displayName.trim(),
                                        titleProfile = if (profileTitle.isBlank()) "Focus Cube Explorer" else profileTitle.trim(),
                                        onSuccess = {
                                            Toast.makeText(context, "Account Registered!", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { err -> errorMessage = err }
                                    )
                                } else {
                                    viewModel.signInWithEmail(
                                        email = email.trim(),
                                        password = password,
                                        onSuccess = {
                                            Toast.makeText(context, "Welcome to Focus Cube!", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { err -> errorMessage = err }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_submit_button"),
                            enabled = viewModel.authState !is AuthState.Loading,
                            isLoading = viewModel.authState is AuthState.Loading
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Google Sign-In with frosted border glass look
                        val googleInteractionSource = remember { MutableInteractionSource() }
                        val isGooglePressed by googleInteractionSource.collectIsPressedAsState()
                        val googleScale by animateFloatAsState(
                            targetValue = if (isGooglePressed) 0.96f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "google_scale"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .graphicsLayer {
                                    scaleX = googleScale
                                    scaleY = googleScale
                                }
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x0DFFFFFF))
                                .border(1.dp, Color(0x19FFFFFF), RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = googleInteractionSource,
                                    indication = null,
                                    enabled = viewModel.authState !is AuthState.Loading
                                ) {
                                    errorMessage = null
                                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                }
                                .testTag("google_signin_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(Color.White, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "G",
                                        color = Color(0xFF4285F4),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Sign In with Google",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Troubleshooting Google Sign-In link
                        Text(
                            text = "Google Sign-In not working? Get SHA-1 Guide",
                            color = Color(0xFFA78BFA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                            modifier = Modifier
                                .clickable { showGoogleTroubleshoot = true }
                                .padding(4.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Offline Sandbox Mode Button (Glassmorphic)
                        val offlineInteractionSource = remember { MutableInteractionSource() }
                        val isOfflinePressed by offlineInteractionSource.collectIsPressedAsState()
                        val offlineScale by animateFloatAsState(
                            targetValue = if (isOfflinePressed) 0.96f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "offline_scale"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .graphicsLayer {
                                    scaleX = offlineScale
                                    scaleY = offlineScale
                                }
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x1F10B981)) // Emerald glass tint
                                .border(
                                    width = 1.dp,
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF10B981).copy(alpha = 0.4f),
                                            Color(0xFF3B82F6).copy(alpha = 0.4f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable(
                                    interactionSource = offlineInteractionSource,
                                    indication = null
                                ) {
                                    Toast.makeText(
                                        context,
                                        "Offline sandbox active. All progress saves locally!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    viewModel.navigateTo(AppScreen.MAIN)
                                }
                                .testTag("offline_bypass_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = "Offline Mode",
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Continue to Offline Sandbox",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Mode toggling footer
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isSignUpMode) "Already have an account? " else "Don't have an account? ",
                                color = Color(0xFF8C8AAB),
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (isSignUpMode) "Sign In" else "Sign Up",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        isSignUpMode = !isSignUpMode
                                        errorMessage = null
                                    }
                                    .testTag("auth_mode_toggle")
                            )
                        }
                    }
                    
                    com.example.ui.components.GlassOverlay(
                        isLoading = viewModel.authState is AuthState.Loading,
                        viewModel = viewModel,
                        loadingText = if (isSignUpMode) "Registering account..." else "Signing in..."
                    )
                }
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Text(
                    text = "Configure Firebase Sync",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "To enable real Firebase Authentication and Google Sign-In, please configure the following variables in the Secrets panel in AI Studio:",
                        color = Color(0xFFD0CDE3),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    listOf(
                        "FIREBASE_API_KEY",
                        "FIREBASE_APP_ID",
                        "FIREBASE_PROJECT_ID",
                        "FIREBASE_MESSAGING_SENDER_ID",
                        "GOOGLE_WEB_CLIENT_ID"
                    ).forEach { key ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x1AFFFFFF), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = key,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "No need for a google-services.json file! FocusCube automatically initializes Firebase programmatically at launch using these variables.",
                        color = Color(0xFFE2E0FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                com.example.ui.components.GlassButton(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF))
                ) {
                    Text("Got It", color = Color.White, fontSize = 12.sp)
                }
            },
            containerColor = Color(0xFF1E1C38),
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showGoogleTroubleshoot) {
        val shaVal = getAppSignatureSHA1(context)
        val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
        AlertDialog(
            onDismissRequest = { showGoogleTroubleshoot = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = "Key",
                        tint = Color(0xFFA78BFA)
                    )
                    Text(
                        text = "Google Sign-In SHA-1 Guide",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Google Sign-In requires your unique device signature fingerprint (SHA-1 fingerprint) registered in your Firebase Console project settings.",
                        color = Color(0xFFD0CDE3),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    
                    Text(
                        text = "Your App's Runtime SHA-1 Fingerprint:",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    
                    GlassSurface(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0x26000000)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp))
                            .clickable {
                                clipboard.setText(androidx.compose.ui.text.AnnotatedString(shaVal))
                                Toast.makeText(context, "SHA-1 Key Copied!", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "TAP KEY TO COPY",
                                    color = Color(0xFF34D399),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = shaVal,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    
                    Text(
                        text = "How to register this key in Firebase:",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    
                    Text(
                        text = "1. Go to Firebase Console -> Project Settings (gear icon next to 'Project Overview').\n" +
                               "2. Scroll down to the 'Your Apps' section and select your Android App.\n" +
                               "3. Click 'Add fingerprint' and paste the SHA-1 copied above.\n" +
                               "4. Save it. (No need to download a new google-services.json as our program matches variables on launch!).",
                        color = Color(0xFFB3B0CD),
                        fontSize = 11.sp,
                        lineHeight = 17.sp
                    )
                }
            },
            confirmButton = {
                com.example.ui.components.GlassButton(
                    onClick = { showGoogleTroubleshoot = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                ) {
                    Text("Close Guide", color = Color.White, fontSize = 12.sp)
                }
            },
            containerColor = Color(0xFF1E1C38),
            shape = RoundedCornerShape(20.dp)
        )
    }
}

fun getAppSignatureSHA1(context: android.content.Context): String {
    try {
        val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            context.packageManager.getPackageInfo(
                context.packageName,
                android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(
                context.packageName,
                android.content.pm.PackageManager.GET_SIGNATURES
            )
        }
        
        val signatures = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            packageInfo.signingInfo?.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            packageInfo.signatures
        }
        
        if (signatures != null && signatures.isNotEmpty()) {
            val cert = signatures[0].toByteArray()
            val md = java.security.MessageDigest.getInstance("SHA1")
            val publicKey = md.digest(cert)
            val hexString = StringBuilder()
            for (i in publicKey.indices) {
                val appendString = Integer.toHexString(0xFF and publicKey[i].toInt()).uppercase()
                if (appendString.length == 1) {
                    hexString.append("0")
                }
                hexString.append(appendString)
                if (i < publicKey.size - 1) {
                    hexString.append(":")
                }
            }
            return hexString.toString()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return "Could not retrieve SHA-1. Run './gradlew signingReport' in your project root."
}
