## 2025-05-18 - Accessibility Roles for Custom Glass Controls
**Learning:** Custom interactive Compose components like `GlassToggle` and `ModeToggle` used custom `.clickable` modifiers with null indications, omitting Compose semantics. Screen readers (TalkBack) were unable to announce role (`Switch` / `Tab`) or state (`Checked` / `Selected`).
**Action:** Always prefer `Modifier.toggleable` with `Role.Switch` for toggle components and `Modifier.selectable` with `Role.Tab` for navigation bars to ensure accessibility services announce state and role correctly.
