package com.syncboard.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.syncboard.data.mapper.toDomain
import com.syncboard.data.remote.RemoteUpdateResult
import com.syncboard.data.remote.TaskApi
import com.syncboard.data.remote.TaskDto
import com.syncboard.database.SyncBoardDatabase
import com.syncboard.domain.model.SyncStatus
import com.syncboard.domain.model.Task
import com.syncboard.domain.model.TaskStatus
import com.syncboard.domain.repository.SyncState
import com.syncboard.domain.repository.TaskRepository
import kotlin.random.Random
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TaskRepositoryImpl(
    private val database: SyncBoardDatabase,
    private val api: TaskApi
) : TaskRepository {

    private val taskQueries =
        database.taskQueries

    private val mutationQueries =
        database.pendingMutationQueries

    private val _syncState =
        MutableStateFlow<SyncState>(
            SyncState.Idle
        )

    override val syncState:
        StateFlow<SyncState> =
        _syncState.asStateFlow()

    override fun observeTasks(
        projectId: String
    ): Flow<List<Task>> {
        return taskQueries
            .selectByProject(projectId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows ->
                rows.map {
                    it.toDomain()
                }
            }
    }

    override suspend fun refresh(
        projectId: String
    ) {
        _syncState.value =
            SyncState.Syncing

        try {
            val remoteTasks =
                api.getTasks()

            withContext(
                Dispatchers.Default
            ) {
                database.transaction {
                    remoteTasks
                        .filter {
                            it.projectId ==
                                projectId
                        }
                        .forEach { remote ->

                            val local =
                                taskQueries
                                    .selectById(
                                        remote.id
                                    )
                                    .executeAsOneOrNull()

                            val protectedLocal =
                                local?.sync_status ==
                                    SyncStatus.PENDING.name ||
                                local?.sync_status ==
                                    SyncStatus.CONFLICT.name

                            if (!protectedLocal) {
                                saveRemoteTask(
                                    remote,
                                    SyncStatus.SYNCED
                                )
                            }
                        }
                }
            }

            _syncState.value =
                SyncState.Idle

        } catch (error: Throwable) {
            _syncState.value =
                SyncState.Failed(
                    error.message
                        ?: "Unable to refresh tasks"
                )
        }
    }

    override suspend fun updateStatus(
        taskId: String,
        status: TaskStatus
    ) {
        withContext(
            Dispatchers.Default
        ) {
            val task =
                taskQueries
                    .selectById(taskId)
                    .executeAsOneOrNull()
                    ?: return@withContext

            val updatedAt =
                Clock.System
                    .now()
                    .toString()

            database.transaction {

                taskQueries.updateStatus(
                    status.name,
                    SyncStatus.PENDING.name,
                    updatedAt,
                    taskId
                )

                mutationQueries
                    .deleteForTask(
                        taskId
                    )

                mutationQueries
                    .insertMutation(
                        id =
                            "$taskId-${
                                Random
                                    .nextLong()
                                    .toString(16)
                            }",
                        task_id =
                            taskId,
                        operation =
                            "UPDATE_STATUS",
                        payload =
                            status.name,
                        base_version =
                            task.server_version,
                        created_at =
                            updatedAt
                    )
            }
        }
    }

    override suspend fun syncPendingChanges() {
        _syncState.value =
            SyncState.Syncing

        try {
            val mutations =
                withContext(
                    Dispatchers.Default
                ) {
                    mutationQueries
                        .selectAll()
                        .executeAsList()
                }

            for (mutation in mutations) {

                when (
                    val result =
                        api.updateStatus(
                            taskId =
                                mutation.task_id,
                            status =
                                mutation.payload,
                            expectedVersion =
                                mutation.base_version
                        )
                ) {

                    is RemoteUpdateResult.Success -> {

                        withContext(
                            Dispatchers.Default
                        ) {
                            database.transaction {

                                saveRemoteTask(
                                    task =
                                        result.task,
                                    syncStatus =
                                        SyncStatus.SYNCED
                                )

                                mutationQueries
                                    .deleteById(
                                        mutation.id
                                    )
                            }
                        }
                    }

                    is RemoteUpdateResult.Conflict -> {

                        withContext(
                            Dispatchers.Default
                        ) {
                            database.transaction {

                                taskQueries
                                    .updateSyncStatus(
                                        SyncStatus
                                            .CONFLICT
                                            .name,
                                        mutation.task_id
                                    )

                                mutationQueries
                                    .incrementAttempt(
                                        mutation.id
                                    )
                            }
                        }
                    }

                    is RemoteUpdateResult.Failed -> {

                        withContext(
                            Dispatchers.Default
                        ) {
                            mutationQueries
                                .incrementAttempt(
                                    mutation.id
                                )
                        }

                        throw IllegalStateException(
                            result.message
                        )
                    }
                }
            }

            _syncState.value =
                SyncState.Idle

        } catch (error: Throwable) {
            _syncState.value =
                SyncState.Failed(
                    error.message
                        ?: "Unable to synchronize changes"
                )
        }
    }

    override suspend fun useServerVersion(
        taskId: String
    ) {
        _syncState.value = SyncState.Syncing

        try {
            val serverTask =
                api.getTasks()
                    .firstOrNull {
                        it.id == taskId
                    }
                    ?: error("Task no longer exists on the server.")

            withContext(Dispatchers.Default) {
                database.transaction {
                    saveRemoteTask(
                        task = serverTask,
                        syncStatus = SyncStatus.SYNCED
                    )

                    mutationQueries.deleteForTask(
                        taskId
                    )
                }
            }

            _syncState.value = SyncState.Idle

        } catch (error: Throwable) {
            _syncState.value =
                SyncState.Failed(
                    error.message
                        ?: "Unable to resolve conflict"
                )
        }
    }

    override suspend fun keepLocalChange(
        taskId: String
    ) {
        _syncState.value = SyncState.Syncing

        try {
            val serverTask =
                api.getTasks()
                    .firstOrNull {
                        it.id == taskId
                    }
                    ?: error("Task no longer exists on the server.")

            withContext(Dispatchers.Default) {
                database.transaction {
                    mutationQueries.rebaseForTask(
                        base_version = serverTask.version,
                        task_id = taskId
                    )

                    taskQueries.updateSyncStatus(
                        SyncStatus.PENDING.name,
                        taskId
                    )
                }
            }

            syncPendingChanges()

        } catch (error: Throwable) {
            _syncState.value =
                SyncState.Failed(
                    error.message
                        ?: "Unable to retry local change"
                )
        }
    }

    private fun saveRemoteTask(
        task: TaskDto,
        syncStatus: SyncStatus
    ) {
        taskQueries.upsert(
            task.id,
            task.projectId,
            task.title,
            task.description,
            task.status,
            task.priority,
            task.version,
            task.updatedAt,
            syncStatus.name
        )
    }
}
