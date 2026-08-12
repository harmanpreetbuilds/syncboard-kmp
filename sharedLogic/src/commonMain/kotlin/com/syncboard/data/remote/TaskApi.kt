package com.syncboard.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class TaskApi(
    private val client: HttpClient,
    private val baseUrl: String
) {

    suspend fun getTasks(): List<TaskDto> {
        val response =
            client.get(
                "$baseUrl/tasks"
            )

        if (response.status != HttpStatusCode.OK) {
            error(
                "Unable to load tasks: ${response.status}"
            )
        }

        return response.body()
    }

    suspend fun updateStatus(
        taskId: String,
        status: String,
        expectedVersion: Long
    ): RemoteUpdateResult {

        val response =
            client.patch(
                "$baseUrl/tasks/$taskId"
            ) {
                contentType(
                    ContentType.Application.Json
                )

                setBody(
                    UpdateTaskStatusRequest(
                        status = status,
                        expectedVersion = expectedVersion
                    )
                )
            }

        return when (response.status) {
            HttpStatusCode.OK -> {
                RemoteUpdateResult.Success(
                    task = response.body()
                )
            }

            HttpStatusCode.Conflict -> {
                val conflict =
                    response.body<ConflictResponse>()

                RemoteUpdateResult.Conflict(
                    serverTask =
                        conflict.currentTask
                )
            }

            HttpStatusCode.NotFound -> {
                RemoteUpdateResult.Failed(
                    "Task no longer exists on the server."
                )
            }

            else -> {
                RemoteUpdateResult.Failed(
                    "Sync failed: ${response.status}"
                )
            }
        }
    }
}

sealed interface RemoteUpdateResult {

    data class Success(
        val task: TaskDto
    ) : RemoteUpdateResult

    data class Conflict(
        val serverTask: TaskDto
    ) : RemoteUpdateResult

    data class Failed(
        val message: String
    ) : RemoteUpdateResult
}
