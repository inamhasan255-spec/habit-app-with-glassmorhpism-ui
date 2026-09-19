## 2025-05-10 - GlassIconButton Screen Reader Accessibility Pattern
**Learning:** Icon-only buttons wrapping Compose `Icon`s should set `contentDescription` on the outer button container (`GlassIconButton`) via `semantics` and pass `contentDescription = null` to the inner `Icon` to prevent redundant/conflicting screen reader announcements.
**Action:** Always provide an explicit `contentDescription` parameter on custom icon-button wrapper components to support dynamic accessible labels like "Show password" / "Hide password".
