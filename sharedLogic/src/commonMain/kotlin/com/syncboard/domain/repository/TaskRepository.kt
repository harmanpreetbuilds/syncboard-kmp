package com.syncboard.domain.repository

import com.syncboard.domain.model.Task
import com.syncboard.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TaskRepository {

    fun observeTasks(
        projectId: String
    ): Flow<List<Task>>

    val syncState: StateFlow<SyncState>

    suspend fun refresh(
        projectId: String
    )

    suspend fun updateStatus(
        taskId: String,
        status: TaskStatus
    )

    suspend fun syncPendingChanges()

    suspend fun useServerVersion(
        taskId: String
    )

    suspend fun keepLocalChange(
        taskId: String
    )
}

sealed interface SyncState {

    data object Idle : SyncState

    data object Syncing : SyncState

    data class Failed(
        val message: String
    ) : SyncState
}
