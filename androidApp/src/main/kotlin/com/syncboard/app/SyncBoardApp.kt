package com.syncboard.app

import androidx.compose.runtime.Composable
import com.syncboard.app.ui.theme.SyncBoardTheme
import com.syncboard.app.ui.workspace.WorkspaceScreen
import com.syncboard.presentation.tasks.TasksStateHolder

@Composable
fun SyncBoardApp(
    stateHolder: TasksStateHolder
) {
    SyncBoardTheme {
        WorkspaceScreen(
            stateHolder = stateHolder
        )
    }
}
