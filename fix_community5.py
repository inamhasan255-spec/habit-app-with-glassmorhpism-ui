with open("app/src/main/java/com/example/ui/screens/CommunityScreen.kt", "r") as f:
    text = f.read()

# Fix Unresolved reference 'Groups' in MainActivity
with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    main = f.read()

main = main.replace("import androidx.compose.material.icons.filled.Warning", "import androidx.compose.material.icons.filled.Warning\nimport androidx.compose.material.icons.filled.Groups")
with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(main)

# Re-expose the viewModel repository. It didn't seem to apply correctly earlier or something else is wrong.
with open("app/src/main/java/com/example/data/FocusViewModel.kt", "r") as f:
    vm = f.read()

vm = vm.replace("private val repository: FocusRepository", "val repository: FocusRepository")
with open("app/src/main/java/com/example/data/FocusViewModel.kt", "w") as f:
    f.write(vm)

