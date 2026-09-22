package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.StudyAlarmManager
import com.example.data.local.TaskDatabase
import com.example.data.model.RecurrenceType
import com.example.data.model.StudentProfile
import com.example.data.model.StudentTask
import com.example.data.model.TaskPriority
import com.example.data.repository.TaskRepository
import java.util.Calendar
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilter(val label: String) {
    ALL("All Tasks"),
    UPCOMING("Upcoming & Due"),
    COMPLETED("Completed")
}

enum class TaskSortOrder(val label: String) {
    PRIORITY("Priority (High → Low)"),
    TIME("Time (Earliest First)")
}

data class PriorityProgress(
    val priority: TaskPriority,
    val completedCount: Int,
    val totalCount: Int
) {
    val completionPercentage: Int
        get() = if (totalCount > 0) (completedCount * 100) / totalCount else 0
}

data class SubjectProgress(
    val subject: String,
    val completedCount: Int,
    val totalCount: Int,
    val completedMinutes: Int
) {
    val completionPercentage: Int
        get() = if (totalCount > 0) (completedCount * 100) / totalCount else 0
}

data class StudentProgressSummary(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val completionRatePercent: Int = 0,
    val totalStudyMinutesCompleted: Int = 0,
    val weeklyGoalHours: Int = 15,
    val weeklyGoalProgressPercent: Int = 0,
    val priorityBreakdown: List<PriorityProgress> = emptyList(),
    val subjectBreakdown: List<SubjectProgress> = emptyList()
)

data class StudentTasksUiState(
    val tasks: List<StudentTask> = emptyList(),
    val currentTimeMillis: Long = System.currentTimeMillis(),
    val activeStudyTask: StudentTask? = null,
    val selectedFilter: TaskFilter = TaskFilter.ALL,
    val sortOrder: TaskSortOrder = TaskSortOrder.PRIORITY,
    val searchQuery: String = "",
    val activeFocusTask: StudentTask? = null,
    val profiles: List<StudentProfile> = emptyList(),
    val currentProfile: StudentProfile? = null,
    val completedTasksHistory: List<StudentTask> = emptyList(),
    val progressSummary: StudentProgressSummary = StudentProgressSummary(),
    val currentTab: Int = 0 // 0: Tasks & Clock, 1: Student Profile
)

private data class FilterState(
    val filter: TaskFilter = TaskFilter.ALL,
    val sortOrder: TaskSortOrder = TaskSortOrder.PRIORITY,
    val query: String = "",
    val dismissedIds: Set<Long> = emptySet(),
    val focusTask: StudentTask? = null,
    val selectedProfileId: Long? = null,
    val currentTab: Int = 0
)

class StudentTaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMillis: StateFlow<Long> = _currentTimeMillis.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())

    init {
        val database = TaskDatabase.getDatabase(application)
        repository = TaskRepository(database.taskDao(), database.profileDao())

        // Ensure notification channel is initialized
        StudyAlarmManager.createNotificationChannel(application)

        // Continuous Clock Ticker: updates every second
        viewModelScope.launch {
            while (true) {
                _currentTimeMillis.value = System.currentTimeMillis()
                delay(1000L)
            }
        }
    }

    val uiState: StateFlow<StudentTasksUiState> = combine(
        repository.allTasks,
        repository.allProfiles,
        _currentTimeMillis,
        _filterState
    ) { allTasks, allProfiles, now, state ->

        // Active profile resolution
        val currentProfile = allProfiles.firstOrNull { it.id == state.selectedProfileId }
            ?: allProfiles.firstOrNull()
        val currentProfileId = currentProfile?.id ?: 1L

        // Tasks associated with the active student profile (or unassigned tasks default to profile 1)
        val profileTasks = allTasks.filter {
            it.studentProfileId == currentProfileId || (allProfiles.size <= 1 && it.studentProfileId == 1L)
        }

        // Check if any non-completed task is happening right now (scheduledTime <= now <= scheduledTime + duration)
        val activeTask = profileTasks.firstOrNull { task ->
            !task.isCompleted &&
                task.id !in state.dismissedIds &&
                task.scheduledTimeMillis <= now &&
                now <= (task.scheduledTimeMillis + task.durationMinutes * 60L * 1000L)
        }

        val filteredTasks = profileTasks.filter { task ->
            val matchesFilter = when (state.filter) {
                TaskFilter.ALL -> true
                TaskFilter.UPCOMING -> !task.isCompleted
                TaskFilter.COMPLETED -> task.isCompleted
            }
            val matchesQuery = state.query.isBlank() ||
                task.title.contains(state.query, ignoreCase = true) ||
                task.summary.contains(state.query, ignoreCase = true) ||
                task.subject.contains(state.query, ignoreCase = true)

            matchesFilter && matchesQuery
        }

        // Sort tasks: Active uncompleted first, then by selected sort order (default: Priority High -> Med -> Low)
        val sortedTasks = filteredTasks.sortedWith { a, b ->
            if (a.isCompleted != b.isCompleted) {
                a.isCompleted.compareTo(b.isCompleted)
            } else if (state.sortOrder == TaskSortOrder.PRIORITY) {
                if (a.priority.level != b.priority.level) {
                    a.priority.level.compareTo(b.priority.level)
                } else {
                    a.scheduledTimeMillis.compareTo(b.scheduledTimeMillis)
                }
            } else {
                a.scheduledTimeMillis.compareTo(b.scheduledTimeMillis)
            }
        }

        // History of completed tasks for this student profile, sorted by completed time descending
        val completedHistory = profileTasks
            .filter { it.isCompleted }
            .sortedByDescending { it.completedAtMillis ?: it.scheduledTimeMillis }

        // Comprehensive progress summary calculations
        val totalTasks = profileTasks.size
        val completedCount = completedHistory.size
        val pendingCount = totalTasks - completedCount
        val completionRate = if (totalTasks > 0) (completedCount * 100) / totalTasks else 0
        val totalStudyMinutes = completedHistory.sumOf { it.durationMinutes }
        val goalHours = currentProfile?.weeklyGoalHours ?: 15
        val goalMinutes = goalHours * 60
        val goalProgressPercent = if (goalMinutes > 0) {
            ((totalStudyMinutes * 100) / goalMinutes).coerceAtMost(100)
        } else 0

        val priorityBreakdown = TaskPriority.entries.map { p ->
            val pTasks = profileTasks.filter { it.priority == p }
            val pCompleted = pTasks.count { it.isCompleted }
            PriorityProgress(priority = p, completedCount = pCompleted, totalCount = pTasks.size)
        }

        val subjectNames = profileTasks.map { it.subject }.distinct().sorted()
        val subjectBreakdown = subjectNames.map { subj ->
            val subjTasks = profileTasks.filter { it.subject.equals(subj, ignoreCase = true) }
            val subjCompleted = subjTasks.count { it.isCompleted }
            val subjMinutes = subjTasks.filter { it.isCompleted }.sumOf { it.durationMinutes }
            SubjectProgress(
                subject = subj,
                completedCount = subjCompleted,
                totalCount = subjTasks.size,
                completedMinutes = subjMinutes
            )
        }

        val progressSummary = StudentProgressSummary(
            totalTasks = totalTasks,
            completedTasks = completedCount,
            pendingTasks = pendingCount,
            completionRatePercent = completionRate,
            totalStudyMinutesCompleted = totalStudyMinutes,
            weeklyGoalHours = goalHours,
            weeklyGoalProgressPercent = goalProgressPercent,
            priorityBreakdown = priorityBreakdown,
            subjectBreakdown = subjectBreakdown
        )

        StudentTasksUiState(
            tasks = sortedTasks,
            currentTimeMillis = now,
            activeStudyTask = activeTask,
            selectedFilter = state.filter,
            sortOrder = state.sortOrder,
            searchQuery = state.query,
            activeFocusTask = state.focusTask,
            profiles = allProfiles,
            currentProfile = currentProfile,
            completedTasksHistory = completedHistory,
            progressSummary = progressSummary,
            currentTab = state.currentTab
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StudentTasksUiState()
    )

    fun selectProfile(profileId: Long) {
        _filterState.value = _filterState.value.copy(selectedProfileId = profileId)
    }

    fun switchTab(tabIndex: Int) {
        _filterState.value = _filterState.value.copy(currentTab = tabIndex)
    }

    fun setFilter(filter: TaskFilter) {
        _filterState.value = _filterState.value.copy(filter = filter)
    }

    fun setSortOrder(sortOrder: TaskSortOrder) {
        _filterState.value = _filterState.value.copy(sortOrder = sortOrder)
    }

    fun setSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(query = query)
    }

    fun dismissAlert(taskId: Long) {
        _filterState.value = _filterState.value.copy(
            dismissedIds = _filterState.value.dismissedIds + taskId
        )
    }

    fun startFocusSession(task: StudentTask) {
        _filterState.value = _filterState.value.copy(focusTask = task)
    }

    fun endFocusSession() {
        _filterState.value = _filterState.value.copy(focusTask = null)
    }

    // Profile Management
    fun createProfile(
        name: String,
        gradeOrMajor: String,
        weeklyGoalHours: Int = 15,
        avatarColorHex: Long = 0xFF4F46E5,
        avatarEmoji: String = "🎓"
    ) {
        viewModelScope.launch {
            val newProfile = StudentProfile(
                name = name.trim().ifBlank { "New Student" },
                gradeOrMajor = gradeOrMajor.trim().ifBlank { "General Studies" },
                weeklyGoalHours = weeklyGoalHours.coerceIn(1, 80),
                avatarColorHex = avatarColorHex,
                avatarEmoji = avatarEmoji
            )
            val newId = repository.insertProfile(newProfile)
            _filterState.value = _filterState.value.copy(selectedProfileId = newId)
        }
    }

    fun updateProfile(profile: StudentProfile) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    fun deleteProfile(profile: StudentProfile) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
            val remaining = uiState.value.profiles.filter { it.id != profile.id }
            _filterState.value = _filterState.value.copy(
                selectedProfileId = remaining.firstOrNull()?.id
            )
        }
    }

    /**
     * Add a task with recurring schedule support.
     * When recurrence is set (Daily, Weekly, Monthly), future instances are automatically generated.
     */
    fun addTask(
        title: String,
        summary: String,
        subject: String,
        scheduledTimeMillis: Long,
        durationMinutes: Int,
        priority: TaskPriority = TaskPriority.MEDIUM,
        recurrence: RecurrenceType = RecurrenceType.NONE,
        futureInstancesCount: Int = 3
    ) {
        viewModelScope.launch {
            val profileId = uiState.value.currentProfile?.id ?: 1L
            val seriesId = if (recurrence != RecurrenceType.NONE) UUID.randomUUID().toString() else null

            val initialTask = StudentTask(
                studentProfileId = profileId,
                title = title.trim(),
                summary = summary.trim(),
                subject = subject.trim().ifBlank { "General" },
                scheduledTimeMillis = scheduledTimeMillis,
                durationMinutes = durationMinutes,
                isCompleted = false,
                priority = priority,
                recurrence = recurrence,
                recurrenceSeriesId = seriesId,
                occurrenceIndex = 1
            )
            val newId = repository.insertTask(initialTask)
            val taskWithId = initialTask.copy(id = newId)
            StudyAlarmManager.scheduleTaskAlarm(getApplication(), taskWithId)

            // Automatically generate future instances for recurring tasks
            if (recurrence != RecurrenceType.NONE && futureInstancesCount > 1 && seriesId != null) {
                var lastScheduledTime = scheduledTimeMillis
                for (occurrenceIndex in 2..futureInstancesCount) {
                    val nextTime = calculateNextRecurrence(lastScheduledTime, recurrence)
                    val nextTask = initialTask.copy(
                        id = 0,
                        scheduledTimeMillis = nextTime,
                        recurrenceSeriesId = seriesId,
                        occurrenceIndex = occurrenceIndex,
                        isCompleted = false,
                        hasNotified = false
                    )
                    val generatedId = repository.insertTask(nextTask)
                    StudyAlarmManager.scheduleTaskAlarm(getApplication(), nextTask.copy(id = generatedId))
                    lastScheduledTime = nextTime
                }
            }
        }
    }

    fun updateTask(
        task: StudentTask,
        title: String,
        summary: String,
        subject: String,
        scheduledTimeMillis: Long,
        durationMinutes: Int,
        priority: TaskPriority = task.priority,
        recurrence: RecurrenceType = task.recurrence
    ) {
        viewModelScope.launch {
            val updated = task.copy(
                title = title.trim(),
                summary = summary.trim(),
                subject = subject.trim().ifBlank { "General" },
                scheduledTimeMillis = scheduledTimeMillis,
                durationMinutes = durationMinutes,
                isCompleted = false,
                priority = priority,
                recurrence = recurrence
            )
            repository.updateTask(updated)
            StudyAlarmManager.cancelTaskAlarm(getApplication(), task.id)
            StudyAlarmManager.scheduleTaskAlarm(getApplication(), updated)
        }
    }

    /**
     * Toggles completion status of a task.
     * When completed, records completion timestamp for profile history and progress tracking.
     * If the task has a recurring schedule, automatically generates the next upcoming occurrence.
     */
    fun toggleTaskCompletion(task: StudentTask) {
        viewModelScope.launch {
            val newStatus = !task.isCompleted
            repository.setTaskCompleted(task.id, newStatus)

            if (newStatus) {
                StudyAlarmManager.cancelTaskAlarm(getApplication(), task.id)
                if (_filterState.value.focusTask?.id == task.id) {
                    _filterState.value = _filterState.value.copy(focusTask = null)
                }

                // If task is recurring, automatically ensure future instances continue
                if (task.recurrence != RecurrenceType.NONE) {
                    val seriesId = task.recurrenceSeriesId ?: UUID.randomUUID().toString()
                    val seriesTasks = repository.getTasksInSeries(seriesId)
                    val futureIncompleteExists = seriesTasks.any {
                        !it.isCompleted && it.scheduledTimeMillis > System.currentTimeMillis()
                    }

                    if (!futureIncompleteExists) {
                        val baseTime = seriesTasks.maxOfOrNull { it.scheduledTimeMillis } ?: task.scheduledTimeMillis
                        val nextTime = calculateNextRecurrence(baseTime, task.recurrence)
                        val nextIndex = (seriesTasks.maxOfOrNull { it.occurrenceIndex } ?: task.occurrenceIndex) + 1
                        val nextTask = task.copy(
                            id = 0,
                            scheduledTimeMillis = nextTime,
                            isCompleted = false,
                            completedAtMillis = null,
                            hasNotified = false,
                            recurrenceSeriesId = seriesId,
                            occurrenceIndex = nextIndex
                        )
                        val nextId = repository.insertTask(nextTask)
                        StudyAlarmManager.scheduleTaskAlarm(getApplication(), nextTask.copy(id = nextId))
                    }
                }
            } else {
                if (task.scheduledTimeMillis > System.currentTimeMillis()) {
                    StudyAlarmManager.scheduleTaskAlarm(getApplication(), task)
                }
            }
        }
    }

    /**
     * Explicitly generate additional future occurrences for a recurring task series.
     */
    fun generateFutureOccurrences(task: StudentTask, count: Int = 3) {
        viewModelScope.launch {
            if (task.recurrence == RecurrenceType.NONE) return@launch
            val seriesId = task.recurrenceSeriesId ?: UUID.randomUUID().toString()
            if (task.recurrenceSeriesId == null) {
                repository.updateTask(task.copy(recurrenceSeriesId = seriesId))
            }
            val seriesTasks = repository.getTasksInSeries(seriesId)
            val maxOccurrence = seriesTasks.maxOfOrNull { it.occurrenceIndex } ?: task.occurrenceIndex
            var lastTime = seriesTasks.maxOfOrNull { it.scheduledTimeMillis } ?: task.scheduledTimeMillis

            for (i in 1..count) {
                val nextTime = calculateNextRecurrence(lastTime, task.recurrence)
                val newIndex = maxOccurrence + i
                val futureTask = task.copy(
                    id = 0,
                    scheduledTimeMillis = nextTime,
                    isCompleted = false,
                    completedAtMillis = null,
                    hasNotified = false,
                    recurrenceSeriesId = seriesId,
                    occurrenceIndex = newIndex
                )
                val generatedId = repository.insertTask(futureTask)
                StudyAlarmManager.scheduleTaskAlarm(getApplication(), futureTask.copy(id = generatedId))
                lastTime = nextTime
            }
        }
    }

    fun deleteTask(task: StudentTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
            StudyAlarmManager.cancelTaskAlarm(getApplication(), task.id)
            if (_filterState.value.focusTask?.id == task.id) {
                _filterState.value = _filterState.value.copy(focusTask = null)
            }
        }
    }

    fun snoozeTask(task: StudentTask, minutes: Int = 5) {
        viewModelScope.launch {
            val newTime = System.currentTimeMillis() + (minutes * 60 * 1000L)
            val updated = task.copy(
                scheduledTimeMillis = newTime,
                isCompleted = false,
                hasNotified = false
            )
            // Remove from dismissed so alert can pop up again when snoozed time arrives
            _filterState.value = _filterState.value.copy(
                dismissedIds = _filterState.value.dismissedIds - task.id
            )
            repository.updateTask(updated)
            StudyAlarmManager.cancelTaskAlarm(getApplication(), task.id)
            StudyAlarmManager.scheduleTaskAlarm(getApplication(), updated)
        }
    }

    fun createQuickTestTask() {
        val targetTime = System.currentTimeMillis() + 5000L
        addTask(
            title = "Flashcards Quick Review",
            summary = "Review chemistry formulas and periodic table flashcards before the quiz.",
            subject = "Chemistry",
            scheduledTimeMillis = targetTime,
            durationMinutes = 15,
            priority = TaskPriority.HIGH,
            recurrence = RecurrenceType.DAILY,
            futureInstancesCount = 2
        )
    }

    companion object {
        fun calculateNextRecurrence(scheduledTime: Long, recurrence: RecurrenceType): Long {
            val cal = Calendar.getInstance()
            cal.timeInMillis = scheduledTime
            when (recurrence) {
                RecurrenceType.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
                RecurrenceType.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                RecurrenceType.MONTHLY -> cal.add(Calendar.MONTH, 1)
                RecurrenceType.NONE -> {}
            }
            val now = System.currentTimeMillis()
            while (cal.timeInMillis < now && recurrence != RecurrenceType.NONE) {
                when (recurrence) {
                    RecurrenceType.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
                    RecurrenceType.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                    RecurrenceType.MONTHLY -> cal.add(Calendar.MONTH, 1)
                    RecurrenceType.NONE -> break
                }
            }
            return cal.timeInMillis
        }
    }
}
