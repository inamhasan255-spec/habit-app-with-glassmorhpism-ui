package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocusViewModel
import com.example.ui.theme.softGlassShadow

/**
 * A beautiful, reusable text input field styled with a 'soft glass' aesthetic
 * using semi-transparent white backgrounds and subtle border glow effects to match the Lumina theme.
 */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    viewModel: FocusViewModel
) {
    val isDark = viewModel.isDarkMode
    
    // Lumina: High-end transparent white background with subtle border and shadow glow
    val glassBg = if (isDark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color.White.copy(alpha = 0.65f)
    }
    
    val borderBrush = if (isDark) {
        Color.White.copy(alpha = 0.18f)
    } else {
        Color.Black.copy(alpha = 0.12f)
    }

    val focusedBorderBrush = if (isDark) {
        Color(0xFF38BDF8).copy(alpha = 0.7f) // Cyan glow in dark mode
    } else {
        Color(0xFF4F46E5).copy(alpha = 0.7f) // Indigo glow in light mode
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = glassBg,
            unfocusedContainerColor = glassBg,
            focusedBorderColor = focusedBorderBrush,
            unfocusedBorderColor = borderBrush,
            focusedTextColor = viewModel.themeText,
            unfocusedTextColor = viewModel.themeText,
            focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF4F46E5),
            unfocusedLabelColor = viewModel.themeTextSecondary
        ),
        modifier = modifier
            .softGlassShadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                isDark = isDark
            )
    )
}
