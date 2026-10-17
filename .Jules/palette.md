## 2025-05-18 - GlassIconButton Accessibility & Double Click Actions

**Learning:** Nesting Material 3 `IconButton` inside a custom `Box` that also attaches `.clickable` creates redundant accessibility nodes and duplicate click triggers for screen readers like TalkBack. Furthermore, passing `contentDescription` to inner `Icon` elements instead of `GlassIconButton` causes double announcements when `mergeDescendants = true` is used.
**Action:** Handle `clickable` and `Role.Button` directly on `GlassIconButton`'s root `Box` container with `mergeDescendants = true`. Pass `contentDescription` directly to `GlassIconButton` and set inner `Icon`'s `contentDescription` to `null`.
