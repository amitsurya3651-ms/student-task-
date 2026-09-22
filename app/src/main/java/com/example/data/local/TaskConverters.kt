package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.RecurrenceType
import com.example.data.model.TaskPriority

class TaskConverters {
    @TypeConverter
    fun fromPriority(priority: TaskPriority?): String = priority?.name ?: TaskPriority.MEDIUM.name

    @TypeConverter
    fun toPriority(value: String?): TaskPriority = TaskPriority.fromString(value)

    @TypeConverter
    fun fromRecurrence(recurrence: RecurrenceType?): String = recurrence?.name ?: RecurrenceType.NONE.name

    @TypeConverter
    fun toRecurrence(value: String?): RecurrenceType = RecurrenceType.fromString(value)
}
