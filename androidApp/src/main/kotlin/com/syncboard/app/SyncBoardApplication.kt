package com.syncboard.app

import android.app.Application
import com.syncboard.di.platformModule
import com.syncboard.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SyncBoardApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@SyncBoardApplication)

            modules(
                sharedModule,
                platformModule
            )
        }
    }
}
