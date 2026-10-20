package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val grade: String,
    val avatarId: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1,
    val topic: String,
    val subject: String,
    val userPrompt: String,
    val explanationText: String,
    val diagramType: String = "none",
    val diagramDataJson: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1,
    val topic: String,
    val subject: String,
    val difficulty: String,
    val questionsJson: String,
    val userAnswersJson: String,
    val score: Int,
    val totalQuestions: Int,
    val timestamp: Long = System.currentTimeMillis()
)
