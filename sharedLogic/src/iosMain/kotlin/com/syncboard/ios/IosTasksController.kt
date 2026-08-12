package com.syncboard.ios

import com.syncboard.data.local.IosDatabaseDriverFactory
import com.syncboard.data.remote.ApiConfig
import com.syncboard.data.remote.TaskApi
import com.syncboard.data.remote.createHttpClient
import com.syncboard.data.repository.TaskRepositoryImpl
import com.syncboard.database.SyncBoardDatabase
import com.syncboard.domain.model.TaskStatus
import com.syncboard.presentation.tasks.TaskFilter
import com.syncboard.presentation.tasks.TasksAction
import com.syncboard.presentation.tasks.TasksStateHolder
import com.syncboard.presentation.tasks.TasksUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class IosTasksController {

    private val httpClient =
        createHttpClient()

    private val database =
        SyncBoardDatabase(
            IosDatabaseDriverFactory()
                .createDriver()
        )

    private val repository =
        TaskRepositoryImpl(
            database = database,
            api = TaskApi(
                client = httpClient,
                baseUrl = ApiConfig.baseUrl
            )
        )

    private val stateHolder =
        TasksStateHolder(
            repository = repository
        )

    private val scope =
        CoroutineScope(
            SupervisorJob() +
                Dispatchers.Main
        )

    private var observationJob: Job? =
        null

    fun currentState(): TasksUiState {
        return stateHolder.state.value
    }

    fun startObserving(
        onState: (TasksUiState) -> Unit
    ) {
        observationJob?.cancel()

        observationJob =
            scope.launch {
                stateHolder.state.collect {
                    onState(it)
                }
            }
    }

    fun stopObserving() {
        observationJob?.cancel()
        observationJob = null
    }

    fun refresh() {
        stateHolder.onAction(
            TasksAction.Refresh
        )
    }

    fun selectFilter(
        filter: TaskFilter
    ) {
        stateHolder.onAction(
            TasksAction.FilterChanged(
                filter
            )
        )
    }

    fun updateStatus(
        taskId: String,
        status: TaskStatus
    ) {
        stateHolder.onAction(
            TasksAction.StatusChanged(
                taskId = taskId,
                status = status
            )
        )
    }

    fun retrySync() {
        stateHolder.onAction(
            TasksAction.RetrySync
        )
    }

    fun useServerVersion(
        taskId: String
    ) {
        stateHolder.onAction(
            TasksAction.UseServerVersion(
                taskId
            )
        )
    }

    fun keepLocalChange(
        taskId: String
    ) {
        stateHolder.onAction(
            TasksAction.KeepLocalChange(
                taskId
            )
        )
    }

    fun close() {
        stopObserving()
        stateHolder.close()
        httpClient.close()
        scope.cancel()
    }
}
