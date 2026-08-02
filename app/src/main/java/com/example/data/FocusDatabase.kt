package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Habit::class, 
        HabitCompletion::class, 
        WorkSession::class, 
        BlockedApp::class, 
        EssentialItem::class, 
        JournalEntry::class, 
        ReadingBook::class, 
        ChatMessage::class,
        AcademicSubject::class,
        TimetableEntry::class,
        AcademicTask::class,
        AcademicProfile::class,
        VoiceNote::class,
        AcademicChatThread::class,
        AcademicChatMessage::class,
        FocusSession::class,
        CommunityGroup::class,
        CommunityMember::class,
        UserStats::class,
        Badge::class
    ],
    version = 11,
    exportSchema = false
)
abstract class FocusDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun workSessionDao(): WorkSessionDao
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun essentialItemDao(): EssentialItemDao
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun readingBookDao(): ReadingBookDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun academicSubjectDao(): AcademicSubjectDao
    abstract fun timetableEntryDao(): TimetableEntryDao
    abstract fun academicTaskDao(): AcademicTaskDao
    abstract fun academicProfileDao(): AcademicProfileDao
    abstract fun voiceNoteDao(): VoiceNoteDao
    abstract fun academicChatThreadDao(): AcademicChatThreadDao
    abstract fun academicChatMessageDao(): AcademicChatMessageDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun badgeDao(): BadgeDao
    abstract fun communityGroupDao(): CommunityGroupDao
    abstract fun communityMemberDao(): CommunityMemberDao

    companion object {
        @Volatile
        private var INSTANCE: FocusDatabase? = null

        fun getDatabase(context: Context): FocusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FocusDatabase::class.java,
                    "focus_cube_database"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
