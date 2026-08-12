package com.syncboard.server

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class TaskStoreTest {

    @Test
    fun seededTaskCanBeRead() {
        val store =
            TaskStore()

        val task =
            store.find(
                "task-29"
            )

        assertNotNull(task)

        assertEquals(
            "BACKLOG",
            task.status
        )

        assertEquals(
            3,
            task.version
        )
    }

    @Test
    fun successfulUpdateIncrementsVersion() {
        val store =
            TaskStore()

        val result =
            store.updateStatus(
                id = "task-29",
                request =
                    UpdateTaskStatusRequest(
                        status = "IN_PROGRESS",
                        expectedVersion = 3
                    )
            )

        val success =
            assertIs<UpdateResult.Success>(
                result
            )

        assertEquals(
            "IN_PROGRESS",
            success.task.status
        )

        assertEquals(
            4,
            success.task.version
        )
    }

    @Test
    fun staleUpdateDoesNotOverwriteServerTask() {
        val store =
            TaskStore()

        store.updateStatus(
            id = "task-29",
            request =
                UpdateTaskStatusRequest(
                    status = "DONE",
                    expectedVersion = 3
                )
        )

        val result =
            store.updateStatus(
                id = "task-29",
                request =
                    UpdateTaskStatusRequest(
                        status = "IN_PROGRESS",
                        expectedVersion = 3
                    )
            )

        assertIs<UpdateResult.Conflict>(
            result
        )

        val stored =
            store.find(
                "task-29"
            )

        assertNotNull(stored)

        assertEquals(
            "DONE",
            stored.status
        )

        assertEquals(
            4,
            stored.version
        )
    }

    @Test
    fun unknownTaskReturnsNotFound() {
        val store =
            TaskStore()

        val result =
            store.updateStatus(
                id = "missing",
                request =
                    UpdateTaskStatusRequest(
                        status = "DONE",
                        expectedVersion = 1
                    )
            )

        assertIs<UpdateResult.NotFound>(
            result
        )
    }
}
