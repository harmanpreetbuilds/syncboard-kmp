package com.syncboard.presentation.tasks

import com.syncboard.domain.model.Task

data class TasksUiState(
    val projectName: String = "Engineering",
    val tasks: List<Task> = emptyList(),
    val visibleTasks: List<Task> = emptyList(),
    val filter: TaskFilter = TaskFilter.ALL,
    val isRefreshing: Boolean = false,
    val pendingChanges: Int = 0,
    val conflictCount: Int = 0,
    val errorMessage: String? = null
)

enum class TaskFilter {
    ALL,
    BACKLOG,
    IN_PROGRESS,
    DONE
}
