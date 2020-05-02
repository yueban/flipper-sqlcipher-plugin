package com.yueban.flipper.sqlcipher.plugin

import java.io.File

/**
 * @author yueban fbzhh007@gmail.com
 * @date 2020/5/2
 */
interface DatabasePasswordProvider {
    fun getDatabasePassword(databaseFile: File): String
}