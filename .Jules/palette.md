## 2025-05-18 - Dynamic Item Context in Compose Content Descriptions
**Learning:** Generic accessibility descriptions on icon buttons within lists (e.g., "Delete", "Mark Done", "Toggle Complete") lack necessary context for screen reader users. Adding dynamic item titles and state-aware descriptions (e.g., `Delete task '${todo.title}'`, `Mark '${habit.chapter}' as incomplete`) drastically improves navigation clarity for assistive technologies.
**Action:** Always prefer contextual, state-aware `contentDescription` strings over static text for list item actions in Jetpack Compose components.
