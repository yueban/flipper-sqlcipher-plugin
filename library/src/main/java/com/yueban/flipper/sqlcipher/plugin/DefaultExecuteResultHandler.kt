package com.yueban.flipper.sqlcipher.plugin

import android.database.Cursor
import com.facebook.flipper.plugins.databases.DatabaseDriver.DatabaseExecuteSqlResponse

/**
 * @author yueban fbzhh007@gmail.com
 * @date 2020/5/2
 */
class DefaultExecuteResultHandler : ExecuteResultHandler<DatabaseExecuteSqlResponse> {
    override fun handleRawQuery(): DatabaseExecuteSqlResponse {
        return DatabaseExecuteSqlResponse.successfulRawQuery()
    }

    override fun handleSelect(result: Cursor?): DatabaseExecuteSqlResponse {
        val columns = result!!.columnNames.asList()
        val rows: List<List<Any>> = Util.cursorToList(result)

        return DatabaseExecuteSqlResponse.successfulSelect(columns, rows)
    }

    override fun handleInsert(insertedId: Long): DatabaseExecuteSqlResponse {
        return DatabaseExecuteSqlResponse.successfulInsert(insertedId)
    }

    override fun handleUpdateDelete(count: Int): DatabaseExecuteSqlResponse {
        return DatabaseExecuteSqlResponse.successfulUpdateDelete(count)
    }
}