package com.yueban.flipper.sqlcipher.plugin

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.text.TextUtils
import com.facebook.flipper.plugins.databases.DatabaseDescriptor
import com.facebook.flipper.plugins.databases.DatabaseDriver
import net.sqlcipher.DatabaseUtils
import net.sqlcipher.database.SQLiteDatabase
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*

/**
 * @author yueban fbzhh007@gmail.com
 * @date 2020/5/2
 */
class SqlCipherDatabaseDriver(
    context: Context,
    private val databaseFilesProvider: DefaultDatabaseFileProvider,
    private val databaseConnectionProvider: DatabaseConnectionProvider
) : DatabaseDriver<SqlCipherDatabaseDriver.SqlCipherDatabaseDescriptor>(context) {

    constructor(
        context: Context,
        databasePasswordProvider: DatabasePasswordProvider
    ) : this(
        context,
        DefaultDatabaseFileProvider(context),
        DefaultDatabaseConnectionProvider(databasePasswordProvider)
    )

    private val handler: ExecuteResultHandler<DatabaseExecuteSqlResponse> =
        DefaultExecuteResultHandler()

    class SqlCipherDatabaseDescriptor(
        val file: File
    ) : DatabaseDescriptor {
        override fun name(): String {
            return "${file.name} (SQLCipher)"
        }
    }

    override fun getTableInfo(
        databaseDescriptor: SqlCipherDatabaseDescriptor?,
        table: String?
    ): DatabaseGetTableInfoResponse {
        return databaseConnectionProvider.openDatabase(databaseDescriptor!!.file).use { database ->
            database.rawQuery(
                "SELECT sql FROM $SCHEMA_TABLE WHERE name=?",
                arrayOf(table!!)
            ).use { definitionCursor ->
                // Definition
                definitionCursor.moveToFirst()
                val definition =
                    definitionCursor.getString(definitionCursor.getColumnIndex("sql"))
                DatabaseGetTableInfoResponse(definition)
            }
        }
    }

    override fun getTableStructure(
        databaseDescriptor: SqlCipherDatabaseDescriptor?,
        table: String?
    ): DatabaseGetTableStructureResponse {
        val database =
            databaseConnectionProvider.openDatabase(databaseDescriptor!!.file)
        return try {
            val structureCursor: Cursor =
                database.rawQuery("PRAGMA table_info($table)", null)
            val foreignKeysCursor: Cursor =
                database.rawQuery("PRAGMA foreign_key_list($table)", null)
            val indexesCursor: Cursor =
                database.rawQuery("PRAGMA index_list($table)", null)
            try {
                // Structure & foreign keys
                val structureColumns =
                    listOf(
                        "column_name",
                        "data_type",
                        "nullable",
                        "default",
                        "primary_key",
                        "foreign_key"
                    )
                val structureValues: MutableList<List<Any?>> =
                    ArrayList()
                val foreignKeyValues: MutableMap<String, String> =
                    HashMap()
                while (foreignKeysCursor.moveToNext()) {
                    foreignKeyValues[foreignKeysCursor.getString(foreignKeysCursor.getColumnIndex("from"))] =
                        foreignKeysCursor.getString(foreignKeysCursor.getColumnIndex("table")) + "(" +
                                foreignKeysCursor.getString(foreignKeysCursor.getColumnIndex("to")) + ")"
                }
                while (structureCursor.moveToNext()) {
                    val columnName =
                        structureCursor.getString(structureCursor.getColumnIndex("name"))
                    val foreignKey =
                        if (foreignKeyValues.containsKey(columnName)) foreignKeyValues[columnName] else null
                    structureValues.add(
                        listOf(
                            columnName,
                            structureCursor.getString(structureCursor.getColumnIndex("type")),
                            structureCursor.getInt(structureCursor.getColumnIndex("notnull")) == 0,  // true if Nullable, false otherwise
                            Util.getObjectFromColumnIndex(
                                structureCursor,
                                structureCursor.getColumnIndex("dflt_value")
                            ),
                            structureCursor.getInt(structureCursor.getColumnIndex("pk")) == 1,
                            foreignKey
                        )
                    )
                }

                // Indexes
                val indexesColumns =
                    listOf(
                        "index_name",
                        "unique",
                        "indexed_column_name"
                    )
                val indexesValues: MutableList<List<Any>> =
                    ArrayList()
                while (indexesCursor.moveToNext()) {
                    val indexedColumnNames: MutableList<String?> =
                        ArrayList()
                    val indexName =
                        indexesCursor.getString(indexesCursor.getColumnIndex("name"))
                    database.rawQuery("PRAGMA index_info($indexName)", null)
                        .use { indexInfoCursor ->
                            while (indexInfoCursor.moveToNext()) {
                                indexedColumnNames.add(
                                    indexInfoCursor.getString(
                                        indexInfoCursor.getColumnIndex(
                                            "name"
                                        )
                                    )
                                )
                            }
                            indexesValues.add(
                                listOf<Any>(
                                    indexName,
                                    indexesCursor.getInt(indexesCursor.getColumnIndex("unique")) == 1,
                                    TextUtils.join(",", indexedColumnNames)
                                )
                            )
                        }
                }
                DatabaseGetTableStructureResponse(
                    structureColumns,
                    structureValues,
                    indexesColumns,
                    indexesValues
                )
            } finally {
                structureCursor.close()
                foreignKeysCursor.close()
                indexesCursor.close()
            }
        } finally {
            database.close()
        }
    }

    override fun getTableNames(databaseDescriptor: SqlCipherDatabaseDescriptor?): MutableList<String> {
        return try {
            openDatabase(databaseDescriptor!!)
        } catch (e: java.lang.RuntimeException) {
            throw SQLiteException("Unable to open database", e)
        }.use { database ->
            database.rawQuery(
                "SELECT name FROM sqlite_master WHERE type IN (?, ?)",
                arrayOf("table", "view")
            ).use { cursor ->
                val tableNames: MutableList<String> =
                    ArrayList(cursor.count)
                while (cursor.moveToNext()) {
                    tableNames.add(cursor.getString(0))
                }
                tableNames
            }
        }
    }

    override fun getTableData(
        databaseDescriptor: SqlCipherDatabaseDescriptor?,
        table: String?,
        order: String?,
        reverse: Boolean,
        start: Int,
        count: Int
    ): DatabaseGetTableDataResponse {
        return databaseConnectionProvider.openDatabase(databaseDescriptor!!.file).use { database ->
            val orderBy =
                if (order != null) order + (if (reverse) " DESC" else " ASC") else null
            val limitBy = "$start, $count"
            val total = DatabaseUtils.queryNumEntries(database, table)
            database.query(table, null, null, null, null, null, orderBy, limitBy).use { cursor ->
                val columnNames = cursor.columnNames
                val rows: List<List<Any>> = Util.cursorToList(cursor)
                DatabaseGetTableDataResponse(
                    listOf(*columnNames),
                    rows,
                    start,
                    rows.size,
                    total
                )
            }
        }
    }

    override fun getDatabases(): MutableList<SqlCipherDatabaseDescriptor> {
        val potentialDatabaseFiles =
            databaseFilesProvider.getDatabaseFiles()
        potentialDatabaseFiles.sort()
        val tidiedList: List<File> =
            tidyDatabaseList(potentialDatabaseFiles)

        val databases: MutableList<SqlCipherDatabaseDescriptor> =
            ArrayList(tidiedList.size)
        for (databaseFile in tidiedList) {
            if (checkFileHeader(databaseFile)) {
                databases.add(SqlCipherDatabaseDescriptor(databaseFile))
            }
        }
        return databases
    }

    override fun executeSQL(
        databaseDescriptor: SqlCipherDatabaseDescriptor?,
        query: String?
    ): DatabaseExecuteSqlResponse {
        return openDatabase(databaseDescriptor!!).use { database ->
            when (getFirstWord(query!!).toUpperCase(Locale.ROOT)) {
                "UPDATE", "DELETE" -> executeUpdateDelete(
                    database,
                    query,
                    handler
                )
                "INSERT" -> executeInsert(
                    database,
                    query,
                    handler
                )
                "SELECT", "PRAGMA", "EXPLAIN" -> executeSelect(
                    database,
                    query,
                    handler
                )
                else -> executeRawQuery(
                    database,
                    query, handler
                )
            }
        }
    }

    private fun tidyDatabaseList(databaseFiles: List<File>): List<File> {
        val originalAsSet: Set<File> =
            HashSet(databaseFiles)
        val tidiedList: MutableList<File> =
            ArrayList()
        for (databaseFile in databaseFiles) {
            val databaseFilename = databaseFile.path
            val sansSuffix = removeSuffix(
                databaseFilename,
                UNINTERESTING_FILENAME_SUFFIXES
            )
            if (sansSuffix == databaseFilename || !originalAsSet.contains(File(sansSuffix))) {
                tidiedList.add(databaseFile)
            }
        }
        return tidiedList
    }

    private fun removeSuffix(
        str: String,
        suffixesToRemove: Array<String>
    ): String {
        for (suffix in suffixesToRemove) {
            if (str.endsWith(suffix)) {
                return str.substring(0, str.length - suffix.length)
            }
        }
        return str
    }

    private fun getFirstWord(s: String): String {
        val firstStr = s.trim { it <= ' ' }
        val firstSpace = firstStr.indexOf(' ')
        return if (firstSpace >= 0) firstStr.substring(0, firstSpace) else firstStr
    }

    private fun <T> executeUpdateDelete(
        database: SQLiteDatabase,
        query: String,
        handler: ExecuteResultHandler<T>
    ): T {
        val statement = database.compileStatement(query)
        return try {
            val count = statement.executeUpdateDelete()
            handler.handleUpdateDelete(count)
        } finally {
            try {
                statement.close()
            } catch (e: Exception) {
                throw RuntimeException("Exception attempting to close statement", e)
            }
        }
    }

    private fun <T> executeInsert(
        database: SQLiteDatabase,
        query: String,
        handler: ExecuteResultHandler<T>
    ): T {
        val statement = database.compileStatement(query)
        val count = statement.executeInsert()
        return handler.handleInsert(count)
    }

    private fun <T> executeSelect(
        database: SQLiteDatabase,
        query: String,
        handler: ExecuteResultHandler<T>
    ): T {
        return database.rawQuery(query, null).use { cursor ->
            handler.handleSelect(cursor)
        }
    }

    private fun <T> executeRawQuery(
        database: SQLiteDatabase,
        query: String,
        handler: ExecuteResultHandler<T>
    ): T {
        database.execSQL(query)
        return handler.handleRawQuery()
    }

    private fun openDatabase(databaseDesc: SqlCipherDatabaseDescriptor): SQLiteDatabase {
        return databaseConnectionProvider.openDatabase(databaseDesc.file)
    }

    private fun checkFileHeader(databaseFile: File): Boolean {
        var input: FileInputStream? = null
        return try {
            input = FileInputStream(databaseFile)
            val magic = ByteArray(16)
            input.read(magic)
            !magic.contentEquals(SQLITE_MAGIC_BYTES)
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (ignored: IOException) {
                }
            }
        }
    }

    companion object {
        private const val SCHEMA_TABLE = "sqlite_master"

        private val SQLITE_MAGIC_BYTES = "SQLite format 3\u0000".toByteArray()

        private val UNINTERESTING_FILENAME_SUFFIXES =
            arrayOf(
                "-journal", "-shm", "-uid", "-wal"
            )
    }
}