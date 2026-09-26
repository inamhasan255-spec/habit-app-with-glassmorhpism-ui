## 2025-05-18 - Compose GlassIconButton Accessibility Semantics
**Learning:** `GlassIconButton` was missing support for accessibility descriptions, causing screen readers (TalkBack) to read generic or missing labels on icon buttons across the app.
**Action:** Always accept a `contentDescription: String? = null` on custom icon button wrappers and apply `Modifier.semantics(mergeDescendants = true)` so that action icons speak actionable item details (e.g. "Delete task Homework").
