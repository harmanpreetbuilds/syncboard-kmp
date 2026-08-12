package com.syncboard.di

import com.syncboard.data.local.DatabaseDriverFactory
import com.syncboard.data.local.IosDatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module =
    module {
        single<DatabaseDriverFactory> {
            IosDatabaseDriverFactory()
        }
    }
