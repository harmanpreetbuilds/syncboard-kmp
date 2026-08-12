package com.syncboard.di

import com.syncboard.data.local.DatabaseDriverFactory
import com.syncboard.data.remote.ApiConfig
import com.syncboard.data.remote.TaskApi
import com.syncboard.data.remote.createHttpClient
import com.syncboard.data.repository.TaskRepositoryImpl
import com.syncboard.database.SyncBoardDatabase
import com.syncboard.domain.repository.TaskRepository
import com.syncboard.presentation.tasks.TasksStateHolder
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformModule: Module

val sharedModule = module {

    single {
        createHttpClient()
    }

    single {
        TaskApi(
            client = get(),
            baseUrl = ApiConfig.baseUrl
        )
    }

    single {
        SyncBoardDatabase(
            get<DatabaseDriverFactory>()
                .createDriver()
        )
    }

    single<TaskRepository> {
        TaskRepositoryImpl(
            database = get(),
            api = get()
        )
    }

    factory {
        TasksStateHolder(
            repository = get()
        )
    }
}
