## 2025-05-18 - GlassIconButton Screen Reader Accessibility Pattern
**Learning:** `GlassIconButton` handles button role semantics and click handling on its outer container. Supplying `contentDescription` directly to `GlassIconButton` while setting inner `Icon`'s `contentDescription` to `null` ensures proper screen reader accessibility without duplicate or missing announcements.
**Action:** Always pass `contentDescription` to `GlassIconButton` rather than inner `Icon` elements in Jetpack Compose UI screens.
