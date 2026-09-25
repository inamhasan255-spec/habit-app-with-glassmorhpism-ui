## 2025-05-18 - Accessibility for Custom GlassIconButton Component
**Learning:** In Compose custom icon button containers like `GlassIconButton`, wrapping `Box` elements without semantics lead to icon buttons lacking proper screen reader role announcements and accessible descriptions.
**Action:** Always provide `contentDescription: String? = null` parameter on custom icon button wrappers and add `.semantics(mergeDescendants = true) { role = Role.Button; if (contentDescription != null) this.contentDescription = contentDescription }`.
