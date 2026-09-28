## 2025-05-18 - Jetpack Compose Custom Checkbox Accessibility Pattern
**Learning:** Custom interactive `Box` components acting as checkboxes need explicit `Role.Checkbox`, `stateDescription`, `onClickLabel`, and parent `contentDescription`, while child `Icon` content descriptions should be set to `null` to avoid screen reader duplicate announcements.
**Action:** When implementing custom interactive controls in Compose, apply `role = Role.Checkbox` on `.clickable()` alongside `.semantics { stateDescription = ...; contentDescription = ... }`.
