# Palette's Journal - Critical UX & Accessibility Learnings

## 2026-10-13 - Toggle/Switch Accessibility in Custom Compose Components
**Learning:** Custom toggle switch components constructed with `Box` and `Modifier.clickable` fail to communicate their state (`checked`/`unchecked`) or interactive component role to TalkBack/screen readers, leading to confusing screen reader announcements where switches sound like generic unlabelled buttons. Using `Modifier.toggleable` with `role = Role.Switch` automatically provides the accessibility node with the proper toggle semantics and state announcements.
**Action:** When building or polishing custom toggle switches, always use `Modifier.toggleable` with `Role.Switch` and ensure optional `contentDescription` semantics can be passed down.
