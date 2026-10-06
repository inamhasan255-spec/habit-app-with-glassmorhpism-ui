## 2025-05-20 - Custom Compose GlassIconButton Accessibility
**Learning:** In Jetpack Compose custom button wrappers (like `GlassIconButton`), placing `contentDescription` on the inner `Icon` rather than the interactive container causes screen readers to either double-announce or fail to properly describe the clickable action.
**Action:** Always accept `contentDescription: String? = null` directly on custom icon button composables, apply `semantics(mergeDescendants = true) { this.contentDescription = contentDescription }` on the container, and set inner `Icon` `contentDescription = null`.
