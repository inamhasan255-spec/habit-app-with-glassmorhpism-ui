package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocusViewModel
import com.example.data.ReadingBook
import com.example.ui.components.GlassSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen(
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier
) {
    val books by viewModel.books.collectAsState()
    val activeBook = viewModel.activeReadingBook

    // Sepia palette
    val sepiaBg = Color(0xFF2B2620)
    val sepiaText = Color(0xFFE8DCC4)
    val sepiaCardBg = Color(0xFF3D352E)
    val sepiaPrimary = Color(0xFFD4AF37) // Golden accents
    val sepiaSecondary = Color(0xFFA89F91)

    var showAddBook by remember { mutableStateOf(false) }
    var bookTitle by remember { mutableStateOf("") }
    var bookAuthor by remember { mutableStateOf("") }
    var bookPages by remember { mutableStateOf("") }

    var selectedTimerMinutes by remember { mutableStateOf(25) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(sepiaBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Sepia Header ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "Reading",
                    tint = sepiaPrimary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bibliophile Shelf",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = sepiaText
                    )
                )
                Text(
                    text = "Deep concentration for distraction-free reading.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Serif,
                        color = sepiaSecondary
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }

        // --- Reading Statistics Card ---
        item {
            GlassSurface(
                colors = CardDefaults.cardColors(containerColor = sepiaCardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val booksFinished = books.count { it.isFinished }
                    val activeReading = books.count { !it.isFinished }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = booksFinished.toString(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = sepiaPrimary,
                                fontFamily = FontFamily.Serif
                            )
                        )
                        Text(
                            text = "Finished",
                            style = MaterialTheme.typography.labelSmall.copy(color = sepiaSecondary, fontFamily = FontFamily.Serif)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(sepiaSecondary.copy(alpha = 0.3f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = activeReading.toString(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = sepiaPrimary,
                                fontFamily = FontFamily.Serif
                            )
                        )
                        Text(
                            text = "In Progress",
                            style = MaterialTheme.typography.labelSmall.copy(color = sepiaSecondary, fontFamily = FontFamily.Serif)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(sepiaSecondary.copy(alpha = 0.3f))
                    )

                    val totalPagesRead = books.sumOf { it.currentPage }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = totalPagesRead.toString(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = sepiaPrimary,
                                fontFamily = FontFamily.Serif
                            )
                        )
                        Text(
                            text = "Pages Read",
                            style = MaterialTheme.typography.labelSmall.copy(color = sepiaSecondary, fontFamily = FontFamily.Serif)
                        )
                    }
                }
            }
        }

        // --- Currently Reading Shelf (Horizontal Flow) ---
        item {
            Text(
                text = "Currently Reading",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = sepiaText
                )
            )
        }

        val readingInProg = books.filter { !it.isFinished }
        if (readingInProg.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(sepiaCardBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add a book below to begin your journey.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = sepiaSecondary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontFamily = FontFamily.Serif
                        )
                    )
                }
            }
        } else {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(readingInProg) { book ->
                        val isSelected = activeBook?.id == book.id
                        val borderMod = if (isSelected) {
                            Modifier.border(2.dp, sepiaPrimary, RoundedCornerShape(16.dp))
                        } else Modifier

                        GlassSurface(
                            colors = CardDefaults.cardColors(containerColor = sepiaCardBg),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .width(220.dp)
                                .then(borderMod)
                                .clickable {
                                    viewModel.selectBookForReading(if (isSelected) null else book)
                                }
                                .testTag("book_card_${book.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = book.title,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = FontFamily.Serif,
                                                fontWeight = FontWeight.Bold,
                                                color = sepiaText
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "by ${book.author}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = sepiaSecondary,
                                                fontFamily = FontFamily.Serif
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    com.example.ui.components.DepthIconButton(
                                        onClick = { viewModel.deleteBook(book) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Delete book",
                                            tint = Color.Red.copy(alpha = 0.6f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                val progress = if (book.totalPages > 0) book.currentPage.toFloat() / book.totalPages.toFloat() else 0f
                                LinearProgressIndicator(
                                    progress = { progress },
                                    color = sepiaPrimary,
                                    trackColor = sepiaBg,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${book.currentPage} / ${book.totalPages} pages",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = sepiaSecondary,
                                            fontFamily = FontFamily.Serif
                                        )
                                    )
                                    Text(
                                        text = "${(progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = sepiaPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Serif
                                        )
                                    )
                                }

                                // Interactive progress update
                                var progressInput by remember { mutableStateOf("") }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    OutlinedTextField(
                                        value = progressInput,
                                        onValueChange = { progressInput = it },
                                        placeholder = { Text("Page", fontSize = 10.sp, color = sepiaSecondary) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = sepiaText,
                                            unfocusedTextColor = sepiaText,
                                            focusedBorderColor = sepiaPrimary,
                                            unfocusedBorderColor = sepiaSecondary.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .testTag("progress_input_${book.id}")
                                    )
                                    com.example.ui.components.DepthIconButton(
                                        onClick = {
                                            val pg = progressInput.toIntOrNull()
                                            if (pg != null && pg >= 0) {
                                                viewModel.updateBookProgress(book, pg)
                                                progressInput = ""
                                            }
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(sepiaPrimary, RoundedCornerShape(8.dp))
                                            .testTag("save_progress_${book.id}")
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Update", tint = sepiaBg, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Distraction-Free Reading Timer ---
        item {
            GlassSurface(
                colors = CardDefaults.cardColors(containerColor = sepiaCardBg),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reading_timer_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Zen Reading Timer",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = sepiaText
                        )
                    )

                    if (activeBook != null) {
                        Text(
                            text = "Focusing on: \"${activeBook.title}\"",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = sepiaPrimary,
                                fontFamily = FontFamily.Serif
                            ),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "Select a book from shelf to couple focus logs.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = sepiaSecondary,
                                fontFamily = FontFamily.Serif
                            ),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Circular Countdown Timer Display
                    val isRunning = viewModel.isReadingTimerRunning
                    val secondsLeft = viewModel.readingTimerSecondsLeft
                    val minutes = secondsLeft / 60
                    val seconds = secondsLeft % 60
                    val timerDisplayString = String.format("%02d:%02d", minutes, seconds)

                    Text(
                        text = timerDisplayString,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = sepiaText,
                            fontFamily = FontFamily.Serif
                        )
                    )

                    if (!isRunning) {
                        // Segmented Duration Selector
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            listOf(10, 15, 25, 45).forEach { mins ->
                                val selected = selectedTimerMinutes == mins
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedTimerMinutes = mins },
                                    label = { Text("$mins min", fontFamily = FontFamily.Serif) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = sepiaPrimary,
                                        selectedLabelColor = sepiaBg,
                                        containerColor = sepiaBg,
                                        labelColor = sepiaText
                                    )
                                )
                            }
                        }

                        com.example.ui.components.DepthButton(
                            onClick = { viewModel.startReadingSession(selectedTimerMinutes) },
                            colors = ButtonDefaults.buttonColors(containerColor = sepiaPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("start_reading_timer")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = sepiaBg)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Enter Sanctuary Mode", fontWeight = FontWeight.Bold, color = sepiaBg, fontFamily = FontFamily.Serif)
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            com.example.ui.components.DepthButton(
                                onClick = { viewModel.pauseReadingSession() },
                                colors = ButtonDefaults.buttonColors(containerColor = sepiaSecondary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("pause_reading_timer")
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null, tint = sepiaBg)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pause", color = sepiaBg, fontFamily = FontFamily.Serif)
                            }

                            com.example.ui.components.DepthButton(
                                onClick = { viewModel.stopReadingSession() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("stop_reading_timer")
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Stop", color = Color.White, fontFamily = FontFamily.Serif)
                            }
                        }
                        
                        Text(
                            text = "🛡️ Focus Shield active — Distracting apps are blocked.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = sepiaPrimary,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // --- Add Book Drawer Trigger ---
        item {
            com.example.ui.components.DepthButton(
                onClick = { showAddBook = !showAddBook },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, sepiaPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .testTag("toggle_add_book_button")
            ) {
                Icon(
                    imageVector = if (showAddBook) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = null,
                    tint = sepiaPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showAddBook) "Dismiss Creator" else "Acquire New Volume",
                    color = sepiaPrimary,
                    fontFamily = FontFamily.Serif
                )
            }
        }

        item {
            AnimatedVisibility(
                visible = showAddBook,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                GlassSurface(
                    colors = CardDefaults.cardColors(containerColor = sepiaCardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_book_form")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = bookTitle,
                            onValueChange = { bookTitle = it },
                            label = { Text("Volume Title", color = sepiaSecondary, fontFamily = FontFamily.Serif) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("book_title_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = sepiaText,
                                unfocusedTextColor = sepiaText,
                                focusedBorderColor = sepiaPrimary,
                                unfocusedBorderColor = sepiaSecondary.copy(alpha = 0.5f)
                            )
                        )

                        OutlinedTextField(
                            value = bookAuthor,
                            onValueChange = { bookAuthor = it },
                            label = { Text("Author", color = sepiaSecondary, fontFamily = FontFamily.Serif) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("book_author_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = sepiaText,
                                unfocusedTextColor = sepiaText,
                                focusedBorderColor = sepiaPrimary,
                                unfocusedBorderColor = sepiaSecondary.copy(alpha = 0.5f)
                            )
                        )

                        OutlinedTextField(
                            value = bookPages,
                            onValueChange = { bookPages = it },
                            label = { Text("Total Pages", color = sepiaSecondary, fontFamily = FontFamily.Serif) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("book_pages_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = sepiaText,
                                unfocusedTextColor = sepiaText,
                                focusedBorderColor = sepiaPrimary,
                                unfocusedBorderColor = sepiaSecondary.copy(alpha = 0.5f)
                            )
                        )

                        com.example.ui.components.DepthButton(
                            onClick = {
                                val pg = bookPages.toIntOrNull()
                                if (bookTitle.isNotEmpty() && bookAuthor.isNotEmpty() && pg != null && pg > 0) {
                                    viewModel.addBook(bookTitle, bookAuthor, pg)
                                    bookTitle = ""
                                    bookAuthor = ""
                                    bookPages = ""
                                    showAddBook = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = sepiaPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("save_book_button")
                        ) {
                            Text("Place on Shelf", fontWeight = FontWeight.Bold, color = sepiaBg, fontFamily = FontFamily.Serif)
                        }
                    }
                }
            }
        }

        // --- Finished Volumes Shelf ---
        item {
            Text(
                text = "Library Archives (Finished Books)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = sepiaText
                )
            )
        }

        val finishedBooks = books.filter { it.isFinished }
        if (finishedBooks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No completed volumes yet. Finish books to display them in archives.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = sepiaSecondary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontFamily = FontFamily.Serif
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(finishedBooks) { book ->
                GlassSurface(
                    colors = CardDefaults.cardColors(containerColor = sepiaCardBg.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("archive_book_${book.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = "Finished", tint = sepiaPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = book.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif,
                                        color = sepiaText
                                    )
                                )
                                Text(
                                    text = "by ${book.author}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = sepiaSecondary,
                                        fontFamily = FontFamily.Serif
                                    )
                                )
                            }
                        }
                        com.example.ui.components.DepthIconButton(onClick = { viewModel.deleteBook(book) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete book", tint = Color.Red.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}
