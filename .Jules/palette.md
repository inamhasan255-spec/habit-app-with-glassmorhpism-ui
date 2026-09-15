## 2025-02-23 - Accessibility Semantics on Custom Glass Icon Buttons
**Learning:** Custom Compose wrapper components that encapsulate icon buttons (like `GlassIconButton`) can strip outer semantics unless explicit `contentDescription` or `semantics { this.contentDescription = ... }` modifiers are exposed and attached to the outer composable box/container.
**Action:** Always expose an optional `contentDescription: String? = null` parameter on custom icon button wrappers and attach it via `Modifier.semantics` so screen readers properly announce button actions.
