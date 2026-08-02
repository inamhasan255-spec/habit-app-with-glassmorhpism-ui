package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY id DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId")
    fun getCompletionsForHabit(habitId: Int): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions")
    fun getAllCompletions(): Flow<List<HabitCompletion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletion)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCompletion(habitId: Int, date: String)
}

@Dao
interface WorkSessionDao {
    @Query("SELECT * FROM work_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<WorkSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkSession)

    @Delete
    suspend fun deleteSession(session: WorkSession)
}

@Dao
interface BlockedAppDao {
    @Query("SELECT * FROM blocked_apps ORDER BY appName ASC")
    fun getAllBlockedApps(): Flow<List<BlockedApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedApp(app: BlockedApp)

    @Update
    suspend fun updateBlockedApp(app: BlockedApp)

    @Delete
    suspend fun deleteBlockedApp(app: BlockedApp)

    @Query("SELECT * FROM blocked_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getBlockedAppByPackageName(packageName: String): BlockedApp?

    @Query("SELECT * FROM blocked_apps WHERE packageName = :packageName")
    suspend fun getBlockedAppsByPackageName(packageName: String): List<BlockedApp>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<BlockedApp>)
}

@Dao
interface EssentialItemDao {
    @Query("SELECT * FROM essential_items ORDER BY id DESC")
    fun getAllEssentialItems(): Flow<List<EssentialItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEssentialItem(item: EssentialItem)

    @Delete
    suspend fun deleteEssentialItem(item: EssentialItem)

    @Query("SELECT * FROM essential_items WHERE identifier = :identifier LIMIT 1")
    suspend fun getEssentialItemByIdentifier(identifier: String): EssentialItem?

    @Query("SELECT * FROM essential_items WHERE type = 'Website' LIMIT 1")
    suspend fun getAnyEssentialWebsite(): EssentialItem?
}

@Dao
interface JournalEntryDao {
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllJournalEntries(): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntry)

    @Delete
    suspend fun deleteJournalEntry(entry: JournalEntry)

    @Query("DELETE FROM journal_entries")
    suspend fun deleteAllJournalEntries()
}

@Dao
interface ReadingBookDao {
    @Query("SELECT * FROM reading_books ORDER BY lastReadTimestamp DESC")
    fun getAllBooks(): Flow<List<ReadingBook>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: ReadingBook)

    @Delete
    suspend fun deleteBook(book: ReadingBook)

    @Query("DELETE FROM reading_books")
    suspend fun deleteAllBooks()
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()
}

@Dao
interface AcademicSubjectDao {
    @Query("SELECT * FROM academic_subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<AcademicSubject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: AcademicSubject): Long

    @Delete
    suspend fun deleteSubject(subject: AcademicSubject)
}

@Dao
interface TimetableEntryDao {
    @Query("SELECT * FROM timetable_entries ORDER BY startTime ASC")
    fun getAllEntries(): Flow<List<TimetableEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: TimetableEntry): Long

    @Update
    suspend fun updateEntry(entry: TimetableEntry)

    @Delete
    suspend fun deleteEntry(entry: TimetableEntry)
}

@Dao
interface AcademicTaskDao {
    @Query("SELECT * FROM academic_tasks ORDER BY sortOrder ASC, dueDate ASC")
    fun getAllTasks(): Flow<List<AcademicTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: AcademicTask): Long

    @Update
    suspend fun updateTask(task: AcademicTask)

    @Delete
    suspend fun deleteTask(task: AcademicTask)
}

@Dao
interface AcademicProfileDao {
    @Query("SELECT * FROM academic_profiles WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<AcademicProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: AcademicProfile)
}

@Dao
interface VoiceNoteDao {
    @Query("SELECT * FROM voice_notes ORDER BY timestamp DESC")
    fun getAllVoiceNotes(): Flow<List<VoiceNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceNote(note: VoiceNote): Long

    @Delete
    suspend fun deleteVoiceNote(note: VoiceNote)
}

@Dao
interface AcademicChatThreadDao {
    @Query("SELECT * FROM academic_chat_threads ORDER BY lastActiveTimestamp DESC")
    fun getAllThreads(): Flow<List<AcademicChatThread>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThread(thread: AcademicChatThread)

    @Query("DELETE FROM academic_chat_threads WHERE id = :threadId")
    suspend fun deleteThread(threadId: String)

    @Query("DELETE FROM academic_chat_threads")
    suspend fun deleteAllThreads()
}

@Dao
interface AcademicChatMessageDao {
    @Query("SELECT * FROM academic_chat_messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun getMessagesForThread(threadId: String): Flow<List<AcademicChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AcademicChatMessage): Long

    @Query("UPDATE academic_chat_messages SET message = :newMessage WHERE id = :messageId")
    suspend fun updateMessage(messageId: Int, newMessage: String)

    @Query("DELETE FROM academic_chat_messages WHERE threadId = :threadId")
    suspend fun deleteMessagesForThread(threadId: String)

    @Query("DELETE FROM academic_chat_messages")
    suspend fun deleteAllMessages()
}

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllFocusSessions(): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions WHERE startTime >= :startOfDay AND startTime <= :endOfDay ORDER BY startTime DESC")
    fun getFocusSessionsByDate(startOfDay: Long, endOfDay: Long): Flow<List<FocusSession>>

    @Query("SELECT SUM(interruptionCount) FROM focus_sessions WHERE startTime >= :startOfDay AND startTime <= :endOfDay")
    fun getDailyInterruptionCount(startOfDay: Long, endOfDay: Long): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSession): Long

    @Update
    suspend fun updateFocusSession(session: FocusSession)

    @Delete
    suspend fun deleteFocusSession(session: FocusSession)
}




@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStats(): Flow<UserStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStats)

    @Update
    suspend fun updateUserStats(stats: UserStats)
}

@Dao
interface BadgeDao {
    @Query("SELECT * FROM badges")
    fun getAllBadges(): Flow<List<Badge>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBadges(badges: List<Badge>)

    @Update
    suspend fun updateBadge(badge: Badge)
}

@Dao
interface CommunityGroupDao {
    @Query("SELECT * FROM community_groups")
    fun getAllGroups(): Flow<List<CommunityGroup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: CommunityGroup): Long

    @Delete
    suspend fun deleteGroup(group: CommunityGroup)
}

@Dao
interface CommunityMemberDao {
    @Query("SELECT * FROM community_members WHERE groupId = :groupId ORDER BY progressHours DESC")
    fun getMembersForGroup(groupId: Int): Flow<List<CommunityMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: CommunityMember)

    @Update
    suspend fun updateMember(member: CommunityMember)

    @Delete
    suspend fun deleteMember(member: CommunityMember)
}
