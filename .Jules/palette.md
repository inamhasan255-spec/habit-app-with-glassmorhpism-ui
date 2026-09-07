## 2025-05-20 - Dynamic Accessibility Content Descriptions in Jetpack Compose
**Learning:** Icon-only toggle buttons require dynamic ARIA / content descriptions that state the targeted action (e.g., "Mark as incomplete" vs "Mark as complete") rather than static state labels ("Mark Done"). Additionally, decorative icons placed inside text-labeled buttons should explicitly set `contentDescription = null` to avoid redundant screen reader announcements.
**Action:** Always audit `contentDescription` on interactive Compose `IconButton`s and check for redundant descriptions on embedded button icons.
