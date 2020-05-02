package com.yueban.flipper.sqlcipher.sample.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author yueban fbzhh007@gmail.com
 * @date 2020/5/2
 */
@Entity
class Book(
    @PrimaryKey
    val title: String,
    val author: String,
    val pages: Int
)