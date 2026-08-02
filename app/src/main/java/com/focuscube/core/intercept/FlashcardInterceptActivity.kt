package com.focuscube.core.intercept

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.focuscube.data.local.FocusCubeDatabase
import com.focuscube.data.model.Flashcard
import kotlinx.coroutines.launch

class FlashcardInterceptActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // This makes the activity transparent if it has a translucent theme
        
        val targetPackage = intent.getStringExtra("target_package") ?: ""
        val db = FocusCubeDatabase.getDatabase(this)
        val tokenManager = TokenManager.getInstance(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                ejectUser()
            }
        })
        
        setContent {
            MaterialTheme {
                var flashcard by remember { mutableStateOf<Flashcard?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                val scope = rememberCoroutineScope()
                
                LaunchedEffect(Unit) {
                    val card = db.flashcardDao().getRandomFlashcard()
                    if (card == null) {
                        // Fallback card if database is empty
                        flashcard = Flashcard(
                            question = "What is the powerhouse of the cell?",
                            correctAnswer = "Mitochondria",
                            distractors = listOf("Nucleus", "Ribosome", "Endoplasmic Reticulum"),
                            associatedCourseId = "fallback"
                        )
                    } else {
                        flashcard = card
                    }
                    isLoading = false
                }

                if (!isLoading && flashcard != null) {
                    FlashcardOverlay(
                        flashcard = flashcard!!,
                        onCorrect = {
                            tokenManager.grantToken()
                            finish()
                        },
                        onWrong = {
                            ejectUser()
                        }
                    )
                }
            }
        }
    }
    
    private fun ejectUser() {
        val intent = Intent("com.focuscube.ACTION_EJECT")
        intent.setPackage(packageName)
        sendBroadcast(intent)
        finish()
    }
}

@Composable
fun FlashcardOverlay(
    flashcard: Flashcard,
    onCorrect: () -> Unit,
    onWrong: () -> Unit
) {
    val options = remember(flashcard) {
        (flashcard.distractors + flashcard.correctAnswer).shuffled()
    }

    // We can use a Dialog to make it look like an overlay over the app
    Dialog(
        onDismissRequest = { onWrong() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Dopamine Intercept",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = flashcard.question,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                options.forEach { option ->
                    com.example.ui.components.GlassButton(
                        onClick = {
                            if (option == flashcard.correctAnswer) {
                                onCorrect()
                            } else {
                                onWrong()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = option,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                com.example.ui.components.GlassButton(variant = com.example.ui.components.GlassButtonVariant.Secondary, onClick = onWrong) {
                    Text("Skip & Close App")
                }
            }
        }
    }
}
