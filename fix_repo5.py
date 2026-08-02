with open("app/src/main/java/com/example/data/FocusRepository.kt", "r") as f:
    text = f.read()

import re
text = re.sub(r'    val communityGroupDao = database.communityGroupDao\(\)\n    val communityMemberDao = database.communityMemberDao\(\)\n', '', text, count=1)

with open("app/src/main/java/com/example/data/FocusRepository.kt", "w") as f:
    f.write(text)
