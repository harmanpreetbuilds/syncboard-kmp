package com.syncboard.server

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(
        factory = Netty,
        host = "0.0.0.0",
        port = 8080,
        module = Application::syncBoardModule
    ).start(
        wait = true
    )
}

fun Application.syncBoardModule() {

    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
        )
    }

    val store =
        TaskStore()

    routing {

        get("/health") {
            call.respond(
                HealthResponse(
                    status = "ok"
                )
            )
        }

        get("/tasks") {
            call.respond(
                store.all()
            )
        }

        patch("/tasks/{id}") {

            val taskId =
                call.parameters["id"]

            if (taskId.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiError(
                        code = "INVALID_TASK_ID",
                        message = "Task id is required."
                    )
                )

                return@patch
            }

            val request =
                call.receive<UpdateTaskStatusRequest>()

            when (
                val result =
                    store.updateStatus(
                        taskId,
                        request
                    )
            ) {

                is UpdateResult.Success -> {
                    call.respond(
                        HttpStatusCode.OK,
                        result.task
                    )
                }

                is UpdateResult.Conflict -> {
                    call.respond(
                        HttpStatusCode.Conflict,
                        ConflictResponse(
                            code = "VERSION_CONFLICT",
                            message = "The task has changed since it was last synchronized.",
                            currentTask =
                                result.currentTask
                        )
                    )
                }

                UpdateResult.NotFound -> {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiError(
                            code = "TASK_NOT_FOUND",
                            message = "The requested task does not exist."
                        )
                    )
                }
            }
        }
    }
}
