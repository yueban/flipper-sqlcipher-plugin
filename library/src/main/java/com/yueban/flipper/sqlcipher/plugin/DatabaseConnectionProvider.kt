package com.yueban.flipper.sqlcipher.plugin

import net.sqlcipher.SQLException
import net.sqlcipher.database.SQLiteDatabase
import java.io.File

/**
 * @author yueban fbzhh007@gmail.com
 * @date 2020/5/2
 */
interface DatabaseConnectionProvider {
    @Throws(SQLException::class)
    fun openDatabase(databaseFile: File): SQLiteDatabase
}