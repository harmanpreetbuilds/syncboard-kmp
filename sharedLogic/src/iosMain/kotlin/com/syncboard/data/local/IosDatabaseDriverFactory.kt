package com.syncboard.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.syncboard.database.SyncBoardDatabase

class IosDatabaseDriverFactory : DatabaseDriverFactory {

    override fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = SyncBoardDatabase.Schema,
            name = "syncboard.db"
        )
    }
}
