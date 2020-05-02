package com.yueban.flipper.sqlcipher.sample.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

/**
 * @author yueban fbzhh007@gmail.com
 * @date 2020/5/2
 */
@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBooks(books: List<Book>)
}