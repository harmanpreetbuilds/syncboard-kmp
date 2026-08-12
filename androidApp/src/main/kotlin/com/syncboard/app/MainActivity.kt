package com.syncboard.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.syncboard.presentation.tasks.TasksStateHolder
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val tasksStateHolder:
        TasksStateHolder by inject()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            SyncBoardApp(
                stateHolder = tasksStateHolder
            )
        }
    }

    override fun onDestroy() {
        tasksStateHolder.close()
        super.onDestroy()
    }
}
