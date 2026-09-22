package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.model.StudentProfile
import com.example.data.model.StudentTask
import com.example.data.model.TaskPriority
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    uiState: StudentTasksUiState,
    viewModel: StudentTaskViewModel,
    onNavigateToTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentProfile = uiState.currentProfile
    val summary = uiState.progressSummary

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAddProfileDialog by remember { mutableStateOf(false) }
    var showSwitchProfileDialog by remember { mutableStateOf(false) }

    var historySearchQuery by remember { mutableStateOf("") }
    var selectedSubjectFilter by remember { mutableStateOf<String?>(null) }

    // Filter completed tasks
    val filteredHistory = remember(uiState.completedTasksHistory, historySearchQuery, selectedSubjectFilter) {
        uiState.completedTasksHistory.filter { task ->
            val matchesSubject = selectedSubjectFilter == null || task.subject.equals(selectedSubjectFilter, ignoreCase = true)
            val matchesQuery = historySearchQuery.isBlank() ||
                task.title.contains(historySearchQuery, ignoreCase = true) ||
                task.summary.contains(historySearchQuery, ignoreCase = true)
            matchesSubject && matchesQuery
        }
    }

    val availableSubjects = remember(uiState.completedTasksHistory) {
        uiState.completedTasksHistory.map { it.subject }.distinct().sorted()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 700.dp)
                .testTag("student_profile_lazy_column"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Student Profile Header Card
            item(key = "profile_header") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_profile_header_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            val avatarBg = Color(currentProfile?.avatarColorHex ?: 0xFF4F46E5)
                            val initials = (currentProfile?.name ?: "S")
                                .split(" ")
                                .mapNotNull { it.firstOrNull()?.toString() }
                                .take(2)
                                .joinToString("")
                                .uppercase()

                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(avatarBg)
                                    .testTag("student_avatar"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials.ifBlank { "ST" },
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentProfile?.name ?: "Student",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("student_profile_name")
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = currentProfile?.gradeOrMajor ?: "General Studies",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "Weekly Study Target: ${currentProfile?.weeklyGoalHours ?: 15} hours",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Switch Profile & Edit Profile
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showSwitchProfileDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("switch_profile_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Switch Student")
                            }

                            OutlinedButton(
                                onClick = { showEditProfileDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("edit_profile_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit Profile")
                            }
                        }
                    }
                }
            }

            // 2. Summary of Overall Task Completion Progress
            item(key = "progress_summary_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Task Completion Progress Summary",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 2A. Key Stats & Progress Gauge Card
            item(key = "progress_stats_card") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("progress_summary_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Top Section: Circular Completion Rate Gauge & Main Metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Overall Completion Rate",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${summary.completionRatePercent}%",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = "${summary.completedTasks} of ${summary.totalTasks} tasks completed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Circular Progress
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(76.dp)
                            ) {
                                CircularProgressIndicator(
                                    progress = { summary.completionRatePercent / 100f },
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    strokeWidth = 8.dp,
                                    strokeCap = StrokeCap.Round
                                )
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Linear progress indicator
                        LinearProgressIndicator(
                            progress = { summary.completionRatePercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            strokeCap = StrokeCap.Round
                        )

                        // 3 Quick Stat Metric Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Completed Tasks
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFDCFCE7),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Completed",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF166534)
                                    )
                                    Text(
                                        text = "${summary.completedTasks}",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF166534)
                                        )
                                    )
                                    Text(
                                        text = "Tasks done",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF166534).copy(alpha = 0.8f)
                                    )
                                }
                            }

                            // Pending Tasks
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Pending",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "${summary.pendingTasks}",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    )
                                    Text(
                                        text = "In schedule",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            // Total Study Time
                            val hours = summary.totalStudyMinutesCompleted / 60
                            val minutes = summary.totalStudyMinutesCompleted % 60
                            val timeLabel = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFEEF2FF),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Study Time",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF3730A3)
                                    )
                                    Text(
                                        text = timeLabel,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF3730A3)
                                        )
                                    )
                                    Text(
                                        text = "Logged",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF3730A3).copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        // Weekly Study Target progress
                        val goalMinutes = summary.weeklyGoalHours * 60
                        val goalHoursFormatted = String.format(Locale.getDefault(), "%.1f", summary.totalStudyMinutesCompleted / 60.0)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Weekly Study Target Progress",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$goalHoursFormatted / ${summary.weeklyGoalHours} hrs (${summary.weeklyGoalProgressPercent}%)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { summary.weeklyGoalProgressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF10B981),
                                trackColor = Color(0xFFD1FAE5),
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }

            // 2B. Priority Level Breakdown Card
            item(key = "priority_breakdown_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Completion by Priority Level",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        summary.priorityBreakdown.forEach { p ->
                            val (pColor, pBg) = when (p.priority) {
                                TaskPriority.HIGH -> Color(0xFFDC2626) to Color(0xFFFEE2E2)
                                TaskPriority.MEDIUM -> Color(0xFFD97706) to Color(0xFFFEF3C7)
                                TaskPriority.LOW -> Color(0xFF16A34A) to Color(0xFFDCFCE7)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = pBg,
                                    modifier = Modifier.width(80.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Flag,
                                            contentDescription = null,
                                            tint = pColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = p.priority.label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = pColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                LinearProgressIndicator(
                                    progress = { if (p.totalCount > 0) p.completedCount.toFloat() / p.totalCount else 0f },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = pColor,
                                    trackColor = pBg,
                                    strokeCap = StrokeCap.Round
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = "${p.completedCount}/${p.totalCount} (${p.completionPercentage}%)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 2C. Subject Progress Breakdown Card
            if (summary.subjectBreakdown.isNotEmpty()) {
                item(key = "subject_breakdown_card") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Subject Performance & Completion",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            summary.subjectBreakdown.forEach { subj ->
                                val subjHours = subj.completedMinutes / 60
                                val subjMins = subj.completedMinutes % 60
                                val timeText = if (subjHours > 0) "${subjHours}h ${subjMins}m" else "${subjMins}m"

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = subj.subject,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${subj.completedCount}/${subj.totalCount} completed • $timeText logged",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { if (subj.totalCount > 0) subj.completedCount.toFloat() / subj.totalCount else 0f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                        strokeCap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. History of Completed Tasks Section
            item(key = "completed_history_header") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "History of Completed Tasks",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = "${uiState.completedTasksHistory.size} completed",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Search input for completed history
                    OutlinedTextField(
                        value = historySearchQuery,
                        onValueChange = { historySearchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_search_input"),
                        placeholder = { Text("Search completed task history or notes...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (historySearchQuery.isNotBlank()) {
                                IconButton(onClick = { historySearchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Subject filter chips for history
                    if (availableSubjects.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedSubjectFilter == null,
                                onClick = { selectedSubjectFilter = null },
                                label = { Text("All Subjects") },
                                shape = RoundedCornerShape(8.dp)
                            )
                            availableSubjects.forEach { subj ->
                                FilterChip(
                                    selected = selectedSubjectFilter == subj,
                                    onClick = {
                                        selectedSubjectFilter = if (selectedSubjectFilter == subj) null else subj
                                    },
                                    label = { Text(subj) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Completed Task Cards in History
            if (filteredHistory.isEmpty()) {
                item(key = "empty_history") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("empty_history_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (historySearchQuery.isNotBlank() || selectedSubjectFilter != null)
                                    "No completed tasks match your search"
                                else
                                    "No completed tasks recorded yet",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Complete scheduled tasks from your study list to see your history and achievements recorded here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onNavigateToTasks,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Go to Study Tasks")
                            }
                        }
                    }
                }
            } else {
                items(
                    items = filteredHistory,
                    key = { "completed_${it.id}" }
                ) { task ->
                    CompletedTaskHistoryItem(
                        task = task,
                        onRestore = { viewModel.toggleTaskCompletion(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }

            // Clearance spacer
            item(key = "bottom_profile_spacer") {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Switch Profile Dialog
    if (showSwitchProfileDialog) {
        SwitchProfileDialog(
            profiles = uiState.profiles,
            currentProfileId = currentProfile?.id ?: 1L,
            onSelectProfile = { profileId ->
                viewModel.selectProfile(profileId)
                showSwitchProfileDialog = false
            },
            onAddNewProfile = {
                showSwitchProfileDialog = false
                showAddProfileDialog = true
            },
            onDismiss = { showSwitchProfileDialog = false }
        )
    }

    // Add Profile Dialog
    if (showAddProfileDialog) {
        AddEditProfileDialog(
            isEditing = false,
            initialProfile = null,
            onDismiss = { showAddProfileDialog = false },
            onSave = { name, major, goalHours, colorHex ->
                viewModel.createProfile(name, major, goalHours, colorHex)
                showAddProfileDialog = false
            }
        )
    }

    // Edit Current Profile Dialog
    if (showEditProfileDialog && currentProfile != null) {
        AddEditProfileDialog(
            isEditing = true,
            initialProfile = currentProfile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, major, goalHours, colorHex ->
                viewModel.updateProfile(
                    currentProfile.copy(
                        name = name,
                        gradeOrMajor = major,
                        weeklyGoalHours = goalHours,
                        avatarColorHex = colorHex
                    )
                )
                showEditProfileDialog = false
            }
        )
    }
}

@Composable
fun CompletedTaskHistoryItem(
    task: StudentTask,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    val completedTimestamp = task.completedAtMillis ?: task.scheduledTimeMillis
    val formattedDate = dateFormat.format(Date(completedTimestamp))

    val (priorityColor, priorityBg) = when (task.priority) {
        TaskPriority.HIGH -> Color(0xFFDC2626) to Color(0xFFFEE2E2)
        TaskPriority.MEDIUM -> Color(0xFFD97706) to Color(0xFFFEF3C7)
        TaskPriority.LOW -> Color(0xFF16A34A) to Color(0xFFDCFCE7)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("completed_task_card_${task.id}")
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = Color(0xFF16A34A),
                    modifier = Modifier
                        .size(22.dp)
                        .padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Badges row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = priorityBg
                            ) {
                                Text(
                                    text = task.priority.label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = priorityColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = task.subject,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (task.recurrence != com.example.data.model.RecurrenceType.NONE) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFEEF2FF)
                                ) {
                                    Text(
                                        text = task.recurrence.label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color(0xFF4F46E5),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HourglassBottom,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${task.durationMinutes}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.LineThrough
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Completed: $formattedDate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Summary notes block
            if (task.summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Study Notes:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = task.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (expanded) Int.MAX_VALUE else 2
                        )
                    }
                }
            }

            // Action row: Restore & Delete
            AnimatedVisibility(visible = expanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 32.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onRestore,
                        modifier = Modifier.testTag("restore_task_button_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restore to Active")
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_history_button_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SwitchProfileDialog(
    profiles: List<StudentProfile>,
    currentProfileId: Long,
    onSelectProfile: (Long) -> Unit,
    onAddNewProfile: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Switch Student Profile",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                profiles.forEach { profile ->
                    val isSelected = profile.id == currentProfileId
                    val avatarColor = Color(profile.avatarColorHex)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectProfile(profile.id) }
                            .testTag("profile_item_${profile.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(avatarColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profile.name.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = profile.gradeOrMajor,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAddNewProfile,
                modifier = Modifier.testTag("add_new_profile_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add New Student")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun AddEditProfileDialog(
    isEditing: Boolean,
    initialProfile: StudentProfile?,
    onDismiss: () -> Unit,
    onSave: (name: String, major: String, goalHours: Int, colorHex: Long) -> Unit
) {
    var name by remember { mutableStateOf(initialProfile?.name ?: "") }
    var major by remember { mutableStateOf(initialProfile?.gradeOrMajor ?: "Computer Science & Engineering") }
    var goalHours by remember { mutableIntStateOf(initialProfile?.weeklyGoalHours ?: 15) }
    var selectedColor by remember { mutableStateOf(initialProfile?.avatarColorHex ?: 0xFF4F46E5) }
    var nameError by remember { mutableStateOf(false) }

    val colors = listOf(
        0xFF4F46E5, // Indigo
        0xFF059669, // Emerald
        0xFFDC2626, // Red
        0xFFD97706, // Amber
        0xFF7C3AED, // Violet
        0xFF0284C7  // Sky Blue
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Edit Student Profile" else "Create Student Profile",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError && it.isNotBlank()) nameError = false
                    },
                    label = { Text("Student Name *") },
                    placeholder = { Text("e.g., Alex Rivera") },
                    isError = nameError,
                    supportingText = { if (nameError) Text("Student name is required") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = major,
                    onValueChange = { major = it },
                    label = { Text("Grade / Major") },
                    placeholder = { Text("e.g., Biology, 12th Grade") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_major_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Weekly Goal Hours
                Text(
                    text = "Weekly Study Goal: $goalHours hours",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(10, 15, 20, 25, 30).forEach { hours ->
                        FilterChip(
                            selected = goalHours == hours,
                            onClick = { goalHours = hours },
                            label = { Text("${hours}h") },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                // Avatar Color Selection
                Text(
                    text = "Profile Avatar Theme",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { c ->
                        val isSelected = selectedColor == c
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(c))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = c },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        onSave(name.trim(), major.trim(), goalHours, selectedColor)
                    }
                },
                modifier = Modifier.testTag("save_profile_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (isEditing) "Save Profile" else "Create Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
