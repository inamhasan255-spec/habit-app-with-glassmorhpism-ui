# Palette's Journal - Critical Learnings Only

## 2026-10-13 - Toggle and Switch Component Accessibility in Jetpack Compose
**Learning:** Custom toggle/switch UI components (such as `GlassToggle`) in Jetpack Compose should use `Modifier.toggleable` with `Role.Switch` instead of `Modifier.clickable`. Using `.clickable` causes TalkBack/screen readers to announce the element as a standard button and omits the toggle state (on/off). Providing an optional `contentDescription` parameter mapped to semantics allows clear screen reader labeling without interfering with visual layout.
**Action:** When building or refactoring custom toggle or switch controls in Compose, always attach `Modifier.toggleable(value = checked, role = Role.Switch, ...)` and expose a semantic `contentDescription`.
