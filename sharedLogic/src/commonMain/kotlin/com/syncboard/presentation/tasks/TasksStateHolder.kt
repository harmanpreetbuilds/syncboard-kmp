package com.syncboard.presentation.tasks

import com.syncboard.domain.model.SyncStatus
import com.syncboard.domain.model.TaskStatus
import com.syncboard.domain.repository.SyncState
import com.syncboard.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TasksStateHolder(
    private val repository: TaskRepository
) {

    private val projectId = "engineering"

    private val scope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.Main
        )

    private val selectedFilter =
        MutableStateFlow(TaskFilter.ALL)

    private val _effects =
        MutableSharedFlow<TasksEffect>()

    val effects: SharedFlow<TasksEffect> =
        _effects.asSharedFlow()

    val state: StateFlow<TasksUiState> =
        combine(
            repository.observeTasks(projectId),
            repository.syncState,
            selectedFilter
        ) { tasks, syncState, filter ->

            val visibleTasks =
                when (filter) {
                    TaskFilter.ALL -> tasks

                    TaskFilter.BACKLOG ->
                        tasks.filter {
                            it.status == TaskStatus.BACKLOG
                        }

                    TaskFilter.IN_PROGRESS ->
                        tasks.filter {
                            it.status == TaskStatus.IN_PROGRESS
                        }

                    TaskFilter.DONE ->
                        tasks.filter {
                            it.status == TaskStatus.DONE
                        }
                }

            TasksUiState(
                projectName = "Engineering",
                tasks = tasks,
                visibleTasks = visibleTasks,
                filter = filter,
                isRefreshing =
                    syncState is SyncState.Syncing,
                pendingChanges =
                    tasks.count {
                        it.syncStatus == SyncStatus.PENDING
                    },
                conflictCount =
                    tasks.count {
                        it.syncStatus == SyncStatus.CONFLICT
                    },
                errorMessage =
                    (syncState as? SyncState.Failed)
                        ?.message
            )
        }
        .stateIn(
            scope = scope,
            started =
                SharingStarted.WhileSubscribed(5_000),
            initialValue = TasksUiState()
        )

    init {
        scope.launch {
            repository.refresh(projectId)
            repository.syncPendingChanges()
        }
    }

    fun onAction(
        action: TasksAction
    ) {
        when (action) {
            TasksAction.Refresh -> {
                scope.launch {
                    repository.refresh(projectId)
                }
            }

            TasksAction.RetrySync -> {
                scope.launch {
                    repository.syncPendingChanges()
                }
            }

            is TasksAction.FilterChanged -> {
                selectedFilter.value = action.filter
            }

            is TasksAction.StatusChanged -> {
                scope.launch {
                    repository.updateStatus(
                        action.taskId,
                        action.status
                    )

                    _effects.emit(
                        TasksEffect.ShowMessage(
                            "Task updated"
                        )
                    )
                }
            }

            is TasksAction.UseServerVersion -> {
                scope.launch {
                    repository.useServerVersion(
                        action.taskId
                    )
                }
            }

            is TasksAction.KeepLocalChange -> {
                scope.launch {
                    repository.keepLocalChange(
                        action.taskId
                    )
                }
            }
        }
    }

    fun close() {
        scope.cancel()
    }
}
