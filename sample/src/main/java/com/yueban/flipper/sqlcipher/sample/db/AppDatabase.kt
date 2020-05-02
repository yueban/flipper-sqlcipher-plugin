package com.yueban.flipper.sqlcipher.sample.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yueban.flipper.sqlcipher.sample.App
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

/**
 * @author yueban fbzhh007@gmail.com
 * @date 2020/5/2
 */
@Database(entities = [Book::class], version = AppDatabase.VERSION, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao

    companion object {
        const val VERSION = 1
        const val NAME = "test"
        const val PASSWORD = "123456"

        private lateinit var mDbInstance: AppDatabase

        fun getInstance(): AppDatabase {
            if (!Companion::mDbInstance.isInitialized) {
                synchronized(AppDatabase::class.java) {
                    // sqlcipher
                    val passphrase: ByteArray = SQLiteDatabase.getBytes(PASSWORD.toCharArray())
                    val factory = SupportFactory(passphrase)

                    // create db instance
                    mDbInstance = Room.databaseBuilder(
                        App.getInstance(),
                        AppDatabase::class.java,
                        NAME
                    )
                        .allowMainThreadQueries()
                        .openHelperFactory(factory)
                        .build()
                }
            }
            return mDbInstance
        }
    }
}