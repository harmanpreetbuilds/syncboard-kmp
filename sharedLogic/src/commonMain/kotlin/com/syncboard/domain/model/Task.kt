package com.syncboard.domain.model

data class Task(
    val id: String,
    val projectId: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: TaskPriority,
    val serverVersion: Long,
    val updatedAt: String,
    val syncStatus: SyncStatus
)

enum class TaskStatus {
    BACKLOG,
    IN_PROGRESS,
    DONE
}

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

enum class SyncStatus {
    SYNCED,
    PENDING,
    CONFLICT
}
