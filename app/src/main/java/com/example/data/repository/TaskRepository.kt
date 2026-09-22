package com.example.data.repository

import com.example.data.local.StudentProfileDao
import com.example.data.local.StudentTaskDao
import com.example.data.model.StudentProfile
import com.example.data.model.StudentTask
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: StudentTaskDao,
    private val profileDao: StudentProfileDao
) {

    val allTasks: Flow<List<StudentTask>> = taskDao.getAllTasks()
    val allProfiles: Flow<List<StudentProfile>> = profileDao.getAllProfiles()

    fun getTasksForProfile(profileId: Long): Flow<List<StudentTask>> {
        return taskDao.getTasksForProfile(profileId)
    }

    fun getCompletedTasksForProfile(profileId: Long): Flow<List<StudentTask>> {
        return taskDao.getCompletedTasksForProfile(profileId)
    }

    suspend fun getTaskById(id: Long): StudentTask? {
        return taskDao.getTaskById(id)
    }

    suspend fun insertTask(task: StudentTask): Long {
        return taskDao.insertTask(task)
    }

    suspend fun insertTasks(tasks: List<StudentTask>): List<Long> {
        return taskDao.insertTasks(tasks)
    }

    suspend fun updateTask(task: StudentTask) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: StudentTask) {
        taskDao.deleteTask(task)
    }

    suspend fun deleteTaskById(id: Long) {
        taskDao.deleteTaskById(id)
    }

    suspend fun setTaskCompleted(id: Long, isCompleted: Boolean) {
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        taskDao.updateTaskCompletionWithTime(id, isCompleted, completedAt)
    }

    suspend fun setNotificationStatus(id: Long, hasNotified: Boolean) {
        taskDao.updateNotificationStatus(id, hasNotified)
    }

    suspend fun getDueTasks(currentTime: Long, windowStartTime: Long): List<StudentTask> {
        return taskDao.getDueTasks(currentTime, windowStartTime)
    }

    suspend fun getTasksInSeries(seriesId: String): List<StudentTask> {
        return taskDao.getTasksInSeries(seriesId)
    }

    // Profile functions
    suspend fun insertProfile(profile: StudentProfile): Long {
        return profileDao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: StudentProfile) {
        profileDao.updateProfile(profile)
    }

    suspend fun deleteProfile(profile: StudentProfile) {
        profileDao.deleteProfile(profile)
    }

    suspend fun getProfileById(id: Long): StudentProfile? {
        return profileDao.getProfileById(id)
    }
}

