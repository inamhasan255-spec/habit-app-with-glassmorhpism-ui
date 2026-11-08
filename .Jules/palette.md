## 2024-05-20 - Custom Compose Toggles Accessibility
**Learning:** Custom UI toggles built with `Box` and `Modifier.clickable` lack screen reader role (`Role.Switch`) and state (checked/unchecked) announcements.
**Action:** Use `Modifier.toggleable` with `Role.Switch` and `onValueChange` for custom switch components, and ensure parent containers carry `contentDescription` while inner icons set `contentDescription = null`.
