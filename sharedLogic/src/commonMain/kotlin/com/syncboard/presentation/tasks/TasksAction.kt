package com.syncboard.presentation.tasks

import com.syncboard.domain.model.TaskStatus

sealed interface TasksAction {

    data object Refresh : TasksAction

    data object RetrySync : TasksAction

    data class FilterChanged(
        val filter: TaskFilter
    ) : TasksAction

    data class StatusChanged(
        val taskId: String,
        val status: TaskStatus
    ) : TasksAction

    data class UseServerVersion(
        val taskId: String
    ) : TasksAction

    data class KeepLocalChange(
        val taskId: String
    ) : TasksAction
}
