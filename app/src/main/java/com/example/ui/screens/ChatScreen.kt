package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessage
import com.example.data.FocusViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: FocusViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isCoachLoading = viewModel.isCoachLoading
    var userInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val isSpeechAvailable = remember(context) { android.speech.SpeechRecognizer.isRecognitionAvailable(context) }
    var isListening by remember { mutableStateOf(false) }
    val speechRecognizer = remember(context, isSpeechAvailable) {
        if (isSpeechAvailable) {
            try {
                android.speech.SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
            } catch (e: Exception) { null }
        } else null
    }

    val speechIntent = remember {
        android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
        }
    }

    DisposableEffect(speechRecognizer) {
        if (speechRecognizer != null) {
            try {
                speechRecognizer.setRecognitionListener(object : android.speech.RecognitionListener {
                    override fun onReadyForSpeech(params: android.os.Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() { isListening = false }
                    override fun onError(error: Int) { isListening = false }
                    override fun onResults(results: android.os.Bundle?) {
                        val matches = results?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            userInput = matches[0]
                        }
                    }
                    override fun onPartialResults(partialResults: android.os.Bundle?) {
                        val matches = partialResults?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            userInput = matches[0]
                        }
                    }
                    override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
                })
            } catch (e: Exception) {}
        }
        onDispose {
            try { speechRecognizer?.cancel() } catch (e: Exception) {}
            try { speechRecognizer?.destroy() } catch (e: Exception) {}
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (speechRecognizer != null) {
                isListening = true
                userInput = "Listening..."
                try {
                    speechRecognizer.startListening(speechIntent)
                } catch (e: Exception) { isListening = false }
            } else {
                android.widget.Toast.makeText(context, "Speech recognition unavailable", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            android.widget.Toast.makeText(context, "Audio permission denied", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // Scroll to bottom when messages list size changes
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    var showQuickVoiceDialog by remember { mutableStateOf(false) }

    if (showQuickVoiceDialog) {
        com.example.ui.components.QuickVoiceEntryDialog(
            viewModel = viewModel,
            onDismiss = { showQuickVoiceDialog = false },
            onOpenChatWithPrompt = { prompt ->
                userInput = prompt
                showQuickVoiceDialog = false
            }
        )
    }

    val quickActions = listOf(
        "🎤 Voice Task / Goal",
        "Why am I distracted?",
        "Boost my streak",
        "Plan tomorrow",
        "Focus technique"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // --- Sheet Header ---
        CenterAlignedTopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = com.example.ui.components.CubeIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gemini", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                }
            },
            actions = {
                com.example.ui.components.GlassIconButton(
                    onClick = { showQuickVoiceDialog = true },
                    contentDescription = "Quick Voice Task/Goal Entry",
                    modifier = Modifier.testTag("quick_voice_entry_header_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                com.example.ui.components.GlassIconButton(
                    onClick = { viewModel.clearChatHistory() },
                    contentDescription = "Clear chat history",
                    modifier = Modifier.testTag("clear_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        )

        // --- Chat Logs View ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (chatMessages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = com.example.ui.components.CubeIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Hello! I am Gemini.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "I am context-aware of your streaks & habits to give highly custom guidance.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(chatMessages) { message ->
                        ChatBubble(message = message)
                    }
                }
            }

            com.example.ui.components.GlassOverlay(
                isLoading = isCoachLoading,
                viewModel = viewModel,
                loadingText = "Coach is formulating strategy..."
            )
        }

        // --- Quick Action Chips ---
        Text(
            text = "QUICK ACTIONS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickActions.forEach { action ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable {
                            if (action.contains("Voice Task")) {
                                showQuickVoiceDialog = true
                            } else {
                                viewModel.sendMessageToCoach(action)
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("chip_${action.replace(" ", "_")}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = action,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- Loader Status text ---
        AnimatedVisibility(visible = isCoachLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Syncing insights and thinking...",
                    style = MaterialTheme.typography.bodySmall,
                    color = viewModel.themeTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // --- Chat Input ---
        Surface(
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    placeholder = { Text("Ask Coach anything...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_text"),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                com.example.ui.components.GlassIconButton(
                    onClick = {
                        if (isListening) {
                            try { speechRecognizer?.stopListening() } catch (e: Exception) {}
                            isListening = false
                        } else {
                            micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    contentDescription = if (isListening) "Stop voice input" else "Start voice input",
                    modifier = Modifier
                        .background(
                            if (isListening) Color.Red.copy(alpha = 0.2f) else MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(16.dp)
                        )
                        .testTag("mic_chat_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = null,
                        tint = if (isListening) Color.Red else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                com.example.ui.components.GlassIconButton(
                    onClick = {
                        if (userInput.trim().isNotEmpty()) {
                            viewModel.sendMessageToCoach(userInput.trim())
                            userInput = ""
                        }
                    },
                    contentDescription = "Send Message",
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                        .testTag("send_chat_button"),
                    enabled = !isCoachLoading
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bg = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
    val shape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chat_bubble_${message.id}"),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Text(
                text = if (isUser) "You" else "Gemini",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(shape)
                    .background(bg)
                    .padding(12.dp)
            ) {
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )
            }
        }
    }
}
