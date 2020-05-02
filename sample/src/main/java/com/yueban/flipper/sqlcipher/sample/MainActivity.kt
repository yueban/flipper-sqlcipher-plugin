package com.yueban.flipper.sqlcipher.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yueban.flipper.sqlcipher.sample.db.AppDatabase
import com.yueban.flipper.sqlcipher.sample.db.Book

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // insert test data
        AppDatabase.getInstance().bookDao().insertBooks(
            listOf(
                Book("The Long Tail", "Chris Anderson", 256),
                Book("The Kite Runner", "Khaled Hosseini", 372),
                Book("The Moon and Sixpence", "William Somerset Maugham", 263),
                Book("Nineteen Eighty-Four", "George Orwell", 328)
            )
        )
    }
}
