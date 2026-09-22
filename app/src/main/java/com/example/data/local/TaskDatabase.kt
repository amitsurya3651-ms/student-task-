package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.RecurrenceType
import com.example.data.model.StudentProfile
import com.example.data.model.StudentTask
import com.example.data.model.TaskPriority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(entities = [StudentTask::class, StudentProfile::class], version = 3, exportSchema = false)
@TypeConverters(TaskConverters::class)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): StudentTaskDao
    abstract fun profileDao(): StudentProfileDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getDatabase(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "student_tasks_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.profileDao(), database.taskDao())
                    }
                }
            }

            private suspend fun populateInitialData(profileDao: StudentProfileDao, taskDao: StudentTaskDao) {
                // Default Profile 1: Alex Rivera
                val profile1Id = profileDao.insertProfile(
                    StudentProfile(
                        name = "Alex Rivera",
                        gradeOrMajor = "Computer Science & Mathematics",
                        avatarColorHex = 0xFF4F46E5,
                        weeklyGoalHours = 15
                    )
                )

                // Default Profile 2: Sophia Chen
                val profile2Id = profileDao.insertProfile(
                    StudentProfile(
                        name = "Sophia Chen",
                        gradeOrMajor = "Biomedical Engineering",
                        avatarColorHex = 0xFF059669,
                        weeklyGoalHours = 20
                    )
                )

                val now = System.currentTimeMillis()
                val mathSeriesId = UUID.randomUUID().toString()
                val physicsSeriesId = UUID.randomUUID().toString()

                // Sample task 1: In 10 minutes - High Priority, Daily recurrence (Occurrence 1)
                taskDao.insertTask(
                    StudentTask(
                        studentProfileId = profile1Id,
                        title = "Calculus & Linear Algebra Revision",
                        summary = "Review derivative rules, matrix transformations, and complete practice problem set #4 (problems 1-12). Focus on integration by parts.",
                        subject = "Mathematics",
                        scheduledTimeMillis = now + 10 * 60 * 1000,
                        durationMinutes = 45,
                        isCompleted = false,
                        priority = TaskPriority.HIGH,
                        recurrence = RecurrenceType.DAILY,
                        recurrenceSeriesId = mathSeriesId,
                        occurrenceIndex = 1
                    )
                )

                // Sample task 1 future auto-generated instance: Tomorrow (Occurrence 2)
                taskDao.insertTask(
                    StudentTask(
                        studentProfileId = profile1Id,
                        title = "Calculus & Linear Algebra Revision",
                        summary = "Review derivative rules, matrix transformations, and complete practice problem set #4 (problems 1-12). Focus on integration by parts.",
                        subject = "Mathematics",
                        scheduledTimeMillis = now + 10 * 60 * 1000 + 24 * 60 * 60 * 1000L,
                        durationMinutes = 45,
                        isCompleted = false,
                        priority = TaskPriority.HIGH,
                        recurrence = RecurrenceType.DAILY,
                        recurrenceSeriesId = mathSeriesId,
                        occurrenceIndex = 2
                    )
                )

                // Sample task 2: In 45 minutes - Medium Priority, Weekly recurrence
                taskDao.insertTask(
                    StudentTask(
                        studentProfileId = profile1Id,
                        title = "Physics Lab Report Summary",
                        summary = "Write conclusion section for Pendulum Oscillation experiment. Calculate standard deviation and error bars from lab readings.",
                        subject = "Physics",
                        scheduledTimeMillis = now + 45 * 60 * 1000,
                        durationMinutes = 30,
                        isCompleted = false,
                        priority = TaskPriority.MEDIUM,
                        recurrence = RecurrenceType.WEEKLY,
                        recurrenceSeriesId = physicsSeriesId,
                        occurrenceIndex = 1
                    )
                )

                // Sample task 3: Today later - Low Priority, No repeat
                taskDao.insertTask(
                    StudentTask(
                        studentProfileId = profile1Id,
                        title = "World History Essay Draft",
                        summary = "Synthesize key arguments on the Industrial Revolution. Write intro and body paragraphs 1 & 2 with bibliography references.",
                        subject = "History",
                        scheduledTimeMillis = now + 120 * 60 * 1000,
                        durationMinutes = 60,
                        isCompleted = false,
                        priority = TaskPriority.LOW,
                        recurrence = RecurrenceType.NONE
                    )
                )

                // Sample Completed Tasks for Profile 1 to immediately showcase history and progress statistics
                taskDao.insertTask(
                    StudentTask(
                        studentProfileId = profile1Id,
                        title = "Algorithms & Data Structures: Graphs Quiz Prep",
                        summary = "Mastered Dijkstra's algorithm, BFS/DFS traversal, and topological sort questions. All practice problems solved.",
                        subject = "Coding",
                        scheduledTimeMillis = now - 28 * 60 * 60 * 1000L,
                        completedAtMillis = now - 27 * 60 * 60 * 1000L,
                        durationMinutes = 60,
                        isCompleted = true,
                        priority = TaskPriority.HIGH,
                        recurrence = RecurrenceType.NONE
                    )
                )

                taskDao.insertTask(
                    StudentTask(
                        studentProfileId = profile1Id,
                        title = "Chemistry Periodic Table Flashcards",
                        summary = "Reviewed transition metals, electronegativity trends, and oxidation states flashcard deck twice.",
                        subject = "Chemistry",
                        scheduledTimeMillis = now - 52 * 60 * 60 * 1000L,
                        completedAtMillis = now - 51 * 60 * 60 * 1000L,
                        durationMinutes = 30,
                        isCompleted = true,
                        priority = TaskPriority.MEDIUM,
                        recurrence = RecurrenceType.NONE
                    )
                )

                taskDao.insertTask(
                    StudentTask(
                        studentProfileId = profile1Id,
                        title = "Linear Algebra Matrix Diagonalization",
                        summary = "Solved characteristic polynomial equations and found eigenvalues & eigenvectors for 3x3 matrices.",
                        subject = "Mathematics",
                        scheduledTimeMillis = now - 72 * 60 * 60 * 1000L,
                        completedAtMillis = now - 71 * 60 * 60 * 1000L,
                        durationMinutes = 45,
                        isCompleted = true,
                        priority = TaskPriority.HIGH,
                        recurrence = RecurrenceType.NONE
                    )
                )

                // Sample task for Sophia (Profile 2)
                taskDao.insertTask(
                    StudentTask(
                        studentProfileId = profile2Id,
                        title = "Biomechanics & Tissue Engineering Seminar",
                        summary = "Prepare slide presentation on artificial vascular scaffolds and mechanical tensile testing.",
                        subject = "Physics",
                        scheduledTimeMillis = now + 90 * 60 * 1000L,
                        durationMinutes = 50,
                        isCompleted = false,
                        priority = TaskPriority.HIGH,
                        recurrence = RecurrenceType.WEEKLY
                    )
                )
            }
        }
    }
}

