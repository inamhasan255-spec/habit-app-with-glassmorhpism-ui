

package com.example.ui.screens
import com.example.ui.components.GlassToggle


import androidx.compose.animation.*

import androidx.compose.animation.core.*

import androidx.compose.foundation.background

import androidx.compose.foundation.border

import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.*

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Brush

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import com.example.data.FocusViewModel

@Composable
fun LuminaFloatingSoundControl(viewModel: FocusViewModel) {
    val activeSound = viewModel.selectedAmbientSound
    val isPlaying = viewModel.isWhiteNoisePlayingAnytime
    val accentColor = Color(android.graphics.Color.parseColor(viewModel.accentColorHex))
    
    Box(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Soundscapes",
                    color = viewModel.themeText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                GlassToggle(
                    checked = isPlaying,
                    onCheckedChange = { viewModel.toggleWhiteNoisePlayingAnytime() },
                    checkedTrackColor = accentColor
                )
            }
            
            val sounds = listOf("Rain", "Forest", "Cafe", "Brown Noise", "Comfort Chain")
            sounds.forEach { sound ->
                val isSelected = activeSound == sound
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { 
                            viewModel.selectAmbientSound(sound)
                            if(!isPlaying) viewModel.toggleWhiteNoisePlayingAnytime()
                        }
                        .background(if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (sound == "Rain") Icons.Default.WaterDrop else if (sound == "Forest") Icons.Default.Park else if(sound == "Cafe") Icons.Default.LocalCafe else Icons.Default.GraphicEq,
                        contentDescription = sound,
                        tint = if (isSelected) accentColor else viewModel.themeTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = sound,
                        color = if (isSelected) accentColor else viewModel.themeText,
                        fontSize = 12.sp,
                        fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LuminaMarkdownNotesSheet(viewModel: FocusViewModel, onDismiss: () -> Unit) {
    val accentColor = Color(android.graphics.Color.parseColor(viewModel.accentColorHex))
    
    // Simulate fetching linked notes from local markdown files
    val mockNotes = listOf(
        "Algorithms_CS101.md" to "# Big O Notation\n\nO(1) - Constant\nO(N) - Linear\nO(N^2) - Quadratic\n\nAlways optimize for time complexity.",
        "Quantum_Physics_Notes.md" to "# Schrodinger's Cat\n\nThe cat is both alive and dead until observed. A metaphor for quantum superposition.",
        "Daily_Journal.md" to "# Reflection\n\nToday I focused well during the 90 min deep work session. Need to read more on Jetpack Compose."
    )
    
    var selectedNote by remember { mutableStateOf<Pair<String, String>?>(null) }
    var aiSummary by remember { mutableStateOf<String?>(null) }
    var isSummarizing by remember { mutableStateOf(false) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = viewModel.themeBg,
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Local Markdown Notes",
                color = viewModel.themeText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (selectedNote != null) {
                // Display Note Content
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                    com.example.ui.components.GlassIconButton(
                        onClick = {
                            selectedNote = null
                            aiSummary = null
                            isSummarizing = false
                        },
                        contentDescription = "Back"
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = viewModel.themeText)
                    }
                    Text(
                        text = selectedNote!!.first,
                        color = accentColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (isSummarizing) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = accentColor,
                            strokeWidth = 2.dp
                        )
                    } else {
                        com.example.ui.components.GlassIconButton(onClick = {
                            isSummarizing = true
                            viewModel.summarizeTextFast(selectedNote!!.second) { summary ->
                                aiSummary = summary
                                isSummarizing = false
                            }
                        }) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Summarize", tint = accentColor)
                        }
                    }
                }
                
                if (aiSummary != null) {
                    Box(
        modifier = Modifier
            .fillMaxWidth()
                            
            .clip(RoundedCornerShape(16.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(16.dp))
            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accentColor.copy(alpha = 0.1f))
                            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Summary (flash-lite)", color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = aiSummary!!,
                                color = viewModel.themeText,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
            .padding(16.dp)
    ) {
                    Text(
                        text = selectedNote!!.second,
                        color = viewModel.themeText,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            } else {
                // List Notes
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(mockNotes) { note ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(viewModel.themeGlassFill)
                                .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(16.dp))
                                .clickable { selectedNote = note }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(accentColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Description, contentDescription = "MD File", tint = accentColor)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = note.first,
                                    color = viewModel.themeText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${note.second.length} bytes · Local Storage",
                                    color = viewModel.themeTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
