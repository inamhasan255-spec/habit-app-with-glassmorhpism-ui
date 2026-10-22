# Palette's Journal - Critical Learnings

## 2025-02-17 - Custom Compose Toggles and Accessibility Semantics
**Learning:** Custom UI toggle controls implemented with `Modifier.clickable` fail to announce their role or checked state to screen readers (TalkBack).
**Action:** Use `Modifier.toggleable` with `role = Role.Switch` and ensure an optional `contentDescription` parameter is available for accessible labels.
