package com.syncboard.presentation.tasks

sealed interface TasksEffect {

    data class OpenTask(
        val taskId: String
    ) : TasksEffect

    data class ShowMessage(
        val message: String
    ) : TasksEffect
}
