package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.AcademicChatMessage
import com.example.data.AcademicChatThread
import com.example.data.FocusViewModel
import java.util.Calendar
import java.util.Locale
import com.example.ui.components.GlassSurface

data class TaskProposal(
    val title: String,
    val subject: String,
    val dueInDays: Int,
    val priority: String,
    val notes: String
)

data class ClassProposal(
    val subject: String,
    val day: Int,
    val start: String,
    val end: String,
    val instructor: String,
    val room: String
)

fun parseTaskProposal(message: String): TaskProposal? {
    val regex = """\[PROPOSAL_ADD_TASK:\s*title="([^"]*)",\s*subject="([^"]*)",\s*dueInDays=(\d+),\s*priority="([^"]*)",\s*notes="([^"]*)"\]""".toRegex()
    val match = regex.find(message) ?: return null
    val (title, subject, dueInDays, priority, notes) = match.destructured
    return TaskProposal(title, subject, dueInDays.toIntOrNull() ?: 1, priority, notes)
}

fun parseClassProposal(message: String): ClassProposal? {
    val regex = """\[PROPOSAL_ADD_CLASS:\s*subject="([^"]*)",\s*day=(\d+),\s*start="([^"]*)",\s*end="([^"]*)",\s*instructor="([^"]*)",\s*room="([^"]*)"\]""".toRegex()
    val match = regex.find(message) ?: return null
    val (subject, day, start, end, instructor, room) = match.destructured
    return ClassProposal(subject, day.toIntOrNull() ?: 1, start, end, instructor, room)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicAssistantDialog(
    viewModel: FocusViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isDark = viewModel.isDarkMode

    // Theming Colors - White Glass in Light mode, Deep Dark Glass in Dark mode
    val baseBgColor = if (isDark) Color(0xFF0A0814) else Color(0xFFF1F5F9)
    val cardBgColor = if (isDark) Color(0x331E1B2E) else Color(0xB2FFFFFF)
    val borderCol = if (isDark) Color(0x33FFFFFF) else Color(0x1F000000)
    val txtPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val txtSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    val accentColor = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488)

    // Database states
    val threads by viewModel.academicChatThreads.collectAsState()
    val activeThreadId by viewModel.activeAcademicThreadId.collectAsState()
    val messages by viewModel.activeAcademicMessages.collectAsState()
    val isChatLoading = viewModel.isAcademicChatLoading

    // UI Input states
    var chatInput by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var micModeActive by remember { mutableStateOf(false) }
    var isThreadDropdownExpanded by remember { mutableStateOf(false) }

    // TTS Engine State
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    // Initialize TTS
    DisposableEffect(context) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Done
            }
        }
        tts.language = Locale.getDefault()
        ttsEngine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun speakText(text: String) {
        ttsEngine?.let { engine ->
            // Strip any bracketed proposal tags so they aren't read aloud
            val speechText = text.replace("""\[PROPOSAL_[^\]]*\]""".toRegex(), "").trim()
            if (speechText.isNotEmpty()) {
                isSpeaking = true
                engine.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "academic_tts")
            }
        }
    }

    fun stopSpeaking() {
        ttsEngine?.stop()
        isSpeaking = false
    }

    // Auto-create thread if none exists
    LaunchedEffect(threads, activeThreadId) {
        if (threads.isEmpty()) {
            viewModel.createAcademicChatThread("Homework & Study Help")
        } else if (activeThreadId == null) {
            viewModel.selectAcademicChatThread(threads.first().id)
        }
    }

    // List scroll state
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Speech Recognizer for Mic Mode
    val isSpeechAvailable = remember(context) { SpeechRecognizer.isRecognitionAvailable(context) }
    var isListening by remember { mutableStateOf(false) }
    var partialSpeechTranscript by remember { mutableStateOf("") }

    val speechRecognizer = remember(context, isSpeechAvailable) {
        if (isSpeechAvailable) {
            try {
                SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
            } catch (e: Exception) { null }
        } else null
    }

    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
    }

    DisposableEffect(speechRecognizer) {
        if (speechRecognizer != null) {
            try {
                speechRecognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() { isListening = false }
                    override fun onError(error: Int) { isListening = false }
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val transcript = matches[0]
                            partialSpeechTranscript = transcript
                            viewModel.sendAcademicChatMessage(
                                messageText = transcript,
                                onTtsRequired = { aiResponse ->
                                    speakText(aiResponse)
                                }
                            )
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            partialSpeechTranscript = matches[0]
                        }
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            } catch (e: Exception) {}
        }
        onDispose {
            try {
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (speechRecognizer != null) {
                isListening = true
                partialSpeechTranscript = "Listening..."
                stopSpeaking()
                try {
                    speechRecognizer.startListening(speechIntent)
                } catch (e: Exception) { isListening = false }
            } else {
                Toast.makeText(context, "Speech recognition unavailable", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Record Audio Permission Denied", Toast.LENGTH_LONG).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val galaxySpeed by viewModel.galaxyStarSpeed.collectAsState()
        val galaxyHue by viewModel.galaxyHueShift.collectAsState()

        Box(modifier = Modifier.fillMaxSize().background(baseBgColor)) {
            com.example.ui.components.ViewModelConfettiOverlay(viewModel)
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            renderEffect = android.graphics.RenderEffect
                                .createBlurEffect(120f, 120f, android.graphics.Shader.TileMode.CLAMP)
                                .asComposeRenderEffect()
                        }
                    }
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF0D9488).copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.2f),
                        radius = 500f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.45f),
                        radius = 650f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFEC4899).copy(alpha = 0.1f), Color.Transparent),
                        center = Offset(size.width * 0.35f, size.height * 0.75f),
                        radius = 700f
                    )
                )
            }

            com.example.ui.components.GalaxyBackground(
                isVisible = true, // User requested animation in academic portal
                starSpeed = galaxySpeed,
                hueShift = galaxyHue,
                isDarkTheme = isDark, // Which we'll force to false
                modifier = Modifier.fillMaxSize()
            )
            
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                // --- Top Header Bar ---
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "ACADEMIC ASSISTANT",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp,
                                    color = accentColor
                                )
                            )
                            val currentThread = threads.find { it.id == activeThreadId }
                            Row(
                                modifier = Modifier
                                    .clickable { isThreadDropdownExpanded = true }
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentThread?.title ?: "Select Thread",
                                    fontSize = 11.sp,
                                    color = txtSecondary
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Threads dropdown",
                                    tint = txtSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        com.example.ui.components.GlassIconButton(
                            onClick = onDismiss,
                            contentDescription = "Close academic assistant",
                            modifier = Modifier.testTag("dismiss_assistant_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = txtPrimary)
                        }
                    },
                    actions = {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Clear history of active thread / delete thread
                            com.example.ui.components.GlassIconButton(
                                onClick = {
                                    activeThreadId?.let { id ->
                                        viewModel.deleteAcademicChatThread(id)
                                        Toast.makeText(context, "Chat thread removed!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                contentDescription = "Delete thread"
                            ) {
                                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = Color.Red.copy(alpha = 0.8f))
                            }
                            // Create New Chat Thread
                            com.example.ui.components.GlassIconButton(
                                onClick = {
                                    viewModel.createAcademicChatThread("Homework help ${threads.size + 1}")
                                    Toast.makeText(context, "New chat thread created!", Toast.LENGTH_SHORT).show()
                                },
                                contentDescription = "New thread"
                            ) {
                                Icon(imageVector = Icons.Default.AddComment, contentDescription = null, tint = accentColor)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = cardBgColor,
                        titleContentColor = txtPrimary
                    )
                )

                // Thread dropdown selection
                Box {
                    DropdownMenu(
                        expanded = isThreadDropdownExpanded,
                        onDismissRequest = { isThreadDropdownExpanded = false },
                        modifier = Modifier
                            .background(cardBgColor)
                            .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                    ) {
                        threads.forEach { thread ->
                            DropdownMenuItem(
                                text = { Text(thread.title, color = txtPrimary, fontSize = 13.sp) },
                                onClick = {
                                    viewModel.selectAcademicChatThread(thread.id)
                                    isThreadDropdownExpanded = false
                                },
                                trailingIcon = {
                                    if (thread.id == activeThreadId) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = "Active", tint = accentColor, modifier = Modifier.size(16.dp))
                                    }
                                }
                            )
                        }
                    }
                }

                // Divider line
                HorizontalDivider(color = borderCol)

                // --- Main Conversation / Speech area ---
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (micModeActive) {
                        // --- Hands-free MIC MODE View ---
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Hands-free Spoken Conversation",
                                fontWeight = FontWeight.SemiBold,
                                color = txtPrimary,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Push to Talk mode active. Text read aloud automatically.",
                                fontSize = 11.sp,
                                color = txtSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                            )

                            // Breath / Waveform animation for Listening, Speaking, or Thinking
                            val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 1.0f,
                                targetValue = if (isListening) 1.3f else if (isSpeaking) 1.2f else 1.0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = EaseInOutQuad),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "scale"
                            )

                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isListening -> Color.Red.copy(alpha = 0.15f)
                                            isSpeaking -> accentColor.copy(alpha = 0.15f)
                                            isChatLoading -> Color.Yellow.copy(alpha = 0.15f)
                                            else -> txtSecondary.copy(alpha = 0.12f)
                                        }
                                    )
                                    .drawBehind {
                                        val color = when {
                                            isListening -> Color.Red.copy(alpha = 0.4f)
                                            isSpeaking -> accentColor.copy(alpha = 0.4f)
                                            isChatLoading -> Color.Yellow.copy(alpha = 0.4f)
                                            else -> txtSecondary.copy(alpha = 0.2f)
                                        }
                                        drawCircle(
                                            color = color,
                                            radius = size.width * pulseScale / 2,
                                            style = Stroke(width = 3.dp.toPx())
                                        )
                                    }
                                    .clickable {
                                        if (isListening) {
                                            try { speechRecognizer?.stopListening() } catch (e: Exception) {}
                                            isListening = false
                                        } else if (isSpeaking) {
                                            stopSpeaking()
                                        } else {
                                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when {
                                        isListening -> Icons.Default.MicOff
                                        isSpeaking -> Icons.Default.VolumeOff
                                        isChatLoading -> Icons.Default.HourglassEmpty
                                        else -> Icons.Default.Mic
                                    },
                                    contentDescription = "Interact mic",
                                    tint = when {
                                        isListening -> Color.Red
                                        isSpeaking -> accentColor
                                        isChatLoading -> Color.Yellow
                                        else -> txtPrimary
                                    },
                                    modifier = Modifier.size(48.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = when {
                                    isListening -> "Listening... Tap to stop"
                                    isSpeaking -> "Speaking response... Tap to mute"
                                    isChatLoading -> "Thinking step-by-step..."
                                    else -> "Tap the orb to speak!"
                                },
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isListening -> Color.Red
                                    isSpeaking -> accentColor
                                    else -> txtPrimary
                                },
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Live Transcript area
                            GlassSurface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                border = BorderStroke(1.dp, borderCol)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "LIVE SPEECH TRANSCRIPT",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = txtSecondary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (partialSpeechTranscript.isNotBlank()) "\"$partialSpeechTranscript\"" else "Speak to start transcribing on-device...",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                            color = txtPrimary
                                        ),
                                        minLines = 3
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Quick exit mic mode
                            com.example.ui.components.GlassButton(
                                onClick = { micModeActive = false; stopSpeaking() },
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                            ) {
                                Icon(imageVector = Icons.Default.Keyboard, contentDescription = "Type mode", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Switch to Text Mode", fontSize = 12.sp)
                            }
                        }
                    } else {
                        // --- Standard AI Chat Logs ---
                        if (messages.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp)
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Academic Assistant Logo",
                                    tint = accentColor.copy(alpha = 0.4f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Ready to assist you step-by-step!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = txtPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "I have read-only access to your schedule and homework list. I can help explain concepts or draft study proposals.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = txtSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                                )

                                // Suggested prompt starters
                                Text(
                                    text = "CONVERSATION STARTERS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    ),
                                    modifier = Modifier.align(Alignment.Start)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                listOf(
                                    "What do I have due this week?",
                                    "Help me plan study time before Friday's test",
                                    "Explain standard deviation formula step-by-step",
                                    "Draft a homework proposal for Chemistry assignment"
                                ).forEach { starter ->
                                    GlassSurface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                viewModel.sendAcademicChatMessage(starter)
                                            },
                                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                        border = BorderStroke(1.dp, borderCol)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = "Starter Hint", tint = accentColor, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(starter, fontSize = 12.sp, color = txtPrimary, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(messages) { message ->
                                    AcademicChatBubble(
                                        message = message,
                                        viewModel = viewModel,
                                        cardBgColor = cardBgColor,
                                        borderCol = borderCol,
                                        txtPrimary = txtPrimary,
                                        txtSecondary = txtSecondary,
                                        accentColor = accentColor,
                                        onTtsSpeak = { speakText(message.message) }
                                    )
                                }
                            }
                        }
                    }

                    com.example.ui.components.GlassOverlay(
                        isLoading = isChatLoading,
                        viewModel = viewModel,
                        loadingText = "Academic AI is calculating..."
                    )
                }

                // --- Bottom Input Area (Only in Text Mode) ---
                if (!micModeActive) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardBgColor)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Attached Image Thumbnail
                        selectedImageUri?.let { uri ->
                            Row(
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(accentColor.copy(alpha = 0.12f))
                                    .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Selected Photo Attachment",
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Photo attached for analysis", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = txtPrimary)
                                    Text("Image understanding: gemini-3.1-pro-preview", fontSize = 10.sp, color = accentColor)
                                }
                                com.example.ui.components.GlassIconButton(
                                    onClick = { selectedImageUri = null },
                                    contentDescription = "Remove Photo",
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Input field Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.navigationBarsPadding().imePadding()
                        ) {
                            // Camera/Gallery Trigger
                            com.example.ui.components.GlassIconButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                contentDescription = "Attach photo"
                            ) {
                                Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = accentColor)
                            }

                            // Text Input Field
                            OutlinedTextField(
                                value = chatInput,
                                onValueChange = { chatInput = it },
                                placeholder = { Text("Ask, explain, or draft schedule changes...", fontSize = 13.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("academic_assistant_input"),
                                shape = RoundedCornerShape(24.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = borderCol
                                )
                            )

                            // Mic Mode Toggle
                            com.example.ui.components.GlassIconButton(
                                onClick = { micModeActive = true },
                                contentDescription = "Voice mode"
                            ) {
                                Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = txtSecondary)
                            }

                            // Send Button
                            com.example.ui.components.GlassIconButton(
                                onClick = {
                                    if (chatInput.trim().isNotBlank() || selectedImageUri != null) {
                                        viewModel.sendAcademicChatMessage(
                                            messageText = chatInput.trim(),
                                            imageUri = selectedImageUri?.toString()
                                        )
                                        chatInput = ""
                                        selectedImageUri = null
                                    }
                                },
                                contentDescription = "Send message",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(accentColor)
                                    .testTag("academic_assistant_send_btn"),
                                enabled = !isChatLoading
                            ) {
                                if (isChatLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                                } else {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun AcademicChatBubble(
    message: AcademicChatMessage,
    viewModel: FocusViewModel,
    cardBgColor: Color,
    borderCol: Color,
    txtPrimary: Color,
    txtSecondary: Color,
    accentColor: Color,
    onTtsSpeak: () -> Unit
) {
    val isUser = message.role == "user"
    val bubbleBg = if (isUser) accentColor else cardBgColor
    val textCol = if (isUser) Color.White else txtPrimary

    val shape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    // Check for AI proposals
    val taskProposal = remember(message.message) {
        if (!isUser) parseTaskProposal(message.message) else null
    }
    val classProposal = remember(message.message) {
        if (!isUser) parseClassProposal(message.message) else null
    }
    val citations = remember(message.message) {
        if (!isUser) parseCitations(message.message) else emptyList()
    }

    // Clean text to show in bubble (hide raw proposal tag and citation string from output)
    val displayMessageText = remember(message.message) {
        message.message
            .replace("""\[PROPOSAL_[^\]]*\]""".toRegex(), "")
            .replace("""\[CITATION:[^\]]*\]""".toRegex(), "")
            .trim()
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = if (isUser) "You" else "Academic Assistant",
                style = MaterialTheme.typography.labelSmall,
                color = txtSecondary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )

            // Message Bubble content
            Surface(
                shape = shape,
                color = bubbleBg,
                border = if (!isUser) BorderStroke(1.dp, borderCol) else null,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Render image attachment if user uploaded one
                    message.imageUri?.let { uriStr: String ->
                        AsyncImage(
                            model = Uri.parse(uriStr),
                            contentDescription = "User upload image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.1f))
                                .padding(bottom = 8.dp)
                        )
                    }

                    // Display text content
                    Text(
                        text = displayMessageText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textCol
                    )

                    if (citations.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(citations) { (title, url) ->
                                val context = LocalContext.current
                                SuggestionChip(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Fallback
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = title,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 11.sp
                                        )
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = if (isUser) Color.White.copy(alpha = 0.2f) else accentColor.copy(alpha = 0.15f),
                                        labelColor = if (isUser) Color.White else txtPrimary,
                                        iconContentColor = if (isUser) Color.White else accentColor
                                    )
                                )
                            }
                        }
                    }

                    // TTS speak button for AI responses
                    if (!isUser) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            com.example.ui.components.GlassIconButton(
                                onClick = onTtsSpeak,
                                contentDescription = "Read aloud",
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = txtSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- Interactive Proposal Card ---
            if (taskProposal != null) {
                var isProposalHandled by remember { mutableStateOf(false) }
                if (!isProposalHandled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassSurface(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, accentColor),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Assignment, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Suggested Homework", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = txtPrimary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Title: ${taskProposal.title}", fontSize = 12.sp, color = txtPrimary)
                            Text("Subject: ${taskProposal.subject}", fontSize = 12.sp, color = txtPrimary)
                            Text("Priority: ${taskProposal.priority}", fontSize = 12.sp, color = txtPrimary)
                            if (taskProposal.notes.isNotEmpty()) {
                                Text("Notes: ${taskProposal.notes}", fontSize = 11.sp, color = txtSecondary)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                com.example.ui.components.GlassButton(
                                    onClick = {
                                        val cal = Calendar.getInstance()
                                        cal.add(Calendar.DAY_OF_YEAR, taskProposal.dueInDays)
                                        viewModel.addAcademicTask(
                                            title = taskProposal.title,
                                            subjectName = taskProposal.subject,
                                            dueDate = cal.timeInMillis,
                                            priority = taskProposal.priority,
                                            notes = taskProposal.notes
                                        )
                                        isProposalHandled = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text("Add Homework", fontSize = 10.sp)
                                }
                                com.example.ui.components.GlassButton(variant = com.example.ui.components.GlassButtonVariant.Secondary, 
                                    onClick = { isProposalHandled = true },
                                    modifier = Modifier.weight(1f),
                                    border = BorderStroke(1.dp, borderCol),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text("Decline", fontSize = 10.sp, color = txtPrimary)
                                }
                            }
                        }
                    }
                } else {
                    Text("Homework Added successfully!", fontSize = 11.sp, color = accentColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
            }

            if (classProposal != null) {
                var isProposalHandled by remember { mutableStateOf(false) }
                if (!isProposalHandled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassSurface(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, accentColor),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Suggested Class Entry", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = txtPrimary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Subject: ${classProposal.subject}", fontSize = 12.sp, color = txtPrimary)
                            val dayName = when(classProposal.day) {
                                1 -> "Monday"; 2 -> "Tuesday"; 3 -> "Wednesday"; 4 -> "Thursday"; 5 -> "Friday"; 6 -> "Saturday"; 7 -> "Sunday"
                                else -> "All"
                            }
                            Text("Day: $dayName", fontSize = 12.sp, color = txtPrimary)
                            Text("Time: ${classProposal.start} - ${classProposal.end}", fontSize = 12.sp, color = txtPrimary)
                            Text("Instructor: ${classProposal.instructor}", fontSize = 11.sp, color = txtSecondary)
                            Text("Room: ${classProposal.room}", fontSize = 11.sp, color = txtSecondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                com.example.ui.components.GlassButton(
                                    onClick = {
                                        viewModel.addTimetableEntry(
                                            subjectName = classProposal.subject,
                                            dayOfWeek = classProposal.day,
                                            weekRotation = "All",
                                            startTime = classProposal.start,
                                            endTime = classProposal.end,
                                            instructor = classProposal.instructor,
                                            room = classProposal.room
                                        )
                                        if (viewModel.academicSubjects.value.none { it.name.lowercase() == classProposal.subject.lowercase() }) {
                                            viewModel.addSubject(classProposal.subject, classProposal.instructor, classProposal.room)
                                        }
                                        isProposalHandled = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text("Add Class", fontSize = 10.sp)
                                }
                                com.example.ui.components.GlassButton(variant = com.example.ui.components.GlassButtonVariant.Secondary, 
                                    onClick = { isProposalHandled = true },
                                    modifier = Modifier.weight(1f),
                                    border = BorderStroke(1.dp, borderCol),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text("Decline", fontSize = 10.sp, color = txtPrimary)
                                }
                            }
                        }
                    }
                } else {
                    Text("Class Added successfully!", fontSize = 11.sp, color = accentColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
            }
        }
    }
}

private val EaseInOutQuad = Easing { fraction ->
    if (fraction < 0.5f) 2f * fraction * fraction else 1f - (-2f * fraction + 2f) * (-2f * fraction + 2f) / 2f
}

fun parseCitations(messageText: String): List<Pair<String, String>> {
    val regex = """\[CITATION:\s*title="([^"]*)",\s*url="([^"]*)"\]""".toRegex()
    val matches = regex.findAll(messageText)
    return matches.map { match ->
        val title = match.groups[1]?.value ?: "Source"
        val url = match.groups[2]?.value ?: ""
        Pair(title, url)
    }.toList()
}
