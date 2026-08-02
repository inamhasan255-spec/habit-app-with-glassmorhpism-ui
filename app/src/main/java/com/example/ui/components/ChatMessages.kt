package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ChatMessageItem(
    val id: String,
    val sender: String, // "user" or "assistant"
    val content: String
)

val defaultMessages = listOf(
    ChatMessageItem("1", "assistant", "Hello! I'm your Nexus AI assistant. How can I help you build something amazing today?"),
    ChatMessageItem("2", "user", "I want to create a beautiful landing page for my SaaS product."),
    ChatMessageItem("3", "assistant", "Great choice! Nexus UI has everything you need — animated components, premium styling, and easy integration. What feature would you like to showcase first?"),
    ChatMessageItem("4", "user", "I need something that will impress investors during pitch meetings."),
    ChatMessageItem("5", "assistant", "Perfect. I'd recommend our Hero Aurora Spotlight combined with the Cinematic Logo Cloud. They create a stunning first impression that showcases both innovation and credibility."),
    ChatMessageItem("6", "user", "Can I see how they look together?"),
    ChatMessageItem("7", "assistant", "Absolutely. Click any component in the sidebar to preview it live, then copy the code directly into your project. No complex setup required.")
)

@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "typing")
    val dot1 by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "dot1"
    )
    val dot2 by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing, delayMillis = 150),
            repeatMode = RepeatMode.Reverse
        ), label = "dot2"
    )
    val dot3 by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing, delayMillis = 300),
            repeatMode = RepeatMode.Reverse
        ), label = "dot3"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
            .background(Color(0xFF27272A).copy(alpha = 0.9f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).offset(y = (-4 * dot1).dp).clip(CircleShape).background(Color.White.copy(alpha = 0.6f + 0.4f * dot1)))
        Box(modifier = Modifier.size(8.dp).offset(y = (-4 * dot2).dp).clip(CircleShape).background(Color.White.copy(alpha = 0.6f + 0.4f * dot2)))
        Box(modifier = Modifier.size(8.dp).offset(y = (-4 * dot3).dp).clip(CircleShape).background(Color.White.copy(alpha = 0.6f + 0.4f * dot3)))
    }
}

@Composable
fun MessageBubble(message: ChatMessageItem) {
    val isUser = message.sender == "user"
    
    // Animate presence
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 2 }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF7C3AED)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clip(
                        RoundedCornerShape(
                            topStart = if (isUser) 16.dp else 4.dp,
                            topEnd = if (isUser) 4.dp else 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .background(
                        if (isUser) Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF7C3AED)))
                        else SolidColor(Color(0xFF27272A).copy(alpha = 0.9f))
                    )
                    .then(
                        if (!isUser) Modifier.border(
                            1.dp,
                            Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                        ) else Modifier
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.content,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun ChatMessagesComponent(
    messages: List<ChatMessageItem> = defaultMessages,
    autoPlay: Boolean = true,
    autoPlayDelay: Long = 1800,
    typingDuration: Long = 1400,
    showReplay: Boolean = true,
    interactive: Boolean = false,
    modifier: Modifier = Modifier
) {
    var visibleCount by remember { mutableStateOf(if (autoPlay) 0 else messages.size) }
    var isTyping by remember { mutableStateOf(false) }
    var inputValue by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(messages) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    var isAutoPlaying by remember { mutableStateOf(autoPlay) }

    val revealNext: suspend (Int) -> Unit = { index ->
        if (index < chatMessages.size && isAutoPlaying) {
            val message = chatMessages[index]
            if (message.sender == "assistant") {
                isTyping = true
                delay(typingDuration)
                isTyping = false
            }
            visibleCount = index + 1
            delay(100)
            listState.animateScrollToItem(chatMessages.size)
            
            delay(autoPlayDelay - (if (message.sender == "assistant") typingDuration else 0) - 100)
        } else {
            isAutoPlaying = false
        }
    }

    LaunchedEffect(chatMessages, autoPlay, isAutoPlaying) {
        if (isAutoPlaying && visibleCount < chatMessages.size) {
            revealNext(visibleCount)
        }
    }

    val handleSend = {
        if (inputValue.isNotBlank() && interactive) {
            val userMsg = ChatMessageItem("user-${System.currentTimeMillis()}", "user", inputValue.trim())
            chatMessages = chatMessages + userMsg
            inputValue = ""
            visibleCount++
            scope.launch {
                delay(100)
                listState.animateScrollToItem(chatMessages.size)
                isTyping = true
                delay(typingDuration)
                isTyping = false
                val astMsg = ChatMessageItem("ast-${System.currentTimeMillis()}", "assistant", "That's a great question! Let me help you explore the best options for your use case.")
                chatMessages = chatMessages + astMsg
                visibleCount++
                delay(100)
                listState.animateScrollToItem(chatMessages.size)
            }
        }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF18181B), Color(0xFF09090B))))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.05f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF7C3AED)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Nexus AI", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("Always here to help", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                }
            }
            if (showReplay) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable {
                            visibleCount = 0
                            chatMessages = messages
                            isAutoPlaying = true
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Replay", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Replay", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            }
        }

        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(chatMessages.take(visibleCount)) { _, msg ->
                MessageBubble(msg)
            }
            if (isTyping) {
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.width(40.dp))
                        TypingIndicator()
                    }
                }
            }
        }

        // Input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.05f))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF27272A).copy(alpha = 0.5f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    enabled = interactive,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { handleSend() }),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (inputValue.isEmpty()) {
                            Text(
                                text = if (interactive) "Ask Nexus AI..." else "Demo mode - replay to watch again",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { handleSend() },
                    enabled = interactive && inputValue.isNotBlank(),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (interactive && inputValue.isNotBlank()) Color(0xFF4F46E5) else Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = if (interactive && inputValue.isNotBlank()) Color.White else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
