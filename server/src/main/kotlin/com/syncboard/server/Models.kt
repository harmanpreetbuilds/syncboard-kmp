package com.syncboard.server

import kotlinx.serialization.Serializable

@Serializable
data class ServerTask(
    val id: String,
    val projectId: String,
    val title: String,
    val description: String,
    val status: String,
    val priority: String,
    val version: Long,
    val updatedAt: String
)

@Serializable
data class UpdateTaskStatusRequest(
    val status: String,
    val expectedVersion: Long
)

@Serializable
data class ApiError(
    val code: String,
    val message: String
)

@Serializable
data class ConflictResponse(
    val code: String,
    val message: String,
    val currentTask: ServerTask
)

@Serializable
data class HealthResponse(
    val status: String
)
