import re

with open("app/src/main/java/com/example/data/FocusViewModel.kt", "r") as f:
    content = f.read()

# Add points in finishTimer
pattern1 = r"""repository\.insertFocusSession\(\s*FocusSession\(\s*label = timerLabel,\s*startTime = sessionStartTime,\s*endTime = System\.currentTimeMillis\(\),\s*mode = focusSessionMode,\s*plannedMinutes = timerTotalMinutes,\s*actualMinutes = elapsedMinutes,\s*interruptionCount = sessionInterruptionCount,\s*completed = true\s*\)\s*\)"""

replacement1 = """repository.insertFocusSession(
                FocusSession(
                    label = timerLabel,
                    startTime = sessionStartTime,
                    endTime = System.currentTimeMillis(),
                    mode = focusSessionMode,
                    plannedMinutes = timerTotalMinutes,
                    actualMinutes = elapsedMinutes,
                    interruptionCount = sessionInterruptionCount,
                    completed = true
                )
            )
            addPoints(elapsedMinutes * 10)"""

content = re.sub(pattern1, replacement1, content)

# Add points in toggleHabitCompletion
pattern2 = r"""repository\.completeHabit\(habit, date\)\s*triggerHapticPattern\("SUCCESS"\)"""
replacement2 = """repository.completeHabit(habit, date)
                triggerHapticPattern("SUCCESS")
                addPoints(50)
                // Check if streak increased
                val newStreak = habit.streakCount + 1
                checkStreakBadges(newStreak)"""

content = re.sub(pattern2, replacement2, content)

with open("app/src/main/java/com/example/data/FocusViewModel.kt", "w") as f:
    f.write(content)
