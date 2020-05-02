package com.yueban.flipper.sqlcipher.sample

import android.text.TextUtils
import java.io.File

/**
 * @author yueban fbzhh007@gmail.com
 * @date 2020/5/2
 */
object Util {
    fun getFileNameNoExtension(filePath: String): String {
        if (TextUtils.isEmpty(filePath)) {
            return ""
        }
        val lastPoi = filePath.lastIndexOf('.')
        val lastSep = filePath.lastIndexOf(File.separator)
        if (lastSep == -1) {
            return if (lastPoi == -1) filePath else filePath.substring(0, lastPoi)
        }
        return if (lastPoi == -1 || lastSep > lastPoi) {
            filePath.substring(lastSep + 1)
        } else filePath.substring(lastSep + 1, lastPoi)
    }
}