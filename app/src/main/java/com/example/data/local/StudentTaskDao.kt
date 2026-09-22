package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.StudentTask
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentTaskDao {
    @Query("SELECT * FROM student_tasks ORDER BY isCompleted ASC, CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 ELSE 2 END ASC, scheduledTimeMillis ASC")
    fun getAllTasks(): Flow<List<StudentTask>>

    @Query("SELECT * FROM student_tasks WHERE studentProfileId = :profileId ORDER BY isCompleted ASC, CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 ELSE 2 END ASC, scheduledTimeMillis ASC")
    fun getTasksForProfile(profileId: Long): Flow<List<StudentTask>>

    @Query("SELECT * FROM student_tasks WHERE studentProfileId = :profileId AND isCompleted = 1 ORDER BY COALESCE(completedAtMillis, scheduledTimeMillis) DESC")
    fun getCompletedTasksForProfile(profileId: Long): Flow<List<StudentTask>>

    @Query("SELECT * FROM student_tasks WHERE recurrenceSeriesId = :seriesId ORDER BY occurrenceIndex ASC, scheduledTimeMillis ASC")
    suspend fun getTasksInSeries(seriesId: String): List<StudentTask>

    @Query("SELECT * FROM student_tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): StudentTask?

    @Query("SELECT * FROM student_tasks WHERE isCompleted = 0 AND scheduledTimeMillis <= :currentTime AND scheduledTimeMillis >= :windowStartTime")
    suspend fun getDueTasks(currentTime: Long, windowStartTime: Long): List<StudentTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: StudentTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<StudentTask>): List<Long>

    @Update
    suspend fun updateTask(task: StudentTask)

    @Delete
    suspend fun deleteTask(task: StudentTask)

    @Query("DELETE FROM student_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("UPDATE student_tasks SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateTaskCompletion(id: Long, isCompleted: Boolean)

    @Query("UPDATE student_tasks SET isCompleted = :isCompleted, completedAtMillis = :completedAt WHERE id = :id")
    suspend fun updateTaskCompletionWithTime(id: Long, isCompleted: Boolean, completedAt: Long?)

    @Query("UPDATE student_tasks SET hasNotified = :hasNotified WHERE id = :id")
    suspend fun updateNotificationStatus(id: Long, hasNotified: Boolean)
}
