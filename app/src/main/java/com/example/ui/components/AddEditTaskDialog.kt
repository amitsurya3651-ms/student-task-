package com.example.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.RecurrenceType
import com.example.data.model.StudentTask
import com.example.data.model.TaskPriority
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskDialog(
    initialTask: StudentTask? = null,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        summary: String,
        subject: String,
        scheduledTimeMillis: Long,
        durationMinutes: Int,
        priority: TaskPriority,
        recurrence: RecurrenceType,
        futureInstancesCount: Int
    ) -> Unit
) {
    val context = LocalContext.current
    val isEditing = initialTask != null

    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var summary by remember { mutableStateOf(initialTask?.summary ?: "") }
    var subject by remember { mutableStateOf(initialTask?.subject ?: "Mathematics") }
    var durationMinutes by remember { mutableIntStateOf(initialTask?.durationMinutes ?: 30) }
    var priority by remember { mutableStateOf(initialTask?.priority ?: TaskPriority.MEDIUM) }
    var recurrence by remember { mutableStateOf(initialTask?.recurrence ?: RecurrenceType.NONE) }
    var futureInstancesCount by remember { mutableIntStateOf(3) }

    // Default scheduled time: either existing task time or 15 minutes from now
    var scheduledTimeMillis by remember {
        mutableLongStateOf(initialTask?.scheduledTimeMillis ?: (System.currentTimeMillis() + 15 * 60 * 1000L))
    }

    var titleError by remember { mutableStateOf(false) }

    val subjects = listOf("Mathematics", "Physics", "Chemistry", "History", "Literature", "Coding", "General")
    val durations = listOf(15, 25, 30, 45, 60, 90)

    val timeFormat = SimpleDateFormat("h:mm a, MMM d", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Edit Student Task" else "Add New Student Task",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.testTag("dialog_title")
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Task Title
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (titleError && it.isNotBlank()) titleError = false
                    },
                    label = { Text("Task Title *") },
                    placeholder = { Text("e.g., Calculus Problem Set, Chapter 4") },
                    leadingIcon = {
                        Icon(Icons.Default.Book, contentDescription = null)
                    },
                    isError = titleError,
                    supportingText = {
                        if (titleError) Text("Title is required")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Priority Level Selector (High, Medium, Low)
                Text(
                    text = "Priority Level",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskPriority.entries.forEach { p ->
                        val isSelected = priority == p
                        val (pColor, pContainer) = when (p) {
                            TaskPriority.HIGH -> Color(0xFFDC2626) to Color(0xFFFEE2E2)
                            TaskPriority.MEDIUM -> Color(0xFFD97706) to Color(0xFFFEF3C7)
                            TaskPriority.LOW -> Color(0xFF16A34A) to Color(0xFFDCFCE7)
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { priority = p },
                            label = { Text(p.label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = if (isSelected) pColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = pContainer,
                                selectedLabelColor = pColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("priority_chip_${p.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Recurrence Selector (None, Daily, Weekly, Monthly)
                Text(
                    text = "Recurrence (Repeat Schedule)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RecurrenceType.entries.forEach { r ->
                        val isSelected = recurrence == r
                        FilterChip(
                            selected = isSelected,
                            onClick = { recurrence = r },
                            label = { Text(r.label) },
                            leadingIcon = if (r != RecurrenceType.NONE) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Repeat,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("recurrence_chip_${r.name.lowercase()}")
                        )
                    }
                }

                // If recurring is enabled, configure automatic future instance generation
                if (recurrence != RecurrenceType.NONE && !isEditing) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Auto-Generate Future Occurrences:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(2, 3, 5).forEach { count ->
                            FilterChip(
                                selected = futureInstancesCount == count,
                                onClick = { futureInstancesCount = count },
                                label = { Text("$count occurrences") },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                    Text(
                        text = "The app will automatically schedule $futureInstancesCount future ${recurrence.label.lowercase()} instances and generate further instances as you complete them.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Task Summary (Notes & details)
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Task Summary & Study Notes") },
                    placeholder = {
                        Text("Put your task summary, formulas, focus chapters, or homework checklist here...")
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Notes,
                            contentDescription = null,
                            modifier = Modifier.padding(bottom = 48.dp)
                        )
                    },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_summary_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Subject Selector
                Text(
                    text = "Subject",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjects.forEach { item ->
                        FilterChip(
                            selected = subject == item,
                            onClick = { subject = item },
                            label = { Text(item) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scheduled Time with Quick Presets & Clock Picker
                Text(
                    text = "Scheduled Study Clock Time",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = scheduledTimeMillis
                        }
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                val updatedCal = Calendar.getInstance().apply {
                                    timeInMillis = scheduledTimeMillis
                                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                                    set(Calendar.MINUTE, minute)
                                    set(Calendar.SECOND, 0)
                                }
                                scheduledTimeMillis = updatedCal.timeInMillis
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pick_clock_time_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Time: ${timeFormat.format(Date(scheduledTimeMillis))}")
                }

                // Quick presets
                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SuggestionChip(
                        onClick = { scheduledTimeMillis = System.currentTimeMillis() + 60 * 1000L },
                        label = { Text("In 1m") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    SuggestionChip(
                        onClick = { scheduledTimeMillis = System.currentTimeMillis() + 10 * 60 * 1000L },
                        label = { Text("In 10m") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    SuggestionChip(
                        onClick = { scheduledTimeMillis = System.currentTimeMillis() + 30 * 60 * 1000L },
                        label = { Text("In 30m") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    SuggestionChip(
                        onClick = { scheduledTimeMillis = System.currentTimeMillis() + 60 * 60 * 1000L },
                        label = { Text("In 1h") },
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Duration Selector
                Text(
                    text = "Study Duration",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    durations.forEach { duration ->
                        FilterChip(
                            selected = durationMinutes == duration,
                            onClick = { durationMinutes = duration },
                            label = { Text("${duration}m") },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        onSave(title, summary, subject, scheduledTimeMillis, durationMinutes, priority, recurrence, futureInstancesCount)
                    }
                },
                modifier = Modifier.testTag("save_task_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (isEditing) "Save Changes" else "Schedule Task")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_task_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
