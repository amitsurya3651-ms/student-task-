package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentProfile

val AVATAR_EMOJIS = listOf("🎓", "🔬", "📚", "💻", "🎨", "🚀", "⚖️", "🩺", "📐", "🧠")
val AVATAR_COLORS = listOf(
    0xFF4F46E5L, // Indigo
    0xFF059669L, // Emerald
    0xFFD97706L, // Amber
    0xFFDC2626L, // Crimson
    0xFF0284C7L, // Cyan
    0xFF7C3AEDL, // Purple
    0xFFDB2777L  // Pink
)

@Composable
fun EditProfileDialog(
    profile: StudentProfile,
    onDismiss: () -> Unit,
    onSave: (StudentProfile) -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var gradeOrMajor by remember { mutableStateOf(profile.gradeOrMajor) }
    var weeklyGoalHours by remember { mutableIntStateOf(profile.weeklyGoalHours) }
    var selectedEmoji by remember { mutableStateOf(profile.avatarEmoji) }
    var selectedColor by remember { mutableLongStateOf(profile.avatarColorHex) }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Student Profile",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.testTag("edit_profile_dialog_title")
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Avatar preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(selectedColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = selectedEmoji, fontSize = 34.sp)
                    }
                }

                // Choose Emoji
                Text(
                    text = "Select Avatar Icon",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AVATAR_EMOJIS.forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selectedEmoji == emoji) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (selectedEmoji == emoji) 2.dp else 0.dp,
                                    color = if (selectedEmoji == emoji) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 20.sp)
                        }
                    }
                }

                // Choose Avatar Color
                Text(
                    text = "Theme Color",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AVATAR_COLORS.forEach { colorHex ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .border(
                                    width = if (selectedColor == colorHex) 3.dp else 1.dp,
                                    color = if (selectedColor == colorHex) MaterialTheme.colorScheme.onSurface else Color.White,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorHex }
                        )
                    }
                }

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError && it.isNotBlank()) nameError = false
                    },
                    label = { Text("Student Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = nameError,
                    supportingText = { if (nameError) Text("Name cannot be empty") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Grade / Major field
                OutlinedTextField(
                    value = gradeOrMajor,
                    onValueChange = { gradeOrMajor = it },
                    label = { Text("Grade / Major / Specialization") },
                    placeholder = { Text("e.g., Computer Science, Pre-Med, Grade 12") },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_major_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Weekly Goal Hours
                Text(
                    text = "Target Weekly Study Hours: $weeklyGoalHours hrs",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 15, 20, 25, 30).forEach { hours ->
                        FilterChip(
                            selected = weeklyGoalHours == hours,
                            onClick = { weeklyGoalHours = hours },
                            label = { Text("${hours}h") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Flag,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
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
                        onSave(
                            profile.copy(
                                name = name.trim(),
                                gradeOrMajor = gradeOrMajor.trim().ifBlank { "General Studies" },
                                weeklyGoalHours = weeklyGoalHours,
                                avatarEmoji = selectedEmoji,
                                avatarColorHex = selectedColor
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("save_profile_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Profile")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_profile_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun CreateProfileDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, gradeOrMajor: String, weeklyGoalHours: Int, avatarEmoji: String, avatarColor: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var gradeOrMajor by remember { mutableStateOf("") }
    var weeklyGoalHours by remember { mutableIntStateOf(15) }
    var selectedEmoji by remember { mutableStateOf(AVATAR_EMOJIS[0]) }
    var selectedColor by remember { mutableLongStateOf(AVATAR_COLORS[0]) }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Student Profile",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.testTag("create_profile_dialog_title")
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Avatar preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(selectedColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = selectedEmoji, fontSize = 34.sp)
                    }
                }

                // Choose Emoji
                Text(
                    text = "Select Avatar Icon",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AVATAR_EMOJIS.forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selectedEmoji == emoji) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (selectedEmoji == emoji) 2.dp else 0.dp,
                                    color = if (selectedEmoji == emoji) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 20.sp)
                        }
                    }
                }

                // Choose Avatar Color
                Text(
                    text = "Theme Color",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AVATAR_COLORS.forEach { colorHex ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .border(
                                    width = if (selectedColor == colorHex) 3.dp else 1.dp,
                                    color = if (selectedColor == colorHex) MaterialTheme.colorScheme.onSurface else Color.White,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorHex }
                        )
                    }
                }

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError && it.isNotBlank()) nameError = false
                    },
                    label = { Text("Student Name *") },
                    placeholder = { Text("e.g. Maya Lin, Jordan Patel") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = nameError,
                    supportingText = { if (nameError) Text("Name cannot be empty") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_profile_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Grade / Major field
                OutlinedTextField(
                    value = gradeOrMajor,
                    onValueChange = { gradeOrMajor = it },
                    label = { Text("Grade / Major / Subject") },
                    placeholder = { Text("e.g. Biomedical Engineering, High School") },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_profile_major_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Weekly Goal Hours
                Text(
                    text = "Target Weekly Study Hours: $weeklyGoalHours hrs",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 15, 20, 25, 30).forEach { hours ->
                        FilterChip(
                            selected = weeklyGoalHours == hours,
                            onClick = { weeklyGoalHours = hours },
                            label = { Text("${hours}h") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Flag,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
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
                        onCreate(
                            name.trim(),
                            gradeOrMajor.trim().ifBlank { "General Studies" },
                            weeklyGoalHours,
                            selectedEmoji,
                            selectedColor
                        )
                    }
                },
                modifier = Modifier.testTag("confirm_create_profile_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Create Student Profile")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_create_profile_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
