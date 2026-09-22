package com.example

import com.example.data.model.RecurrenceType
import com.example.data.model.StudentTask
import com.example.data.model.TaskPriority
import com.example.ui.StudentTaskViewModel
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StudentTaskTest {

    @Test
    fun testStudentTaskCreationAndDefaults() {
        val now = System.currentTimeMillis()
        val task = StudentTask(
            title = "Math Revision",
            summary = "Practice calculus questions 1 through 10",
            subject = "Mathematics",
            scheduledTimeMillis = now + 60000L,
            durationMinutes = 45,
            priority = TaskPriority.HIGH,
            recurrence = RecurrenceType.DAILY
        )

        assertEquals("Math Revision", task.title)
        assertEquals("Practice calculus questions 1 through 10", task.summary)
        assertEquals("Mathematics", task.subject)
        assertEquals(45, task.durationMinutes)
        assertEquals(TaskPriority.HIGH, task.priority)
        assertEquals(RecurrenceType.DAILY, task.recurrence)
        assertFalse(task.isCompleted)
    }

    @Test
    fun testTaskPriorityLevelsAndSorting() {
        val tLow = StudentTask(
            title = "Low Priority Task",
            summary = "Summary",
            scheduledTimeMillis = 1000L,
            priority = TaskPriority.LOW
        )
        val tHigh = StudentTask(
            title = "High Priority Task",
            summary = "Summary",
            scheduledTimeMillis = 2000L,
            priority = TaskPriority.HIGH
        )
        val tMed = StudentTask(
            title = "Medium Priority Task",
            summary = "Summary",
            scheduledTimeMillis = 3000L,
            priority = TaskPriority.MEDIUM
        )

        val tasks = listOf(tLow, tHigh, tMed)
        val sortedByPriority = tasks.sortedBy { it.priority.level }

        assertEquals(TaskPriority.HIGH, sortedByPriority[0].priority)
        assertEquals(TaskPriority.MEDIUM, sortedByPriority[1].priority)
        assertEquals(TaskPriority.LOW, sortedByPriority[2].priority)
    }

    @Test
    fun testDailyRecurrenceCalculation() {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.SEPTEMBER, 15, 10, 0, 0)
        val baseTime = cal.timeInMillis

        val nextTime = StudentTaskViewModel.calculateNextRecurrence(baseTime, RecurrenceType.DAILY)
        val resultCal = Calendar.getInstance().apply { timeInMillis = nextTime }

        // Must be in the future and match expected hour/minute
        assertEquals(10, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCal.get(Calendar.MINUTE))
        assertTrue(nextTime > baseTime)
    }

    @Test
    fun testWeeklyRecurrenceCalculation() {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.SEPTEMBER, 15, 14, 30, 0)
        val baseTime = cal.timeInMillis

        val nextTime = StudentTaskViewModel.calculateNextRecurrence(baseTime, RecurrenceType.WEEKLY)
        val resultCal = Calendar.getInstance().apply { timeInMillis = nextTime }

        assertEquals(14, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, resultCal.get(Calendar.MINUTE))
        assertTrue(nextTime > baseTime)
    }

    @Test
    fun testMonthlyRecurrenceCalculation() {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.SEPTEMBER, 1, 9, 15, 0)
        val baseTime = cal.timeInMillis

        val nextTime = StudentTaskViewModel.calculateNextRecurrence(baseTime, RecurrenceType.MONTHLY)
        val resultCal = Calendar.getInstance().apply { timeInMillis = nextTime }

        assertEquals(9, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(15, resultCal.get(Calendar.MINUTE))
        assertTrue(nextTime > baseTime)
    }

    @Test
    fun testTaskCompletionToggle() {
        val now = System.currentTimeMillis()
        val task = StudentTask(
            title = "History Reading",
            summary = "Read pages 45 to 60",
            subject = "History",
            scheduledTimeMillis = now,
            isCompleted = false
        )

        val completed = task.copy(isCompleted = true, completedAtMillis = now)
        assertTrue(completed.isCompleted)
        assertEquals(now, completed.completedAtMillis)
    }

    @Test
    fun testStudentProfileModel() {
        val profile = com.example.data.model.StudentProfile(
            id = 1L,
            name = "Alex Rivera",
            gradeOrMajor = "Computer Science",
            avatarColorHex = 0xFF4F46E5,
            weeklyGoalHours = 20
        )

        assertEquals("Alex Rivera", profile.name)
        assertEquals("Computer Science", profile.gradeOrMajor)
        assertEquals(20, profile.weeklyGoalHours)
    }

    @Test
    fun testRecurringTaskInstancesGeneration() {
        val seriesId = "series-test-123"
        val now = System.currentTimeMillis()

        val instance1 = StudentTask(
            id = 1L,
            studentProfileId = 1L,
            title = "Daily Algebra",
            summary = "Review quadratic formulas",
            subject = "Mathematics",
            scheduledTimeMillis = now,
            recurrence = RecurrenceType.DAILY,
            recurrenceSeriesId = seriesId,
            occurrenceIndex = 1
        )

        val nextTime = StudentTaskViewModel.calculateNextRecurrence(now, RecurrenceType.DAILY)
        val instance2 = instance1.copy(
            id = 2L,
            scheduledTimeMillis = nextTime,
            occurrenceIndex = 2
        )

        assertEquals(seriesId, instance1.recurrenceSeriesId)
        assertEquals(seriesId, instance2.recurrenceSeriesId)
        assertEquals(1, instance1.occurrenceIndex)
        assertEquals(2, instance2.occurrenceIndex)
        assertTrue(instance2.scheduledTimeMillis > instance1.scheduledTimeMillis)
    }

    @Test
    fun testProgressSummaryCalculation() {
        val profileId = 1L
        val t1 = StudentTask(
            id = 1L,
            studentProfileId = profileId,
            title = "Task 1",
            summary = "Desc",
            scheduledTimeMillis = 1000L,
            durationMinutes = 60,
            isCompleted = true,
            completedAtMillis = 1000L,
            priority = TaskPriority.HIGH
        )
        val t2 = StudentTask(
            id = 2L,
            studentProfileId = profileId,
            title = "Task 2",
            summary = "Desc",
            scheduledTimeMillis = 2000L,
            durationMinutes = 30,
            isCompleted = true,
            completedAtMillis = 2000L,
            priority = TaskPriority.MEDIUM
        )
        val t3 = StudentTask(
            id = 3L,
            studentProfileId = profileId,
            title = "Task 3",
            summary = "Desc",
            scheduledTimeMillis = 3000L,
            durationMinutes = 45,
            isCompleted = false,
            priority = TaskPriority.HIGH
        )

        val tasks = listOf(t1, t2, t3)
        val totalCount = tasks.size
        val completedCount = tasks.count { it.isCompleted }
        val completionRate = (completedCount * 100) / totalCount
        val totalMinutes = tasks.filter { it.isCompleted }.sumOf { it.durationMinutes }

        assertEquals(3, totalCount)
        assertEquals(2, completedCount)
        assertEquals(66, completionRate) // 2/3 = 66%
        assertEquals(90, totalMinutes) // 60 + 30 = 90 mins
    }
}
