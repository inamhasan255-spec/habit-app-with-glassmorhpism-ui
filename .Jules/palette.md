# Palette's Journal

## 2025-05-20 - Custom Glassmorphism Icon Button Screen Reader Accessibility
**Learning:** Custom UI wrappers around icon buttons (like `GlassIconButton`) that use internal `Box` clickables instead of standard Compose `IconButton` content descriptions can end up invisible or uninformative to screen reader accessibility services (TalkBack) unless explicitly configured with `Modifier.semantics { contentDescription = ... }`.
**Action:** Always expose an optional `contentDescription` parameter on custom interactive/icon component wrappers and attach it via `.semantics` to the clickable node.
