## 2026-08-02 - GlassIconButton Accessibility Semantics
**Learning:** Custom UI container components wrapper around `IconButton` (like `GlassIconButton`) can obscure or omit screen reader `contentDescription` if not explicitly passed down and attached via Compose `Modifier.semantics`.
**Action:** When building custom icon button wrappers, accept an optional `contentDescription: String?` parameter and attach it using `Modifier.semantics { this.contentDescription = ... }`.
