package com.syncboard.server

import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {

    private val json =
        Json {
            ignoreUnknownKeys = true
        }

    @Test
    fun healthEndpointReturnsOk() = testApplication {
        application {
            syncBoardModule()
        }

        val response =
            client.get("/health")

        assertEquals(
            HttpStatusCode.OK,
            response.status
        )

        val body =
            json
                .parseToJsonElement(
                    response.bodyAsText()
                )
                .jsonObject

        assertEquals(
            "ok",
            body["status"]
                ?.jsonPrimitive
                ?.content
        )
    }

    @Test
    fun tasksEndpointReturnsSeededTasks() = testApplication {
        application {
            syncBoardModule()
        }

        val response =
            client.get("/tasks")

        assertEquals(
            HttpStatusCode.OK,
            response.status
        )

        val tasks =
            json
                .parseToJsonElement(
                    response.bodyAsText()
                )
                .jsonArray

        assertEquals(
            4,
            tasks.size
        )

        val ids =
            tasks.map {
                it.jsonObject["id"]
                    ?.jsonPrimitive
                    ?.content
            }

        assertTrue(
            ids.containsAll(
                listOf(
                    "task-41",
                    "task-38",
                    "task-35",
                    "task-29"
                )
            )
        )
    }

    @Test
    fun validVersionUpdateSucceeds() = testApplication {
        application {
            syncBoardModule()
        }

        val response =
            client.patch(
                "/tasks/task-29"
            ) {
                contentType(
                    ContentType.Application.Json
                )

                setBody(
                    """
                    {
                      "status": "IN_PROGRESS",
                      "expectedVersion": 3
                    }
                    """.trimIndent()
                )
            }

        assertEquals(
            HttpStatusCode.OK,
            response.status
        )

        val task =
            json
                .parseToJsonElement(
                    response.bodyAsText()
                )
                .jsonObject

        assertEquals(
            "IN_PROGRESS",
            task["status"]
                ?.jsonPrimitive
                ?.content
        )

        assertEquals(
            4L,
            task["version"]
                ?.jsonPrimitive
                ?.long
        )
    }

    @Test
    fun staleVersionReturnsConflict() = testApplication {
        application {
            syncBoardModule()
        }

        val firstResponse =
            client.patch(
                "/tasks/task-29"
            ) {
                contentType(
                    ContentType.Application.Json
                )

                setBody(
                    """
                    {
                      "status": "IN_PROGRESS",
                      "expectedVersion": 3
                    }
                    """.trimIndent()
                )
            }

        assertEquals(
            HttpStatusCode.OK,
            firstResponse.status
        )

        val staleResponse =
            client.patch(
                "/tasks/task-29"
            ) {
                contentType(
                    ContentType.Application.Json
                )

                setBody(
                    """
                    {
                      "status": "DONE",
                      "expectedVersion": 3
                    }
                    """.trimIndent()
                )
            }

        assertEquals(
            HttpStatusCode.Conflict,
            staleResponse.status
        )

        val conflict =
            json
                .parseToJsonElement(
                    staleResponse.bodyAsText()
                )
                .jsonObject

        assertEquals(
            "VERSION_CONFLICT",
            conflict["code"]
                ?.jsonPrimitive
                ?.content
        )

        val currentTask =
            conflict["currentTask"]
                ?.jsonObject

        assertEquals(
            "IN_PROGRESS",
            currentTask
                ?.get("status")
                ?.jsonPrimitive
                ?.content
        )

        assertEquals(
            4L,
            currentTask
                ?.get("version")
                ?.jsonPrimitive
                ?.long
        )
    }

    @Test
    fun missingTaskReturnsNotFound() = testApplication {
        application {
            syncBoardModule()
        }

        val response =
            client.patch(
                "/tasks/task-does-not-exist"
            ) {
                contentType(
                    ContentType.Application.Json
                )

                setBody(
                    """
                    {
                      "status": "DONE",
                      "expectedVersion": 1
                    }
                    """.trimIndent()
                )
            }

        assertEquals(
            HttpStatusCode.NotFound,
            response.status
        )

        val body =
            json
                .parseToJsonElement(
                    response.bodyAsText()
                )
                .jsonObject

        assertEquals(
            "TASK_NOT_FOUND",
            body["code"]
                ?.jsonPrimitive
                ?.content
        )
    }
}
