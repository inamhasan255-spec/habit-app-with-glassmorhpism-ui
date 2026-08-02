package com.example.ui.widget

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocusViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ImageAnalyzerWidget : WidgetSpec {
    override val id = "image_analyzer"
    override val title = "Analyze images"
    override val description = "Analyze images using Gemini"
    override val category = "UNIVERSAL"
    override val iconName = "DocumentScanner"

    @Composable
    override fun Render(
        viewModel: FocusViewModel,
        accentColor: Color,
        scope: CoroutineScope,
        selectedDateStr: String
    ) {
        var resultText by remember { mutableStateOf("") }
        var isAnalyzing by remember { mutableStateOf(false) }
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var promptText by remember { mutableStateOf("") }

        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            imageUri = uri
        }

        Column {
            com.example.ui.components.GlassButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(containerColor = viewModel.themeSurface),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(text = if (imageUri == null) "Select Image" else "Image Selected", color = viewModel.themeText)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = promptText,
                onValueChange = { promptText = it },
                label = { Text("What do you want to know about this image?", color = viewModel.themeTextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = viewModel.themeGlassBorder,
                    focusedTextColor = viewModel.themeText,
                    unfocusedTextColor = viewModel.themeText
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            com.example.ui.components.GlassButton(
                onClick = {
                    val uri = imageUri
                    if (uri != null) {
                        isAnalyzing = true
                        viewModel.analyzeImageWithGemini(promptText, uri.toString()) { result ->
                            resultText = result
                            isAnalyzing = false
                        }
                    }
                },
                enabled = !isAnalyzing && imageUri != null,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(if (isAnalyzing) "Analyzing..." else "Analyze Image", color = Color.White)
            }
            
            if (resultText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = resultText, color = viewModel.themeText, fontSize = 14.sp)
            }
        }
    }
}
