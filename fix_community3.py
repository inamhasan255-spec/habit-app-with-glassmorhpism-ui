with open("app/src/main/java/com/example/ui/screens/CommunityScreen.kt", "r") as f:
    text = f.read()

# Fix color names based on ViewModel properties
text = text.replace("viewModel.accentColor", "androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(viewModel.accentColorHex))")
text = text.replace("viewModel.themeBackground", "viewModel.themeBg")

# Remove unresolved reference it for sum
text = text.replace("members.map { it.progressHours.toDouble() }.sum().toFloat()",
"members.map { member -> member.progressHours.toDouble() }.sum().toFloat()")

# Fix allCommunityGroups error and getMembersForGroup error by adding explicit import for Flow collectAsState
text = "import androidx.compose.runtime.collectAsState\n" + text

with open("app/src/main/java/com/example/ui/screens/CommunityScreen.kt", "w") as f:
    f.write(text)
