## 2025-05-18 - Context-Aware Screen Reader Descriptions for Item Lists
**Learning:** Generic accessibility labels like "Toggle Complete" or "Delete Task" in repeated list items create confusing screen reader navigation (TalkBack speaks identical descriptions for all items).
**Action:** Always include item context (e.g. `todo.title`) in `contentDescription` for icon buttons in list rows.
