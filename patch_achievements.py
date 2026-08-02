with open("app/src/main/java/com/example/ui/screens/StatsScreen.kt", "r") as f:
    content = f.read()

# We need to replace AchievementsStatsTab and AchievementCard entirely

pattern = r"@Composable\nfun AchievementsStatsTab.*?fun AchievementCard.*?\}\n\n"

replacement = """@Composable
fun AchievementsStatsTab(viewModel: FocusViewModel) {
    val userStats by viewModel.userStats.collectAsState()
    val badges by viewModel.allBadges.collectAsState()
    val accentColor = Color(android.graphics.Color.parseColor(viewModel.accentColorHex))
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Points and Level Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(viewModel.themeGlassFill)
                .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Level ${userStats?.level ?: 1}", color = accentColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("${userStats?.points ?: 0} Total Points", color = viewModel.themeTextSecondary, fontSize = 14.sp)
                }
                Icon(Icons.Default.TrendingUp, contentDescription = "Points", tint = accentColor, modifier = Modifier.size(48.dp))
            }
        }

        // Badges Section
        Text("Your Badges", color = viewModel.themeText, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))

        badges.forEachIndexed { index, badge ->
            BadgeCard(badge, viewModel, index, accentColor) {
                // Share action
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "I just unlocked the ${badge.name} badge in FocusCube! ${badge.iconResName} #FocusCube #Productivity")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Badge"))
            }
        }
    }
}

@Composable
fun BadgeCard(badge: com.example.data.Badge, viewModel: FocusViewModel, index: Int = 0, accentColor: Color, onShare: () -> Unit) {
    val revealState = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 100L)
        revealState.animateTo(1f, animationSpec = tween(500))
    }
    
    val pulseTransition = rememberInfiniteTransition()
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val scale = 0.9f + (0.1f * revealState.value)
    val alpha = revealState.value

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(alpha)
            .clip(RoundedCornerShape(20.dp))
            .background(viewModel.themeGlassFill)
            .border(
                width = if (badge.unlocked) 2.dp else 1.dp,
                color = if (badge.unlocked) accentColor.copy(alpha = pulseAlpha) else viewModel.themeGlassBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (badge.unlocked) accentColor.copy(alpha = 0.2f) else viewModel.themeGlassBorder.copy(alpha=0.5f)),
                contentAlignment = Alignment.Center
            ) {
                if (badge.unlocked) {
                    Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(accentColor.copy(alpha = pulseAlpha * 0.4f)))
                }
                Text(badge.iconResName, fontSize = 24.sp) // Using iconResName as emoji for simplicity
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = badge.name,
                    color = if (badge.unlocked) accentColor else viewModel.themeText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = badge.description,
                    color = viewModel.themeTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (badge.unlocked) {
                IconButton(onClick = onShare) {
                    Icon(androidx.compose.material.icons.Icons.Default.Share, contentDescription = "Share", tint = accentColor)
                }
            }
        }
    }
}

@Composable
fun LeaderboardStatsTab(viewModel: FocusViewModel) {
    val accentColor = Color(android.graphics.Color.parseColor(viewModel.accentColorHex))
    val userStats by viewModel.userStats.collectAsState()
    
    val leaderboard = listOf(
        com.example.data.LeaderboardEntry(1, "Alex", 2450),
        com.example.data.LeaderboardEntry(2, "Sarah", 2100),
        com.example.data.LeaderboardEntry(3, "You", userStats?.points ?: 0, isCurrentUser = true),
        com.example.data.LeaderboardEntry(4, "Mike", 1800),
        com.example.data.LeaderboardEntry(5, "Emma", 1550)
    ).sortedByDescending { it.points }.mapIndexed { index, entry -> entry.copy(rank = index + 1) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Global Leaderboard", color = viewModel.themeText, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        
        leaderboard.forEach { entry ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (entry.isCurrentUser) accentColor.copy(alpha = 0.15f) else viewModel.themeGlassFill)
                    .border(1.dp, if (entry.isCurrentUser) accentColor else viewModel.themeGlassBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${entry.rank}",
                        color = if (entry.isCurrentUser) accentColor else viewModel.themeTextSecondary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(40.dp)
                    )
                    
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(viewModel.themeGlassBorder), contentAlignment = Alignment.Center) {
                        Text(entry.userName.first().toString(), color = viewModel.themeText, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = entry.userName,
                        color = if (entry.isCurrentUser) accentColor else viewModel.themeText,
                        fontSize = 16.sp,
                        fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = "${entry.points} pts",
                        color = viewModel.themeText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
"""

import re
# We need to replace from "fun AchievementsStatsTab(viewModel: FocusViewModel) {" 
# to the end of the file, since AchievementCard was the last thing.
# Let's just use regex to match from @Composable fun AchievementsStatsTab to the end.

content = re.sub(r"@Composable\s*fun AchievementsStatsTab\(viewModel: FocusViewModel\).*?\}\s*\}\s*\}", replacement, content, flags=re.DOTALL)

# Add missing import for Icons.Default.Share
if "import androidx.compose.material.icons.filled.Share" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Timer", "import androidx.compose.material.icons.filled.Timer\nimport androidx.compose.material.icons.filled.Share")

with open("app/src/main/java/com/example/ui/screens/StatsScreen.kt", "w") as f:
    f.write(content)
