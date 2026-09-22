package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.RecurrenceType
import com.example.data.model.StudentTask
import com.example.data.model.TaskPriority
import com.example.ui.components.AddEditTaskDialog
import com.example.ui.components.ClockCard
import com.example.ui.components.FocusTimerDialog
import com.example.ui.components.StudyAlertBanner
import com.example.ui.components.TaskItemCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentTaskScreen(
    viewModel: StudentTaskViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<StudentTask?>(null) }
    var showSearchBar by remember { mutableStateOf(false) }

    // Request notification permission for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Identify next upcoming task
    val upcomingTask = remember(uiState.tasks, uiState.currentTimeMillis) {
        uiState.tasks
            .filter { !it.isCompleted && it.scheduledTimeMillis > uiState.currentTimeMillis }
            .minByOrNull { it.scheduledTimeMillis }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (uiState.currentTab == 0) "Student Tasks & Clock" else "Student Profile",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        val studentName = uiState.currentProfile?.name ?: "Student"
                        val totalTasks = uiState.tasks.size
                        val completedCount = uiState.tasks.count { it.isCompleted }
                        Text(
                            text = if (uiState.currentTab == 0) {
                                "$studentName • $completedCount/$totalTasks done"
                            } else {
                                "$studentName • Completion History & Progress"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (uiState.currentTab == 0) {
                        IconButton(
                            onClick = {
                                showSearchBar = !showSearchBar
                                if (!showSearchBar) viewModel.setSearchQuery("")
                            },
                            modifier = Modifier.testTag("search_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (showSearchBar) Icons.Default.Clear else Icons.Default.Search,
                                contentDescription = "Search tasks"
                            )
                        }
                    }

                    // Avatar button in top bar to switch to profile or view
                    val avatarColor = Color(uiState.currentProfile?.avatarColorHex ?: 0xFF4F46E5)
                    val initials = (uiState.currentProfile?.name ?: "S")
                        .take(1)
                        .uppercase()

                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(avatarColor)
                            .clickable {
                                viewModel.switchTab(if (uiState.currentTab == 0) 1 else 0)
                            }
                            .testTag("top_bar_profile_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = uiState.currentTab == 0,
                    onClick = { viewModel.switchTab(0) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Tasks & Clock"
                        )
                    },
                    label = { Text("Tasks & Clock") },
                    modifier = Modifier.testTag("nav_item_tasks")
                )

                NavigationBarItem(
                    selected = uiState.currentTab == 1,
                    onClick = { viewModel.switchTab(1) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Student Profile"
                        )
                    },
                    label = { Text("Student Profile") },
                    modifier = Modifier.testTag("nav_item_profile")
                )
            }
        },
        floatingActionButton = {
            if (uiState.currentTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = {
                        taskToEdit = null
                        showAddDialog = true
                    },
                    modifier = Modifier.testTag("add_task_fab"),
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add Study Task", fontWeight = FontWeight.SemiBold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        if (uiState.currentTab == 1) {
            // Student Profile & Progress Summary View
            StudentProfileScreen(
                uiState = uiState,
                viewModel = viewModel,
                onNavigateToTasks = { viewModel.switchTab(0) },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            // Tasks & Live Clock View
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 700.dp)
                        .testTag("tasks_lazy_column"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Prominent Live Clock Card
                    item(key = "clock_card") {
                        ClockCard(
                            currentTimeMillis = uiState.currentTimeMillis,
                            activeStudyTask = uiState.activeStudyTask,
                            upcomingTask = upcomingTask,
                            onTriggerTestAlert = {
                                viewModel.createQuickTestTask()
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Test study task created! Clock will inform you in 5 seconds."
                                    )
                                }
                            }
                        )
                    }

                    // 2. Active Study Time Alert Banner
                    if (uiState.activeStudyTask != null) {
                        item(key = "study_alert_banner") {
                            StudyAlertBanner(
                                activeTask = uiState.activeStudyTask,
                                onStartFocus = { task ->
                                    viewModel.startFocusSession(task)
                                },
                                onSnooze = { task ->
                                    viewModel.snoozeTask(task, minutes = 5)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Snoozed for 5 minutes")
                                    }
                                },
                                onMarkComplete = { task ->
                                    viewModel.toggleTaskCompletion(task)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Task marked as completed! Great job!")
                                    }
                                },
                                onDismiss = { task ->
                                    viewModel.dismissAlert(task.id)
                                }
                            )
                        }
                    }

                    // 3. Search Bar (if opened)
                    if (showSearchBar) {
                        item(key = "search_bar") {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("search_text_field"),
                                placeholder = { Text("Search by task title, summary, or subject...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (uiState.searchQuery.isNotBlank()) {
                                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // 4. Filters & Controls
                    item(key = "filter_chips") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TaskFilter.entries.forEach { filter ->
                                    FilterChip(
                                        selected = uiState.selectedFilter == filter,
                                        onClick = { viewModel.setFilter(filter) },
                                        label = { Text(filter.label) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
                                    )
                                }
                            }

                            // Sorting Controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Sort:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TaskSortOrder.entries.forEach { sort ->
                                    FilterChip(
                                        selected = uiState.sortOrder == sort,
                                        onClick = { viewModel.setSortOrder(sort) },
                                        label = {
                                            Text(
                                                if (sort == TaskSortOrder.PRIORITY) "Priority (High → Low)" else "Time",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("sort_chip_${sort.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    }

                    // 5. Section Header
                    item(key = "section_header") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Study Tasks (${uiState.currentProfile?.name ?: "All"})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${uiState.tasks.size} tasks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 6. Task List items
                    if (uiState.tasks.isEmpty()) {
                        item(key = "empty_state") {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp)
                                    .testTag("empty_state_card"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        modifier = Modifier.size(56.dp),
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (uiState.searchQuery.isNotBlank()) "No tasks match your search"
                                        else "No study tasks scheduled for ${uiState.currentProfile?.name ?: "this student"}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Add your study tasks, set recurring schedules (Daily, Weekly, Monthly), and the app will inform you when study time arrives.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            taskToEdit = null
                                            showAddDialog = true
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Create Task")
                                    }
                                }
                            }
                        }
                    } else {
                        items(
                            items = uiState.tasks,
                            key = { it.id }
                        ) { task ->
                            TaskItemCard(
                                task = task,
                                currentTimeMillis = uiState.currentTimeMillis,
                                onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                                onEdit = {
                                    taskToEdit = task
                                    showAddDialog = true
                                },
                                onDelete = {
                                    viewModel.deleteTask(task)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Task deleted")
                                    }
                                },
                                onStartFocus = {
                                    viewModel.startFocusSession(task)
                                },
                                onGenerateNextOccurrence = if (task.recurrence != RecurrenceType.NONE) {
                                    {
                                        viewModel.generateFutureOccurrences(task, count = 2)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Generated future recurring instances for ${task.title}")
                                        }
                                    }
                                } else null
                            )
                        }
                    }

                    // Bottom spacer for FAB clearance
                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddDialog) {
        AddEditTaskDialog(
            initialTask = taskToEdit,
            onDismiss = { showAddDialog = false },
            onSave = { title, summary, subject, scheduledTime, duration, priority, recurrence, futureInstancesCount ->
                if (taskToEdit == null) {
                    viewModel.addTask(
                        title = title,
                        summary = summary,
                        subject = subject,
                        scheduledTimeMillis = scheduledTime,
                        durationMinutes = duration,
                        priority = priority,
                        recurrence = recurrence,
                        futureInstancesCount = futureInstancesCount
                    )
                    scope.launch {
                        val message = if (recurrence != RecurrenceType.NONE) {
                            "Scheduled ${recurrence.label.lowercase()} task and auto-generated future instances!"
                        } else {
                            "Study task scheduled (${priority.label} Priority)!"
                        }
                        snackbarHostState.showSnackbar(message)
                    }
                } else {
                    viewModel.updateTask(taskToEdit!!, title, summary, subject, scheduledTime, duration, priority, recurrence)
                    scope.launch {
                        snackbarHostState.showSnackbar("Task updated!")
                    }
                }
                showAddDialog = false
            }
        )
    }

    // Focus Timer Session Dialog
    if (uiState.activeFocusTask != null) {
        FocusTimerDialog(
            task = uiState.activeFocusTask!!,
            onDismiss = { viewModel.endFocusSession() },
            onCompleteTask = { task ->
                viewModel.toggleTaskCompletion(task)
                viewModel.endFocusSession()
                scope.launch {
                    snackbarHostState.showSnackbar("Study session completed! Great job!")
                }
            }
        )
    }
}
