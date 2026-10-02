package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
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
import com.example.data.FocusViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickVoiceEntryDialog(
    viewModel: FocusViewModel,
    onDismiss: () -> Unit,
    onOpenChatWithPrompt: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val isDark = viewModel.isDarkMode

    val cardBg = if (isDark) Color(0xF2120F24) else Color(0xF7FFFFFF)
    val borderCol = if (isDark) Color(0x33FFFFFF) else Color(0x22000000)
    val txtPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val txtSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val accentPrimary = MaterialTheme.colorScheme.primary

    var transcribedText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Tap mic to record task or goal") }

    val isSpeechAvailable = remember(context) { SpeechRecognizer.isRecognitionAvailable(context) }

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
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
    }

    DisposableEffect(speechRecognizer) {
        if (speechRecognizer != null) {
            try {
                speechRecognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening = true
                        statusText = "Listening... Speak your task or focus goal now"
                    }
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        isListening = false
                        statusText = "Recording stopped. Review or save below."
                    }
                    override fun onError(error: Int) {
                        isListening = false
                        statusText = "Listening error ($error). Tap mic to try again."
                    }
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            transcribedText = matches[0]
                            statusText = "Captured voice task successfully!"
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            transcribedText = matches[0]
                        }
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
                statusText = "Preparing speech recognition..."
                try {
                    speechRecognizer.startListening(speechIntent)
                } catch (e: Exception) {
                    isListening = false
                    statusText = "Failed to start speech recognizer."
                }
            } else {
                Toast.makeText(context, "Speech recognizer is not available on this device", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Microphone permission required for voice-to-text entry", Toast.LENGTH_LONG).show()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .testTag("quick_voice_entry_dialog"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderCol)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- Header ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = accentPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Quick Voice Entry",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = txtPrimary
                                )
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("quick_voice_close_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = txtSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Speak a task or focus goal (e.g., 'Study calculus for 45 mins' or 'Complete physics lab report')",
                        style = MaterialTheme.typography.bodySmall,
                        color = txtSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- Pulsing Mic Orb ---
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(
                                if (isListening) Color.Red.copy(alpha = 0.15f) else accentPrimary.copy(alpha = 0.12f)
                            )
                            .drawBehind {
                                val orbColor = if (isListening) Color.Red.copy(alpha = 0.4f) else accentPrimary.copy(alpha = 0.3f)
                                drawCircle(
                                    color = orbColor,
                                    radius = size.width * pulseScale / 2,
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            }
                            .clickable {
                                if (isListening) {
                                    try { speechRecognizer?.stopListening() } catch (e: Exception) {}
                                    isListening = false
                                } else {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                            .testTag("quick_voice_entry_mic_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isListening) "Stop recording" else "Start voice recording",
                            tint = if (isListening) Color.Red else accentPrimary,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isListening) Color.Red else txtSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // --- Live Transcribed Text Field ---
                    OutlinedTextField(
                        value = transcribedText,
                        onValueChange = { transcribedText = it },
                        placeholder = { Text("Transcribed task/goal will appear here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp)
                            .testTag("quick_voice_transcript_text"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentPrimary,
                            unfocusedBorderColor = borderCol,
                            focusedContainerColor = cardBg,
                            unfocusedContainerColor = cardBg
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // --- Action Buttons ---
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Action 1: Add as Focus Goal / Habit
                        Button(
                            onClick = {
                                val text = transcribedText.trim()
                                if (text.isNotBlank()) {
                                    viewModel.addHabit(
                                        name = text,
                                        category = "Focus Goal",
                                        colorHex = "#10B981"
                                    )
                                    Toast.makeText(context, "Added as Focus Goal: \"$text\"", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "Please record or type a goal first", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("save_voice_goal_btn"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            enabled = transcribedText.trim().isNotBlank()
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save as Focus Goal / Habit", fontWeight = FontWeight.SemiBold)
                        }

                        // Action 2: Ask AI Coach / Send to Chat
                        OutlinedButton(
                            onClick = {
                                val text = transcribedText.trim()
                                if (text.isNotBlank()) {
                                    if (onOpenChatWithPrompt != null) {
                                        onOpenChatWithPrompt(text)
                                    } else {
                                        viewModel.sendMessageToCoach("Record task/goal: $text")
                                    }
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "Please record or type a goal first", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("send_voice_to_ai_btn"),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, accentPrimary),
                            enabled = transcribedText.trim().isNotBlank()
                        ) {
                            Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, tint = accentPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Process with AI Coach", fontWeight = FontWeight.SemiBold, color = accentPrimary)
                        }
                    }
                }
            }
        }
    }
}
