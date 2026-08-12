package com.syncboard.data.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.syncboard.data.remote.ConflictResponse
import com.syncboard.data.remote.TaskApi
import com.syncboard.data.remote.TaskDto
import com.syncboard.data.remote.UpdateTaskStatusRequest
import com.syncboard.database.SyncBoardDatabase
import com.syncboard.domain.model.SyncStatus
import com.syncboard.domain.model.TaskStatus
import com.syncboard.domain.repository.SyncState
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TaskRepositoryImplTest {

    @Test
    fun refreshPersistsRemoteTask() =
        withRepository { repository, database, _ ->

            repository.refresh("engineering")

            val task =
                database.taskQueries
                    .selectById("task-29")
                    .executeAsOne()

            assertEquals(
                "BACKLOG",
                task.status
            )

            assertEquals(
                "SYNCED",
                task.sync_status
            )

            assertEquals(
                3L,
                task.server_version
            )
        }

    @Test
    fun offlineUpdateCreatesMutation() =
        withRepository { repository, database, _ ->

            repository.refresh("engineering")

            repository.updateStatus(
                taskId = "task-29",
                status = TaskStatus.IN_PROGRESS
            )

            val task =
                database.taskQueries
                    .selectById("task-29")
                    .executeAsOne()

            assertEquals(
                "IN_PROGRESS",
                task.status
            )

            assertEquals(
                "PENDING",
                task.sync_status
            )

            val mutation =
                database.pendingMutationQueries
                    .selectByTask("task-29")
                    .executeAsOne()

            assertEquals(
                "UPDATE_STATUS",
                mutation.operation
            )

            assertEquals(
                "IN_PROGRESS",
                mutation.payload
            )

            assertEquals(
                3L,
                mutation.base_version
            )

            assertEquals(
                0L,
                mutation.attempt_count
            )
        }

    @Test
    fun pendingTaskSurvivesRefresh() =
        withRepository { repository, database, server ->

            repository.refresh("engineering")

            repository.updateStatus(
                "task-29",
                TaskStatus.IN_PROGRESS
            )

            server.task =
                server.task.copy(
                    status = "DONE",
                    version = 4,
                    updatedAt =
                        "2026-08-12T12:00:00Z"
                )

            repository.refresh("engineering")

            val task =
                database.taskQueries
                    .selectById("task-29")
                    .executeAsOne()

            assertEquals(
                "IN_PROGRESS",
                task.status
            )

            assertEquals(
                "PENDING",
                task.sync_status
            )

            assertEquals(
                3L,
                task.server_version
            )
        }

    @Test
    fun successfulSyncClearsOutboxAndUpdatesVersion() =
        withRepository { repository, database, server ->

            repository.refresh("engineering")

            repository.updateStatus(
                "task-29",
                TaskStatus.IN_PROGRESS
            )

            repository.syncPendingChanges()

            val task =
                database.taskQueries
                    .selectById("task-29")
                    .executeAsOne()

            assertEquals(
                "IN_PROGRESS",
                task.status
            )

            assertEquals(
                "SYNCED",
                task.sync_status
            )

            assertEquals(
                4L,
                task.server_version
            )

            assertEquals(
                "IN_PROGRESS",
                server.task.status
            )

            assertEquals(
                4L,
                server.task.version
            )

            assertTrue(
                database.pendingMutationQueries
                    .selectAll()
                    .executeAsList()
                    .isEmpty()
            )
        }

    @Test
    fun conflictPreservesLocalChangeAndMarksConflict() =
        withRepository { repository, database, server ->

            repository.refresh("engineering")

            repository.updateStatus(
                "task-29",
                TaskStatus.IN_PROGRESS
            )

            server.task =
                server.task.copy(
                    status = "DONE",
                    version = 4,
                    updatedAt =
                        "2026-08-12T12:10:00Z"
                )

            repository.syncPendingChanges()

            val task =
                database.taskQueries
                    .selectById("task-29")
                    .executeAsOne()

            assertEquals(
                "IN_PROGRESS",
                task.status
            )

            assertEquals(
                "CONFLICT",
                task.sync_status
            )

            assertEquals(
                3L,
                task.server_version
            )

            val mutation =
                database.pendingMutationQueries
                    .selectByTask("task-29")
                    .executeAsOne()

            assertEquals(
                1L,
                mutation.attempt_count
            )
        }

    @Test
    fun useServerVersionResolvesConflict() =
        withRepository { repository, database, server ->

            repository.refresh("engineering")

            repository.updateStatus(
                "task-29",
                TaskStatus.IN_PROGRESS
            )

            server.task =
                server.task.copy(
                    status = "DONE",
                    version = 4,
                    updatedAt =
                        "2026-08-12T12:20:00Z"
                )

            repository.syncPendingChanges()

            repository.useServerVersion(
                "task-29"
            )

            val task =
                database.taskQueries
                    .selectById("task-29")
                    .executeAsOne()

            assertEquals(
                "DONE",
                task.status
            )

            assertEquals(
                "SYNCED",
                task.sync_status
            )

            assertEquals(
                4L,
                task.server_version
            )

            assertTrue(
                database.pendingMutationQueries
                    .selectAll()
                    .executeAsList()
                    .isEmpty()
            )
        }

    @Test
    fun keepMineRebasesAndResyncs() =
        withRepository { repository, database, server ->

            repository.refresh("engineering")

            repository.updateStatus(
                "task-29",
                TaskStatus.IN_PROGRESS
            )

            server.task =
                server.task.copy(
                    status = "DONE",
                    version = 4,
                    updatedAt =
                        "2026-08-12T12:30:00Z"
                )

            repository.syncPendingChanges()

            repository.keepLocalChange(
                "task-29"
            )

            val task =
                database.taskQueries
                    .selectById("task-29")
                    .executeAsOne()

            assertEquals(
                "IN_PROGRESS",
                task.status
            )

            assertEquals(
                "SYNCED",
                task.sync_status
            )

            assertEquals(
                5L,
                task.server_version
            )

            assertEquals(
                "IN_PROGRESS",
                server.task.status
            )

            assertEquals(
                5L,
                server.task.version
            )

            assertEquals(
                listOf(3L, 4L),
                server.patchExpectedVersions
            )

            assertTrue(
                database.pendingMutationQueries
                    .selectAll()
                    .executeAsList()
                    .isEmpty()
            )
        }

    @Test
    fun failedSyncKeepsMutationAndIncrementsAttempt() =
        withRepository { repository, database, server ->

            repository.refresh("engineering")

            repository.updateStatus(
                "task-29",
                TaskStatus.IN_PROGRESS
            )

            server.failPatch = true

            repository.syncPendingChanges()

            val task =
                database.taskQueries
                    .selectById("task-29")
                    .executeAsOne()

            assertEquals(
                "IN_PROGRESS",
                task.status
            )

            assertEquals(
                "PENDING",
                task.sync_status
            )

            val mutation =
                database.pendingMutationQueries
                    .selectByTask("task-29")
                    .executeAsOne()

            assertEquals(
                1L,
                mutation.attempt_count
            )

            assertIs<SyncState.Failed>(
                repository.syncState.value
            )
        }

    private fun withRepository(
        block: suspend (
            TaskRepositoryImpl,
            SyncBoardDatabase,
            FakeTaskServer
        ) -> Unit
    ) = runBlocking {

        val driver =
            createDriver()

        val database =
            SyncBoardDatabase(
                driver
            )

        val server =
            FakeTaskServer()

        val repository =
            TaskRepositoryImpl(
                database = database,
                api = server.api
            )

        try {
            block(
                repository,
                database,
                server
            )
        } finally {
            server.close()
            driver.close()
        }
    }

    private fun createDriver(): SqlDriver {
        return JdbcSqliteDriver(
            JdbcSqliteDriver.IN_MEMORY
        ).also { driver ->
            SyncBoardDatabase.Schema
                .create(driver)
        }
    }

    private class FakeTaskServer {

        private val jsonCodec =
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            }

        var task =
            TaskDto(
                id = "task-29",
                projectId = "engineering",
                title =
                    "Expose sync health from shared state",
                description =
                    "Surface synchronization state to clients.",
                status = "BACKLOG",
                priority = "LOW",
                version = 3,
                updatedAt =
                    "2026-08-12T09:54:00Z"
            )

        var failPatch =
            false

        val patchExpectedVersions =
            mutableListOf<Long>()

        private val engine =
            MockEngine { request ->

                val jsonHeaders =
                    headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json
                            .toString()
                    )

                when {
                    request.method ==
                        HttpMethod.Get &&
                        request.url
                            .toString()
                            .endsWith("/tasks") -> {

                        respond(
                            content =
                                jsonCodec.encodeToString(
                                    listOf(task)
                                ),
                            status =
                                HttpStatusCode.OK,
                            headers =
                                jsonHeaders
                        )
                    }

                    request.method ==
                        HttpMethod.Patch &&
                        request.url
                            .toString()
                            .endsWith(
                                "/tasks/task-29"
                            ) -> {

                        if (failPatch) {
                            respond(
                                content =
                                    """{"message":"server error"}""",
                                status =
                                    HttpStatusCode
                                        .InternalServerError,
                                headers =
                                    jsonHeaders
                            )
                        } else {

                            val requestBody =
                                request.body
                                    .toByteArray()
                                    .decodeToString()

                            val update =
                                jsonCodec
                                    .decodeFromString<
                                        UpdateTaskStatusRequest
                                    >(
                                        requestBody
                                    )

                            patchExpectedVersions +=
                                update.expectedVersion

                            if (
                                update.expectedVersion !=
                                task.version
                            ) {

                                respond(
                                    content =
                                        jsonCodec.encodeToString(
                                            ConflictResponse(
                                                code =
                                                    "VERSION_CONFLICT",
                                                message =
                                                    "Task changed on server.",
                                                currentTask =
                                                    task
                                            )
                                        ),
                                    status =
                                        HttpStatusCode
                                            .Conflict,
                                    headers =
                                        jsonHeaders
                                )

                            } else {

                                task =
                                    task.copy(
                                        status =
                                            update.status,
                                        version =
                                            task.version + 1
                                    )

                                respond(
                                    content =
                                        jsonCodec
                                            .encodeToString(
                                                task
                                            ),
                                    status =
                                        HttpStatusCode.OK,
                                    headers =
                                        jsonHeaders
                                )
                            }
                        }
                    }

                    else -> {
                        respond(
                            content =
                                """{"message":"not found"}""",
                            status =
                                HttpStatusCode.NotFound,
                            headers =
                                jsonHeaders
                        )
                    }
                }
            }

        private val client =
            HttpClient(engine) {
                expectSuccess = false

                install(
                    ContentNegotiation
                ) {
                    json(
                        jsonCodec
                    )
                }
            }

        val api =
            TaskApi(
                client = client,
                baseUrl =
                    "http" + "://test"
            )

        fun close() {
            client.close()
        }
    }
}
