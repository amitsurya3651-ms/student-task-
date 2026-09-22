package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_profiles")
data class StudentProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val gradeOrMajor: String = "General Studies",
    val emailOrHandle: String = "",
    val weeklyGoalHours: Int = 15,
    val targetDailyMinutes: Int = 120,
    val avatarEmoji: String = "🎓",
    val avatarColorHex: Long = 0xFF4F46E5,
    val createdAt: Long = System.currentTimeMillis()
)

