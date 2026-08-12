package com.syncboard.app.ui.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncboard.domain.model.SyncStatus
import com.syncboard.domain.model.Task
import com.syncboard.domain.model.TaskPriority
import com.syncboard.domain.model.TaskStatus
import com.syncboard.presentation.tasks.TaskFilter
import com.syncboard.presentation.tasks.TasksAction
import com.syncboard.presentation.tasks.TasksStateHolder
import com.syncboard.presentation.tasks.TasksUiState

@Composable
fun WorkspaceScreen(
    stateHolder: TasksStateHolder
) {
    val state by stateHolder.state.collectAsState()

    Scaffold(
        containerColor =
            MaterialTheme.colorScheme.background
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement =
                Arrangement.spacedBy(18.dp)
        ) {
            item {
                Header()
            }

            item {
                Column(
                    modifier =
                        Modifier.padding(horizontal = 20.dp)
                ) {
                    OverviewCard(state)
                }
            }

            item {
                FilterBar(
                    selected = state.filter,
                    onSelected = {
                        stateHolder.onAction(
                            TasksAction.FilterChanged(it)
                        )
                    }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        text = state.projectName,
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight =
                            FontWeight.SemiBold
                    )

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text =
                            "${state.visibleTasks.size} issues",
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant,
                        style =
                            MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (state.visibleTasks.isEmpty()) {
                item {
                    EmptyState()
                }
            } else {
                items(
                    items = state.visibleTasks,
                    key = { it.id }
                ) { task ->
                    TaskCard(
                        task = task,
                        onStatusChange = { newStatus ->
                            stateHolder.onAction(
                                TasksAction.StatusChanged(
                                    taskId = task.id,
                                    status = newStatus
                                )
                            )
                        },
                        onUseServerVersion = {
                            stateHolder.onAction(
                                TasksAction.UseServerVersion(
                                    task.id
                                )
                            )
                        },
                        onKeepLocalChange = {
                            stateHolder.onAction(
                                TasksAction.KeepLocalChange(
                                    task.id
                                )
                            )
                        },
                        modifier =
                            Modifier.padding(
                                horizontal = 20.dp
                            )
                    )
                }
            }

            item {
                Spacer(
                    modifier = Modifier.height(18.dp)
                )
            }
        }
    }
}

@Composable
private fun Header() {
    Surface {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 18.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SyncBoard",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Workspace",
                        style =
                            MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme
                                .primaryContainer
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text = "HK",
                        fontWeight =
                            FontWeight.SemiBold,
                        color =
                            MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider()
        }
    }
}

@Composable
private fun OverviewCard(
    state: TasksUiState
) {
    val openCount =
        state.tasks.count {
            it.status != TaskStatus.DONE
        }

    val progressCount =
        state.tasks.count {
            it.status == TaskStatus.IN_PROGRESS
        }

    val doneCount =
        state.tasks.count {
            it.status == TaskStatus.DONE
        }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111827)
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = state.projectName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight =
                            FontWeight.SemiBold
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = "Mobile platform",
                        color = Color(0xFF98A2B3),
                        style =
                            MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                SyncBadge(state)
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(28.dp)
            ) {
                Metric(
                    value = openCount.toString(),
                    label = "Open"
                )

                Metric(
                    value = progressCount.toString(),
                    label = "In progress"
                )

                Metric(
                    value = doneCount.toString(),
                    label = "Completed"
                )
            }
        }
    }
}

@Composable
private fun SyncBadge(
    state: TasksUiState
) {
    val label =
        when {
            state.conflictCount > 0 ->
                "${state.conflictCount} conflict"

            state.isRefreshing ->
                "Syncing"

            state.pendingChanges > 0 ->
                "${state.pendingChanges} pending"

            else ->
                "Synced"
        }

    val indicatorColor =
        when {
            state.conflictCount > 0 ->
                Color(0xFFF04438)

            state.pendingChanges > 0 ->
                Color(0xFFFDB022)

            else ->
                Color(0xFF32D583)
        }

    Surface(
        color = Color(0xFF1F2937),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 7.dp
            ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(
                        indicatorColor
                    )
            )

            Spacer(
                modifier = Modifier.width(7.dp)
            )

            Text(
                text = label,
                color = Color(0xFFD0D5DD),
                style =
                    MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun Metric(
    value: String,
    label: String
) {
    Column {
        Text(
            text = value,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            color = Color(0xFF98A2B3),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun FilterBar(
    selected: TaskFilter,
    onSelected: (TaskFilter) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding =
            androidx.compose.foundation.layout.PaddingValues(
                horizontal = 20.dp
            ),
        horizontalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {
        items(TaskFilter.entries) { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = {
                    onSelected(filter)
                },
                label = {
                    Text(filter.label())
                }
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onStatusChange: (TaskStatus) -> Unit,
    onUseServerVersion: () -> Unit,
    onKeepLocalChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text =
                        "SYNC-${task.id.substringAfterLast("-")}",
                    style =
                        MaterialTheme.typography.labelMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                PriorityPill(task.priority)
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = task.title,
                style =
                    MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 22.sp
            )

            if (task.syncStatus == SyncStatus.CONFLICT) {
                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                ConflictNotice(
                    onUseServerVersion = onUseServerVersion,
                    onKeepLocalChange = onKeepLocalChange
                )
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                StatusMenu(
                    status = task.status,
                    onStatusSelected = onStatusChange
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme
                                .surfaceVariant
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text = "HK",
                        style =
                            MaterialTheme.typography.labelSmall,
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ConflictNotice(
    onUseServerVersion: () -> Unit,
    onKeepLocalChange: () -> Unit
) {
    Surface(
        color = Color(0xFFFEF3F2),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Sync conflict",
                color = Color(0xFFB42318),
                style =
                    MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = "This issue changed on another device.",
                color = Color(0xFFB42318),
                style =
                    MaterialTheme.typography.bodySmall
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Row {
                TextButton(
                    onClick = onUseServerVersion
                ) {
                    Text("Use server")
                }

                TextButton(
                    onClick = onKeepLocalChange
                ) {
                    Text("Keep mine")
                }
            }
        }
    }
}

@Composable
private fun PriorityPill(
    priority: TaskPriority
) {
    val background =
        when (priority) {
            TaskPriority.URGENT,
            TaskPriority.HIGH ->
                Color(0xFFFEF3F2)

            TaskPriority.MEDIUM ->
                Color(0xFFFFFAEB)

            TaskPriority.LOW ->
                Color(0xFFF2F4F7)
        }

    val foreground =
        when (priority) {
            TaskPriority.URGENT,
            TaskPriority.HIGH ->
                Color(0xFFB42318)

            TaskPriority.MEDIUM ->
                Color(0xFFB54708)

            TaskPriority.LOW ->
                Color(0xFF475467)
        }

    Surface(
        color = background,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = priority.label(),
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 5.dp
            ),
            color = foreground,
            style =
                MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun StatusMenu(
    status: TaskStatus,
    onStatusSelected: (TaskStatus) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    val dotColor =
        when (status) {
            TaskStatus.DONE ->
                Color(0xFF12B76A)

            TaskStatus.IN_PROGRESS ->
                Color(0xFF6172F3)

            TaskStatus.BACKLOG ->
                Color(0xFF98A2B3)
        }

    Box {
        Surface(
            modifier = Modifier.clickable {
                expanded = true
            },
            color =
                MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 11.dp,
                    vertical = 7.dp
                ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )

                Spacer(
                    modifier = Modifier.width(7.dp)
                )

                Text(
                    text = status.label(),
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                    style =
                        MaterialTheme.typography.bodySmall,
                    fontWeight =
                        FontWeight.Medium
                )

                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                Text(
                    text = "⌄",
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                    style =
                        MaterialTheme.typography.labelMedium
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            TaskStatus.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(option.label())
                    },
                    onClick = {
                        expanded = false

                        if (option != status) {
                            onStatusSelected(option)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text = "No issues here",
            fontWeight = FontWeight.SemiBold
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = "Try another status filter.",
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant,
            style =
                MaterialTheme.typography.bodySmall
        )
    }
}

private fun TaskFilter.label(): String {
    return when (this) {
        TaskFilter.ALL -> "All"
        TaskFilter.BACKLOG -> "Backlog"
        TaskFilter.IN_PROGRESS -> "In progress"
        TaskFilter.DONE -> "Done"
    }
}

private fun TaskStatus.label(): String {
    return when (this) {
        TaskStatus.BACKLOG -> "Backlog"
        TaskStatus.IN_PROGRESS -> "In progress"
        TaskStatus.DONE -> "Done"
    }
}

private fun TaskPriority.label(): String {
    return when (this) {
        TaskPriority.LOW -> "Low"
        TaskPriority.MEDIUM -> "Medium"
        TaskPriority.HIGH -> "High"
        TaskPriority.URGENT -> "Urgent"
    }
}


