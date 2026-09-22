package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.RecurrenceType
import com.example.data.model.StudentTask
import com.example.data.model.TaskPriority
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TaskItemCard(
    task: StudentTask,
    currentTimeMillis: Long,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStartFocus: () -> Unit,
    onGenerateNextOccurrence: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    val scheduledDate = Date(task.scheduledTimeMillis)
    val timeFormatted = timeFormat.format(scheduledDate)
    val dateFormatted = dateFormat.format(scheduledDate)

    val isNow = !task.isCompleted &&
        task.scheduledTimeMillis <= currentTimeMillis &&
        currentTimeMillis <= (task.scheduledTimeMillis + task.durationMinutes * 60000L)

    val isPastDue = !task.isCompleted &&
        (currentTimeMillis > (task.scheduledTimeMillis + task.durationMinutes * 60000L))

    val subjectColor = when (task.subject.lowercase()) {
        "mathematics", "math" -> Color(0xFF3B5998)
        "physics", "science" -> Color(0xFF0284C7)
        "history" -> Color(0xFFB45309)
        "literature", "english" -> Color(0xFF7C3AED)
        "chemistry" -> Color(0xFF059669)
        "coding", "computer science" -> Color(0xFF0D9488)
        else -> MaterialTheme.colorScheme.secondary
    }

    val (priorityColor, priorityBg) = when (task.priority) {
        TaskPriority.HIGH -> Color(0xFFDC2626) to Color(0xFFFEE2E2)
        TaskPriority.MEDIUM -> Color(0xFFD97706) to Color(0xFFFEF3C7)
        TaskPriority.LOW -> Color(0xFF16A34A) to Color(0xFFDCFCE7)
    }

    val cardBorder = if (isNow) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary)
    } else if (task.priority == TaskPriority.HIGH && !task.isCompleted) {
        BorderStroke(1.5.dp, Color(0xFFF87171).copy(alpha = 0.8f))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }

    val cardAlpha = if (task.isCompleted) 0.65f else 1.0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .testTag("task_card_${task.id}")
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNow) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surface
        ),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNow) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Checkbox, Title & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleComplete() },
                    modifier = Modifier
                        .testTag("task_checkbox_${task.id}")
                        .size(24.dp)
                        .padding(top = 2.dp),
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Badge Row: Priority, Subject, Recurrence & Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Priority Badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = priorityBg,
                                modifier = Modifier.testTag("task_priority_badge_${task.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Flag,
                                        contentDescription = null,
                                        tint = priorityColor,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = task.priority.name,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = priorityColor
                                    )
                                }
                            }

                            // Subject Badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = subjectColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = task.subject,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = subjectColor,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }

                            // Recurrence Badge
                            if (task.recurrence != RecurrenceType.NONE) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFEEF2FF),
                                    modifier = Modifier.testTag("task_recurrence_badge_${task.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Repeat,
                                            contentDescription = null,
                                            tint = Color(0xFF4F46E5),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = if (task.occurrenceIndex > 1) "${task.recurrence.label} (#${task.occurrenceIndex})" else task.recurrence.label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = Color(0xFF4F46E5)
                                        )
                                    }
                                }
                            }
                        }

                        // Status badge (Now / Completed / Past due)
                        if (isNow) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(
                                    text = "Happening Now!",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        } else if (task.isCompleted) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "Completed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        } else if (isPastDue) {
                            Text(
                                text = "Past Due",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Scheduled time and duration
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Clock time",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$dateFormatted at $timeFormatted",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HourglassBottom,
                                contentDescription = "Duration",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${task.durationMinutes}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Student Summary block
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 36.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Summary:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.summary.ifBlank { "No summary details added." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!isExpanded && task.summary.length > 90) {
                        Text(
                            text = "Tap to view full summary",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Bottom Actions Bar
            AnimatedVisibility(visible = isExpanded || isNow) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 36.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!task.isCompleted) {
                        IconButton(
                            onClick = onStartFocus,
                            modifier = Modifier.testTag("focus_button_${task.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleOutline,
                                contentDescription = "Start Focus Timer",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (task.recurrence != RecurrenceType.NONE && onGenerateNextOccurrence != null) {
                        IconButton(
                            onClick = onGenerateNextOccurrence,
                            modifier = Modifier.testTag("generate_next_button_${task.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Generate Next Recurring Instance",
                                tint = Color(0xFF4F46E5)
                            )
                        }
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_button_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Task",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_button_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Task",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
