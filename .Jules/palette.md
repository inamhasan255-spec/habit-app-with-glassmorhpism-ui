## 2025-03-08 - Custom Compose Icon Button Accessibility Pattern
**Learning:** Custom Compose icon buttons (`GlassIconButton`, `DepthIconButton`) require explicit `contentDescription` parameter wiring to `Modifier.semantics` or `HoverTooltip` so screen readers and tooltips can accurately announce icon actions.
**Action:** When creating or modifying custom Compose icon button wrappers, always expose an optional `contentDescription: String? = null` parameter and bind it to component semantics.
