with open('app/src/main/java/com/focuscube/data/repository/CalendarSyncManager.kt', 'r') as f:
    content = f.read()

content = content.replace('val cursor: Cursor? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) context.createAttributionContext("calendar_sync").contentResolver else context.contentResolver.query(', 'val resolver = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) context.createAttributionContext("calendar_sync").contentResolver else context.contentResolver\n            val cursor: Cursor? = resolver.query(')

with open('app/src/main/java/com/focuscube/data/repository/CalendarSyncManager.kt', 'w') as f:
    f.write(content)
