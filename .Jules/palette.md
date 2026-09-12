## 2025-05-18 - Accessibility Semantics on Custom Icon Button Wrappers
**Learning:** Custom UI button wrappers like `GlassIconButton` wrapping lower-level touchable elements often hide inner icon content descriptions from screen readers if accessibility semantics aren't explicitly passed down or set on the root container modifier.
**Action:** Always accept an optional `contentDescription` parameter on custom icon button components and expose it via `Modifier.semantics { contentDescription = description }`.
