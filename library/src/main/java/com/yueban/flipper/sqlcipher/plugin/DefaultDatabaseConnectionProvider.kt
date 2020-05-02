package com.yueban.flipper.sqlcipher.plugin

import net.sqlcipher.database.SQLiteDatabase
import java.io.File

/**
 * @author yueban fbzhh007@gmail.com
 * @date 2020/5/2
 */
class DefaultDatabaseConnectionProvider(
    private val databasePasswordProvider: DatabasePasswordProvider
) : DatabaseConnectionProvider {


    override fun openDatabase(databaseFile: File): SQLiteDatabase {
        val walFile =
            File(databaseFile.parent, databaseFile.name + "-wal")
        val db: SQLiteDatabase =
            SQLiteDatabase.openDatabase(
                databaseFile.absolutePath,
                databasePasswordProvider.getDatabasePassword(databaseFile),
                null,
                SQLiteDatabase.OPEN_READWRITE
            )
        db.rawExecSQL("PRAGMA foreign_keys=ON;")
        if (walFile.exists()) {
            db.rawExecSQL("PRAGMA journal_mode=WAL;")
        }
        return db
    }
}