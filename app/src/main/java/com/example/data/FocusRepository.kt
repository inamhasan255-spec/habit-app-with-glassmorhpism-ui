package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FocusRepository(private val database: FocusDatabase) {

    private val habitDao = database.habitDao()
    private val workSessionDao = database.workSessionDao()
    private val blockedAppDao = database.blockedAppDao()
    private val essentialItemDao = database.essentialItemDao()
    private val journalEntryDao = database.journalEntryDao()
    private val readingBookDao = database.readingBookDao()
    private val chatMessageDao = database.chatMessageDao()
    private val academicSubjectDao = database.academicSubjectDao()
    private val timetableEntryDao = database.timetableEntryDao()
    private val academicTaskDao = database.academicTaskDao()
    private val academicProfileDao = database.academicProfileDao()
    private val voiceNoteDao = database.voiceNoteDao()
    private val academicChatThreadDao = database.academicChatThreadDao()
    private val academicChatMessageDao = database.academicChatMessageDao()
    private val focusSessionDao = database.focusSessionDao()
    private val userStatsDao = database.userStatsDao()
    val badgeDao = database.badgeDao()
    val communityGroupDao = database.communityGroupDao()
    val communityMemberDao = database.communityMemberDao()

    // Habits
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val allCompletions: Flow<List<HabitCompletion>> = habitDao.getAllCompletions()

    // Journal, Reading, Chat Streams
    val allJournalEntries: Flow<List<JournalEntry>> = journalEntryDao.getAllJournalEntries()
    val allBooks: Flow<List<ReadingBook>> = readingBookDao.getAllBooks()
    val allMessages: Flow<List<ChatMessage>> = chatMessageDao.getAllMessages()

    // Academic Streams
    val allSubjects: Flow<List<AcademicSubject>> = academicSubjectDao.getAllSubjects()
    val allEntries: Flow<List<TimetableEntry>> = timetableEntryDao.getAllEntries()
    val allTasks: Flow<List<AcademicTask>> = academicTaskDao.getAllTasks()
    val academicProfile: Flow<AcademicProfile?> = academicProfileDao.getProfile()
    val allVoiceNotes: Flow<List<VoiceNote>> = voiceNoteDao.getAllVoiceNotes()
    val allAcademicChatThreads: Flow<List<AcademicChatThread>> = academicChatThreadDao.getAllThreads()

    // Essential Items
    val allEssentialItems: Flow<List<EssentialItem>> = essentialItemDao.getAllEssentialItems()

    suspend fun insertEssentialItem(item: EssentialItem) = essentialItemDao.insertEssentialItem(item)
    suspend fun deleteEssentialItem(item: EssentialItem) = essentialItemDao.deleteEssentialItem(item)
    suspend fun getEssentialItemByIdentifier(identifier: String): EssentialItem? = essentialItemDao.getEssentialItemByIdentifier(identifier)
    suspend fun getAnyEssentialWebsite(): EssentialItem? = essentialItemDao.getAnyEssentialWebsite()

    suspend fun insertHabit(habit: Habit): Long = habitDao.insertHabit(habit)
    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)
    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    fun getCompletionsForHabit(habitId: Int): Flow<List<HabitCompletion>> =
        habitDao.getCompletionsForHabit(habitId)

    suspend fun completeHabit(habit: Habit, date: String) {
        // Check if completion already exists
        val completions = habitDao.getCompletionsForHabit(habit.id).first()
        val alreadyCompleted = completions.any { it.date == date }
        if (!alreadyCompleted) {
            habitDao.insertCompletion(HabitCompletion(habitId = habit.id, date = date, value = habit.targetValue))
            
            // Update streak
            val newStreak = if (habit.lastCompletedDate == "" || habit.lastCompletedDate == date) {
                habit.streakCount + 1
            } else {
                // For simplicity, let's just increase the streak count!
                habit.streakCount + 1
            }
            habitDao.updateHabit(
                habit.copy(
                    streakCount = newStreak,
                    lastCompletedDate = date
                )
            )
        }
    }

    suspend fun updateHabitProgress(habit: Habit, date: String, newValue: Int) {
        val completions = habitDao.getCompletionsForHabit(habit.id).first()
        val existing = completions.find { it.date == date }
        val target = habit.targetValue
        if (newValue <= 0) {
            if (existing != null) {
                habitDao.deleteCompletion(habit.id, date)
                val newStreak = (habit.streakCount - 1).coerceAtLeast(0)
                habitDao.updateHabit(
                    habit.copy(
                        streakCount = newStreak,
                        lastCompletedDate = if (newStreak == 0) "" else habit.lastCompletedDate
                    )
                )
            }
        } else {
            if (existing != null) {
                habitDao.insertCompletion(existing.copy(value = newValue.coerceIn(0, 9999)))
                if (newValue >= target && existing.value < target) {
                    habitDao.updateHabit(habit.copy(lastCompletedDate = date))
                }
            } else {
                habitDao.insertCompletion(HabitCompletion(habitId = habit.id, date = date, value = newValue.coerceIn(0, 9999)))
                val newStreak = if (habit.lastCompletedDate == "" || habit.lastCompletedDate == date) {
                    habit.streakCount + 1
                } else {
                    habit.streakCount + 1
                }
                habitDao.updateHabit(
                    habit.copy(
                        streakCount = newStreak,
                        lastCompletedDate = date
                    )
                )
            }
        }
    }

    suspend fun uncompleteHabit(habit: Habit, date: String) {
        habitDao.deleteCompletion(habit.id, date)
        val newStreak = (habit.streakCount - 1).coerceAtLeast(0)
        habitDao.updateHabit(
            habit.copy(
                streakCount = newStreak,
                lastCompletedDate = if (newStreak == 0) "" else habit.lastCompletedDate
            )
        )
    }

    // Work Sessions
    val allSessions: Flow<List<WorkSession>> = workSessionDao.getAllSessions()
    suspend fun insertSession(session: WorkSession) = workSessionDao.insertSession(session)
    suspend fun deleteSession(session: WorkSession) = workSessionDao.deleteSession(session)

    // Blocked Apps
    val allBlockedApps: Flow<List<BlockedApp>> = blockedAppDao.getAllBlockedApps()
    suspend fun insertBlockedApp(app: BlockedApp) = blockedAppDao.insertBlockedApp(app)
    suspend fun updateBlockedApp(app: BlockedApp) = blockedAppDao.updateBlockedApp(app)
    suspend fun deleteBlockedApp(app: BlockedApp) = blockedAppDao.deleteBlockedApp(app)
    suspend fun getBlockedAppsByPackageName(packageName: String): List<BlockedApp> = blockedAppDao.getBlockedAppsByPackageName(packageName)

    suspend fun prepopulateDefaultAppsIfEmpty() {
        val currentApps = blockedAppDao.getAllBlockedApps().first()
        if (currentApps.isEmpty()) {
            val defaults = listOf(
                BlockedApp(
                    packageName = "com.chatterbox.social",
                    appName = "ChatterBox",
                    isBlocked = true,
                    category = "SOCIAL MEDIA",
                    blockMode = "Session only"
                ),
                BlockedApp(
                    packageName = "com.streammax.video",
                    appName = "StreamMax",
                    isBlocked = true,
                    category = "ENTERTAINMENT",
                    blockMode = "Scheduled"
                ),
                BlockedApp(
                    packageName = "com.infinitymail.comm",
                    appName = "InfinityMail",
                    isBlocked = false,
                    category = "COMMUNICATION",
                    blockMode = "Reminder always"
                ),
                BlockedApp(
                    packageName = "com.tiktok.app",
                    appName = "TikTok",
                    isBlocked = false,
                    category = "SOCIAL MEDIA",
                    blockMode = "Session only"
                ),
                BlockedApp(
                    packageName = "com.reddit.app",
                    appName = "Reddit",
                    isBlocked = false,
                    category = "ENTERTAINMENT",
                    blockMode = "Session only"
                )
            )
            blockedAppDao.insertAll(defaults)
        }
    }

    // Prepopulate habits if empty
    suspend fun prepopulateDefaultHabitsIfEmpty() {
        val currentHabits = habitDao.getAllHabits().first()
        if (currentHabits.isEmpty()) {
            val defaults = listOf(
                Habit(
                    name = "Deep Work Block",
                    category = "Work",
                    colorHex = "#5adcb3", // Teal Accent
                    isObsidianLinked = true,
                    obsidianUri = "obsidian://open?vault=FocusVault&file=Deep%20Work%20Logs",
                    streakCount = 12,
                    lastCompletedDate = "2026-06-26"
                ),
                Habit(
                    name = "Read Design Articles",
                    category = "Learn",
                    colorHex = "#fcb2c5", // Pink Accent
                    isObsidianLinked = false,
                    streakCount = 4,
                    lastCompletedDate = "2026-06-26"
                ),
                Habit(
                    name = "Morning Stretch & Cardio",
                    category = "Health",
                    colorHex = "#f7e299", // Yellow Accent
                    isObsidianLinked = true,
                    obsidianUri = "obsidian://open?vault=FocusVault&file=Health%20Journal",
                    streakCount = 7,
                    lastCompletedDate = "2026-06-25"
                )
            )
            for (habit in defaults) {
                habitDao.insertHabit(habit)
            }
        }
    }

    // New Data Operations
    suspend fun insertJournalEntry(entry: JournalEntry) = journalEntryDao.insertJournalEntry(entry)
    suspend fun deleteJournalEntry(entry: JournalEntry) = journalEntryDao.deleteJournalEntry(entry)
    suspend fun clearAllJournalEntries() = journalEntryDao.deleteAllJournalEntries()

    suspend fun insertBook(book: ReadingBook) = readingBookDao.insertBook(book)
    suspend fun deleteBook(book: ReadingBook) = readingBookDao.deleteBook(book)
    suspend fun clearAllBooks() = readingBookDao.deleteAllBooks()

    suspend fun insertMessage(message: ChatMessage) = chatMessageDao.insertMessage(message)
    suspend fun clearAllMessages() = chatMessageDao.deleteAllMessages()

    // Academic Operations
    suspend fun insertSubject(subject: AcademicSubject): Long = academicSubjectDao.insertSubject(subject)
    suspend fun deleteSubject(subject: AcademicSubject) = academicSubjectDao.deleteSubject(subject)

    suspend fun insertEntry(entry: TimetableEntry): Long = timetableEntryDao.insertEntry(entry)
    suspend fun updateEntry(entry: TimetableEntry) = timetableEntryDao.updateEntry(entry)
    suspend fun deleteEntry(entry: TimetableEntry) = timetableEntryDao.deleteEntry(entry)

    suspend fun insertTask(task: AcademicTask): Long = academicTaskDao.insertTask(task)
    suspend fun updateTask(task: AcademicTask) = academicTaskDao.updateTask(task)
    suspend fun deleteTask(task: AcademicTask) = academicTaskDao.deleteTask(task)

    suspend fun insertProfile(profile: AcademicProfile) = academicProfileDao.insertProfile(profile)

    suspend fun insertVoiceNote(note: VoiceNote): Long = voiceNoteDao.insertVoiceNote(note)
    suspend fun deleteVoiceNote(note: VoiceNote) = voiceNoteDao.deleteVoiceNote(note)

    // Academic Chat Operations
    fun getMessagesForThread(threadId: String): Flow<List<AcademicChatMessage>> =
        academicChatMessageDao.getMessagesForThread(threadId)

    suspend fun insertAcademicChatThread(thread: AcademicChatThread) =
        academicChatThreadDao.insertThread(thread)

    suspend fun deleteAcademicChatThread(threadId: String) {
        academicChatMessageDao.deleteMessagesForThread(threadId)
        academicChatThreadDao.deleteThread(threadId)
    }

    suspend fun insertAcademicChatMessage(message: AcademicChatMessage): Long =
        academicChatMessageDao.insertMessage(message)

    suspend fun updateAcademicChatMessage(messageId: Int, message: String) =
        academicChatMessageDao.updateMessage(messageId, message)

    suspend fun clearAllAcademicChats() {
        academicChatMessageDao.deleteAllMessages()
        academicChatThreadDao.deleteAllThreads()
    }

    // Focus Sessions
    val allFocusSessions: Flow<List<FocusSession>> = focusSessionDao.getAllFocusSessions()

    val userStats: Flow<UserStats?> = userStatsDao.getUserStats()
    val allBadges: Flow<List<Badge>> = badgeDao.getAllBadges()

    suspend fun insertUserStats(stats: UserStats) {
        userStatsDao.insertUserStats(stats)
    }

    suspend fun updateUserStats(stats: UserStats) {
        userStatsDao.updateUserStats(stats)
    }

    suspend fun insertBadges(badges: List<Badge>) {
        badgeDao.insertBadges(badges)
    }

    suspend fun updateBadge(badge: Badge) {
        badgeDao.updateBadge(badge)
    }

    fun getFocusSessionsByDate(startOfDay: Long, endOfDay: Long): Flow<List<FocusSession>> =
        focusSessionDao.getFocusSessionsByDate(startOfDay, endOfDay)
    fun getDailyInterruptionCount(startOfDay: Long, endOfDay: Long): Flow<Int?> =
        focusSessionDao.getDailyInterruptionCount(startOfDay, endOfDay)
    suspend fun insertFocusSession(session: FocusSession): Long = focusSessionDao.insertFocusSession(session)
    suspend fun updateFocusSession(session: FocusSession) = focusSessionDao.updateFocusSession(session)
    suspend fun deleteFocusSession(session: FocusSession) = focusSessionDao.deleteFocusSession(session)


    // Community 
    val allCommunityGroups: Flow<List<CommunityGroup>> = communityGroupDao.getAllGroups()
    
    suspend fun insertCommunityGroup(group: CommunityGroup): Long = communityGroupDao.insertGroup(group)
    suspend fun deleteCommunityGroup(group: CommunityGroup) = communityGroupDao.deleteGroup(group)

    fun getMembersForGroup(groupId: Int): Flow<List<CommunityMember>> = communityMemberDao.getMembersForGroup(groupId)
    
    suspend fun insertCommunityMember(member: CommunityMember) = communityMemberDao.insertMember(member)
    suspend fun updateCommunityMember(member: CommunityMember) = communityMemberDao.updateMember(member)
    suspend fun deleteCommunityMember(member: CommunityMember) = communityMemberDao.deleteMember(member)
}
