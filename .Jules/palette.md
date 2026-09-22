## 2025-05-20 - Accessibility Role and State for Custom Toggle Components
**Learning:** Custom Compose components like `GlassToggle` using `clickable` without `Role.Switch` or semantics state (`toggleable` or `Role.Switch` semantics) are opaque to TalkBack screen readers and keyboard users.
**Action:** When creating or refining custom interactive UI components in Jetpack Compose, use `Modifier.semantics` with `Role.Switch`, `toggleableState`, and `contentDescription` / `stateDescription` to ensure complete screen reader accessibility and correct ARIA/accessibility node properties.
