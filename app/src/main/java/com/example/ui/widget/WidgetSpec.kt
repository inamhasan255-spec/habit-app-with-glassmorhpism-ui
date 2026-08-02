package com.example.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.FocusViewModel
import kotlinx.coroutines.CoroutineScope

interface WidgetSpec {
    val id: String
    val title: String
    val description: String
    val category: String // "STUDENT", "ADULT", "UNIVERSAL"
    val iconName: String

    @Composable
    fun Render(
        viewModel: FocusViewModel,
        accentColor: Color,
        scope: CoroutineScope,
        selectedDateStr: String
    )
}
