package com.example.ui.screens

import kotlinx.coroutines.flow.Flow

import androidx.compose.runtime.collectAsState

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CommunityGroup
import com.example.data.CommunityMember
import com.example.data.FocusViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun CommunityScreen(viewModel: FocusViewModel) {
    val accentColor = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(viewModel.accentColorHex))
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showJoinGroupDialog by remember { mutableStateOf(false) }
    
    val allGroups by viewModel.repository.allCommunityGroups.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Community",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = viewModel.themeText
                )
                Text(
                    text = "Accountability & Shared Goals",
                    fontSize = 16.sp,
                    color = viewModel.themeTextSecondary
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { showJoinGroupDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = viewModel.themeGlassFill,
                        contentColor = viewModel.themeText
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.AddLink, contentDescription = "Join")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Join")
                }
                
                Button(
                    onClick = { showCreateGroupDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Group")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (allGroups.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "No Groups",
                        modifier = Modifier.size(64.dp),
                        tint = viewModel.themeTextSecondary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "You haven't joined any groups yet",
                        color = viewModel.themeTextSecondary,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(allGroups) { group ->
                    CommunityGroupCard(viewModel, group, accentColor)
                }
            }
        }
    }
    
    if (showCreateGroupDialog) {
        CreateGroupDialog(
            viewModel = viewModel,
            onDismiss = { showCreateGroupDialog = false },
            onCreate = { name, desc, goal ->
                coroutineScope.launch {
                    val code = java.util.UUID.randomUUID().toString().substring(0, 8).uppercase()
                    val group = CommunityGroup(
                        name = name,
                        description = desc,
                        sharedGoalHours = goal,
                        joinCode = code
                    )
                    val groupId = viewModel.repository.insertCommunityGroup(group).toInt()
                    
                    // Add self as member
                    val selfHours = viewModel.getFocusTimeHours()
                    viewModel.repository.insertCommunityMember(
                        CommunityMember(
                            groupId = groupId,
                            name = "You",
                            isMe = true,
                            progressHours = selfHours.toFloat()
                        )
                    )
                }
                showCreateGroupDialog = false
            }
        )
    }
    
    if (showJoinGroupDialog) {
        JoinGroupDialog(
            viewModel = viewModel,
            onDismiss = { showJoinGroupDialog = false },
            onJoin = { code ->
                // In a real app this would query a backend. For now we'll just mock it
                coroutineScope.launch {
                    val group = CommunityGroup(
                        name = "Study Group ($code)",
                        description = "Joined via code",
                        sharedGoalHours = 20,
                        joinCode = code
                    )
                    val groupId = viewModel.repository.insertCommunityGroup(group).toInt()
                    
                    // Add self as member
                    val selfHours = viewModel.getFocusTimeHours()
                    viewModel.repository.insertCommunityMember(
                        CommunityMember(
                            groupId = groupId,
                            name = "You",
                            isMe = true,
                            progressHours = selfHours.toFloat()
                        )
                    )
                    
                    // Add mock members
                    viewModel.repository.insertCommunityMember(
                        CommunityMember(
                            groupId = groupId,
                            name = "Alice",
                            isMe = false,
                            progressHours = 14.5f
                        )
                    )
                    viewModel.repository.insertCommunityMember(
                        CommunityMember(
                            groupId = groupId,
                            name = "Bob",
                            isMe = false,
                            progressHours = 8.2f
                        )
                    )
                }
                showJoinGroupDialog = false
            }
        )
    }
}

@Composable
fun CommunityGroupCard(
    viewModel: FocusViewModel,
    group: CommunityGroup,
    accentColor: Color
) {
    val members by viewModel.repository.getMembersForGroup(group.id).collectAsState(initial = emptyList())
    val totalHours = members.map { member -> member.progressHours.toDouble() }.sum().toFloat()
    val goal = group.sharedGoalHours.toFloat()
    val progress = if (goal > 0) (totalHours / goal).coerceIn(0f, 1f) else 0f
    
    Card(
        colors = CardDefaults.cardColors(containerColor = viewModel.themeGlassFill),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = group.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = viewModel.themeText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = group.description,
                        fontSize = 14.sp,
                        color = viewModel.themeTextSecondary
                    )
                }
                
                Surface(
                    color = accentColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Code: ${group.joinCode}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Group Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Group Goal Progress", color = viewModel.themeText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("${String.format("%.1f", totalHours)} / ${group.sharedGoalHours} hrs", color = viewModel.themeTextSecondary, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = accentColor,
                trackColor = viewModel.themeGlassBorder
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Members Leaderboard", color = viewModel.themeText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                members.sortedByDescending { it.progressHours }.forEachIndexed { index, member ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = if (index < 3) accentColor else viewModel.themeTextSecondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(24.dp)
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (member.isMe) accentColor else viewModel.themeGlassBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = member.name.take(1).uppercase(),
                                color = if (member.isMe) Color.White else viewModel.themeText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = member.name + (if (member.isMe) " (You)" else ""),
                            color = viewModel.themeText,
                            fontWeight = if (member.isMe) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = "${String.format("%.1f", member.progressHours)}h",
                            color = viewModel.themeTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateGroupDialog(
    viewModel: FocusViewModel,
    onDismiss: () -> Unit,
    onCreate: (name: String, desc: String, goal: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var goalStr by remember { mutableStateOf("10") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = viewModel.themeBg,
        title = { Text("Create Group", color = viewModel.themeText) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = viewModel.themeText,
                        unfocusedTextColor = viewModel.themeText
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = viewModel.themeText,
                        unfocusedTextColor = viewModel.themeText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = goalStr,
                    onValueChange = { goalStr = it },
                    label = { Text("Shared Goal (Hours)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = viewModel.themeText,
                        unfocusedTextColor = viewModel.themeText
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val goal = goalStr.toIntOrNull() ?: 10
                    onCreate(name, desc, goal)
                },
                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(viewModel.accentColorHex))),
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = viewModel.themeTextSecondary)
            }
        }
    )
}

@Composable
fun JoinGroupDialog(
    viewModel: FocusViewModel,
    onDismiss: () -> Unit,
    onJoin: (code: String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = viewModel.themeBg,
        title = { Text("Join Group", color = viewModel.themeText) },
        text = {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Enter Invite Code") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = viewModel.themeText,
                    unfocusedTextColor = viewModel.themeText
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onJoin(code.uppercase()) },
                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(viewModel.accentColorHex))),
                enabled = code.isNotBlank()
            ) {
                Text("Join")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = viewModel.themeTextSecondary)
            }
        }
    )
}
