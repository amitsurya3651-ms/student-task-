package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_tasks")
data class StudentTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentProfileId: Long = 1L,
    val title: String,
    val summary: String,
    val subject: String = "General",
    val scheduledTimeMillis: Long,
    val durationMinutes: Int = 30,
    val isCompleted: Boolean = false,
    val completedAtMillis: Long? = null,
    val hasNotified: Boolean = false,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val recurrence: RecurrenceType = RecurrenceType.NONE,
    val recurrenceSeriesId: String? = null,
    val occurrenceIndex: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

