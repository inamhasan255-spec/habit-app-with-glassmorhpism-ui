## 2025-05-10 - GlassIconButton Accessibility Pattern
**Learning:** `GlassIconButton` applies `Modifier.semantics(mergeDescendants = true)` when `contentDescription` is provided directly to the wrapper. Setting `contentDescription` on inner `Icon` causes screen readers like TalkBack to read duplicate or conflicting labels.
**Action:** Always supply `contentDescription` directly to `GlassIconButton` and pass `contentDescription = null` to child `Icon` Composables.
