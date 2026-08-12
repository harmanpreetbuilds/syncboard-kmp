package com.syncboard.server

import java.time.Instant

class TaskStore {

    private val tasks =
        linkedMapOf(
            "task-41" to ServerTask(
                id = "task-41",
                projectId = "engineering",
                title = "Handle stale writes during reconnect",
                description = "Protect local changes when the server contains a newer task version.",
                status = "IN_PROGRESS",
                priority = "HIGH",
                version = 4,
                updatedAt = "2026-08-12T10:42:00Z"
            ),

            "task-38" to ServerTask(
                id = "task-38",
                projectId = "engineering",
                title = "Persist pending mutations across restarts",
                description = "Keep offline writes durable until they have been acknowledged by the server.",
                status = "BACKLOG",
                priority = "MEDIUM",
                version = 2,
                updatedAt = "2026-08-12T10:35:00Z"
            ),

            "task-35" to ServerTask(
                id = "task-35",
                projectId = "engineering",
                title = "Add conflict metadata to task details",
                description = "Expose version conflicts without silently replacing local changes.",
                status = "DONE",
                priority = "MEDIUM",
                version = 7,
                updatedAt = "2026-08-12T10:18:00Z"
            ),

            "task-29" to ServerTask(
                id = "task-29",
                projectId = "engineering",
                title = "Expose sync health from shared state",
                description = "Surface pending and failed synchronization state to both clients.",
                status = "BACKLOG",
                priority = "LOW",
                version = 3,
                updatedAt = "2026-08-12T09:54:00Z"
            )
        )

    fun all(): List<ServerTask> {
        return synchronized(tasks) {
            tasks.values.toList()
        }
    }

    fun find(
        id: String
    ): ServerTask? {
        return synchronized(tasks) {
            tasks[id]
        }
    }

    fun updateStatus(
        id: String,
        request: UpdateTaskStatusRequest
    ): UpdateResult {
        return synchronized(tasks) {
            val current =
                tasks[id]
                    ?: return@synchronized UpdateResult.NotFound

            if (current.version != request.expectedVersion) {
                return@synchronized UpdateResult.Conflict(
                    current
                )
            }

            val updated =
                current.copy(
                    status = request.status,
                    version = current.version + 1,
                    updatedAt = Instant.now().toString()
                )

            tasks[id] = updated

            UpdateResult.Success(
                updated
            )
        }
    }
}

sealed interface UpdateResult {

    data class Success(
        val task: ServerTask
    ) : UpdateResult

    data class Conflict(
        val currentTask: ServerTask
    ) : UpdateResult

    data object NotFound : UpdateResult
}
