## 2024-05-20 - Jetpack Compose Icon Buttons & Input Accessibility
**Learning:** Icon buttons inside interactive Jetpack Compose lists and text fields often default to generic content descriptions ("Toggle Complete", "Add Task") or miss KeyboardActions (`ImeAction.Done`), impairing both screen reader accessibility and soft keyboard usability.
**Action:** Always provide task-contextual `contentDescription` strings (e.g., "Mark task 'X' as complete") and attach `keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)` with `keyboardActions` on `OutlinedTextField`.
