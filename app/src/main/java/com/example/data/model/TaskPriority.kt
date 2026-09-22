package com.example.data.model

enum class TaskPriority(val label: String, val level: Int) {
    HIGH("High", 1),
    MEDIUM("Medium", 2),
    LOW("Low", 3);

    companion object {
        fun fromString(value: String?): TaskPriority {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
        }
    }
}
