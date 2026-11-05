## 2025-05-18 - Confirmation Dialogs for Destructive UI Actions
**Learning:** Destructive actions placed in top app bars or card headers (like "Clear All") can easily be triggered accidentally on mobile touchscreens without immediate visual friction or confirmation, leading to permanent user data loss.
**Action:** Always wrap bulk deletion or destructive clear actions in a explicit confirmation dialog (`AlertDialog` or `GlassModal`) describing what will be deleted.
