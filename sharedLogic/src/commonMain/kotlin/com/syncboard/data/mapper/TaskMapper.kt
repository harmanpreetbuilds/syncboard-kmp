package com.syncboard.data.mapper

import com.syncboard.database.Task_entity
import com.syncboard.domain.model.SyncStatus
import com.syncboard.domain.model.Task
import com.syncboard.domain.model.TaskPriority
import com.syncboard.domain.model.TaskStatus

fun Task_entity.toDomain(): Task {
    return Task(
        id = id,
        projectId = project_id,
        title = title,
        description = description,
        status = status.toTaskStatus(),
        priority = priority.toTaskPriority(),
        serverVersion = server_version,
        updatedAt = updated_at,
        syncStatus = sync_status.toSyncStatus()
    )
}

private fun String.toTaskStatus(): TaskStatus {
    return TaskStatus.entries.firstOrNull {
        it.name == this
    } ?: TaskStatus.BACKLOG
}

private fun String.toTaskPriority(): TaskPriority {
    return TaskPriority.entries.firstOrNull {
        it.name == this
    } ?: TaskPriority.MEDIUM
}

private fun String.toSyncStatus(): SyncStatus {
    return SyncStatus.entries.firstOrNull {
        it.name == this
    } ?: SyncStatus.SYNCED
}
