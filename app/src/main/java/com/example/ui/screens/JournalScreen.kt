package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocusViewModel
import com.example.data.JournalEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.ui.components.GlassSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier
) {
    val journalEntries by viewModel.journalEntries.collectAsState()
    val creamBg = Color(0xFFFDFBF7)
    val paperInk = Color(0xFF2B2620)
    val softGold = Color(0xFFD4AF37)

    val prompts = listOf(
        "What is one small win you experienced today?",
        "Name a person who made your day slightly easier or brighter.",
        "What is something beautiful you saw or heard recently?",
        "What is a personal strength you utilized today?",
        "What comfort are you most grateful for right now?"
    )

    var currentPromptIdx by remember { mutableStateOf(0) }
    var selectedMood by remember { mutableStateOf(3) }
    var journalText by remember { mutableStateOf("") }

    // Session reflection states
    var reflectionMood by remember { mutableStateOf(4) }
    var distractionInput by remember { mutableStateOf("") }
    var reflectionText by remember { mutableStateOf("") }

    val activeReflectionTrigger = viewModel.showReflectionTrigger

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(creamBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Warm Header ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.SelfImprovement,
                    contentDescription = "Mindfulness",
                    tint = softGold,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reflective Journal",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = paperInk
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Nourish your focus with mindfulness & gratitude.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Serif,
                        color = paperInk.copy(alpha = 0.7f)
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }

        // --- Post-Session Reflection GlassSurface(Dynamic) ---
        if (activeReflectionTrigger) {
            item {
                GlassSurface(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .testTag("session_reflection_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Post-Session Reflection",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                )
                            )
                            com.example.ui.components.DepthIconButton(
                                onClick = { viewModel.showReflectionTrigger = false },
                                contentDescription = "Dismiss Reflection"
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Dismiss Reflection", tint = Color.Gray)
                            }
                        }

                        Text(
                            text = "How focused did you feel? (${viewModel.reflectionSessionCategory} session of ${viewModel.reflectionSessionMins} mins)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Serif)
                        )

                        // Mood selector 1-5
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                1 to "😢",
                                2 to "😕",
                                3 to "😐",
                                4 to "🙂",
                                5 to "🌟"
                            ).forEach { (score, emoji) ->
                                val selected = reflectionMood == score
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                        .clickable { reflectionMood = score }
                                        .testTag("reflection_mood_$score"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 24.sp)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = distractionInput,
                            onValueChange = { distractionInput = it },
                            label = { Text("What distracted you? (Phone, web, thoughts)", fontFamily = FontFamily.Serif) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("distraction_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = reflectionText,
                            onValueChange = { reflectionText = it },
                            label = { Text("What can you do better next time?", fontFamily = FontFamily.Serif) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .testTag("reflection_text_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        com.example.ui.components.DepthButton(
                            onClick = {
                                viewModel.saveReflectionToJournal(
                                    mood = reflectionMood,
                                    distractedBy = distractionInput.ifEmpty { "None" },
                                    feedback = reflectionText
                                )
                                distractionInput = ""
                                reflectionText = ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_reflection_button")
                        ) {
                            Text("Save Reflection", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Daily Journal Creator ---
        item {
            GlassSurface(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("journal_creator_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Gratitude header with refresh
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Gratitude",
                                tint = Color.Red.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Gratitude Prompt",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = paperInk
                                )
                            )
                        }
                        com.example.ui.components.DepthIconButton(
                            onClick = {
                                currentPromptIdx = (currentPromptIdx + 1) % prompts.size
                            },
                            contentDescription = "Get New Prompt",
                            modifier = Modifier.testTag("refresh_prompt_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Get New Prompt", tint = softGold)
                        }
                    }

                    // Display actual prompt
                    Text(
                        text = prompts[currentPromptIdx],
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = paperInk.copy(alpha = 0.85f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Mood selector 1-5
                    Text(
                        text = "Current Mood:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Serif,
                            color = paperInk
                        )
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            1 to "😢",
                            2 to "😕",
                            3 to "😐",
                            4 to "🙂",
                            5 to "🌟"
                        ).forEach { (score, emoji) ->
                            val selected = selectedMood == score
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) softGold.copy(alpha = 0.25f) else Color.Transparent)
                                    .clickable { selectedMood = score }
                                    .testTag("journal_mood_$score"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 24.sp)
                            }
                        }
                    }

                    var isPolishing by remember { mutableStateOf(false) }
                    
                    // Text Entry
                    OutlinedTextField(
                        value = journalText,
                        onValueChange = { journalText = it },
                        placeholder = {
                            Text(
                                "Pour your heart out here...",
                                fontFamily = FontFamily.Serif,
                                color = Color.Gray
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("journal_text_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = softGold,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Polish
                        com.example.ui.components.DepthButton(
                            onClick = {
                                if (journalText.isNotEmpty()) {
                                    isPolishing = true
                                    viewModel.analyzeContentGeneral(journalText, "Please gently polish and improve the grammar/flow of this journal entry without changing its personal meaning.") { polished ->
                                        if (polished.isNotBlank() && !polished.startsWith("Error")) {
                                            journalText = polished
                                        }
                                        isPolishing = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = softGold.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            if (isPolishing) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = paperInk,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = paperInk)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Polish",
                                    color = paperInk,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                )
                            }
                        }
                        
                        // Submit
                        com.example.ui.components.DepthButton(
                            onClick = {
                                if (journalText.isNotEmpty()) {
                                    viewModel.addJournalEntry(
                                        mood = selectedMood,
                                        gratitudePrompt = prompts[currentPromptIdx],
                                        textEntry = journalText
                                    )
                                    journalText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = paperInk),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("save_journal_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = creamBg)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Save Entry",
                                color = creamBg,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }
                }
            }
        }

        // --- Diary Ledger (Previous Reflections) ---
        item {
            Text(
                text = "Past Journal Entries",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = paperInk
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (journalEntries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Your journal is empty. Take a moment to reflect on your goals.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = paperInk.copy(alpha = 0.5f)
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(journalEntries) { entry ->
                JournalEntryRow(
                    entry = entry,
                    paperInk = paperInk,
                    softGold = softGold,
                    onDelete = { viewModel.deleteJournalEntry(entry) }
                )
            }
        }
    }
}

@Composable
fun JournalEntryRow(
    entry: JournalEntry,
    paperInk: Color,
    softGold: Color,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
    val formattedDate = sdf.format(Date(entry.timestamp))

    val moodEmojis = mapOf(
        1 to "😢",
        2 to "😕",
        3 to "😐",
        4 to "🙂",
        5 to "🌟"
    )

    GlassSurface(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("journal_entry_${entry.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = moodEmojis[entry.mood] ?: "😐",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (entry.isSessionReflection) "Focus Reflection" else "Daily Gratitude",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = if (entry.isSessionReflection) MaterialTheme.colorScheme.primary else softGold
                            )
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.Gray,
                                fontFamily = FontFamily.Serif
                            )
                        )
                    }
                }
                com.example.ui.components.DepthIconButton(
                    onClick = onDelete,
                    contentDescription = "Delete journal entry"
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete journal entry",
                        tint = Color.Red.copy(alpha = 0.6f)
                    )
                }
            }

            Text(
                text = "Prompt: ${entry.gratitudePrompt}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = paperInk.copy(alpha = 0.6f)
                )
            )

            Text(
                text = entry.textEntry,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Serif,
                    color = paperInk
                )
            )

            if (entry.isSessionReflection && entry.focusMinutes > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "⏱️ ${entry.focusMinutes} min session completed in ${entry.focusCategory}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    )
                }
            }
        }
    }
}
