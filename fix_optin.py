with open("app/src/main/java/com/example/ui/screens/HomeDashboardScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    "@OptIn(ExperimentalMaterial3Api::class)\n@OptIn(ExperimentalSharedTransitionApi::class)",
    "@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)"
)

with open("app/src/main/java/com/example/ui/screens/HomeDashboardScreen.kt", "w") as f:
    f.write(content)
