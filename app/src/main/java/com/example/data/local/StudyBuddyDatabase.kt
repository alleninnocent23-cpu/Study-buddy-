package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, StudySessionEntity::class, QuizEntity::class],
    version = 1,
    exportSchema = false
)
abstract class StudyBuddyDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun quizDao(): QuizDao

    companion object {
        @Volatile
        private var INSTANCE: StudyBuddyDatabase? = null

        fun getDatabase(context: Context): StudyBuddyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyBuddyDatabase::class.java,
                    "study_buddy_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
