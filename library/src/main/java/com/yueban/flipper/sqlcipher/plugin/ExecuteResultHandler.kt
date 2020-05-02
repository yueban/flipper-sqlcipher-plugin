package com.yueban.flipper.sqlcipher.plugin

import android.database.Cursor
import android.database.sqlite.SQLiteException

/**
 * @author yueban fbzhh007@gmail.com
 * @date 2020/5/2
 */
interface ExecuteResultHandler<RESULT> {
    @Throws(SQLiteException::class)
    fun handleRawQuery(): RESULT

    @Throws(SQLiteException::class)
    fun handleSelect(result: Cursor?): RESULT

    @Throws(SQLiteException::class)
    fun handleInsert(insertedId: Long): RESULT

    @Throws(SQLiteException::class)
    fun handleUpdateDelete(count: Int): RESULT
}