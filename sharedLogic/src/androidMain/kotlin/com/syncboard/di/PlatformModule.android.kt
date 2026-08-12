package com.syncboard.di

import com.syncboard.data.local.AndroidDatabaseDriverFactory
import com.syncboard.data.local.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module =
    module {
        single<DatabaseDriverFactory> {
            AndroidDatabaseDriverFactory(
                context = androidContext()
            )
        }
    }
