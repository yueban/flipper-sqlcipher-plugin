package com.yueban.flipper.sqlcipher.plugin

import android.database.Cursor
import java.util.*

/**
 * @author yueban fbzhh007@gmail.com
 * @date 2020/5/2
 */
object Util {
    fun cursorToList(cursor: Cursor): List<List<Any?>> {
        val rows: MutableList<List<Any?>> =
            ArrayList()
        val numColumns = cursor.columnCount
        while (cursor.moveToNext()) {
            val values: MutableList<Any?> =
                ArrayList()
            for (column in 0 until numColumns) {
                values.add(getObjectFromColumnIndex(cursor, column))
            }
            rows.add(values)
        }
        return rows
    }

    fun getObjectFromColumnIndex(
        cursor: Cursor,
        column: Int
    ): Any? {
        return when (cursor.getType(column)) {
            Cursor.FIELD_TYPE_NULL -> null
            Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(column)
            Cursor.FIELD_TYPE_FLOAT -> cursor.getDouble(column)
            Cursor.FIELD_TYPE_BLOB -> cursor.getBlob(column)
            Cursor.FIELD_TYPE_STRING -> cursor.getString(column)
            else -> cursor.getString(column)
        }
    }
}