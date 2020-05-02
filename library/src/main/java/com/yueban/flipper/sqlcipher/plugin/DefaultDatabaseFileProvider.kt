package com.yueban.flipper.sqlcipher.plugin

import android.content.Context
import java.io.File
import java.util.*

/**
 * @author yueban fbzhh007@gmail.com
 * @date 2020/5/2
 */
class DefaultDatabaseFileProvider(
    private val context: Context
) {
    fun getDatabaseFiles(): MutableList<File> {
        val databaseFiles: MutableList<File> =
            ArrayList()
        for (databaseName in context.databaseList()) {
            databaseFiles.add(context.getDatabasePath(databaseName))
        }
        return databaseFiles
    }
}