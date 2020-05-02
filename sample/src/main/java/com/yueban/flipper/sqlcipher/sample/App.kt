package com.yueban.flipper.sqlcipher.sample

import android.app.Application
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.soloader.SoLoader
import com.yueban.flipper.sqlcipher.plugin.DatabasePasswordProvider
import com.yueban.flipper.sqlcipher.plugin.SqlCipherDatabaseDriver
import com.yueban.flipper.sqlcipher.sample.db.AppDatabase
import java.io.File

/**
 * @author yueban fbzhh007@gmail.com
 * @date 2020/5/2
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        sInstance = this

        SoLoader.init(this, false)
        if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
            val client = AndroidFlipperClient.getInstance(this)

            // add database plugin
            client.addPlugin(
                DatabasesFlipperPlugin(
                    SqlCipherDatabaseDriver(this, object : DatabasePasswordProvider {
                        override fun getDatabasePassword(databaseFile: File): String {
                            val fileName: String = Util.getFileNameNoExtension(databaseFile.path)
                            return if (AppDatabase.NAME == fileName) {
                                return AppDatabase.PASSWORD
                            } else {
                                ""
                            }
                        }
                    })
                )
            )

            client.start()
        }
    }


    companion object {
        private lateinit var sInstance: App

        fun getInstance() =
            sInstance
    }
}