package com.lrm.accountant.utils

import android.os.Environment
import android.util.Log
import com.lrm.accountant.constants.APP_FOLDER_NAME
import com.lrm.accountant.constants.TAG
import java.io.File

fun createDirectory() {
    Log.i(TAG, "createDirectory is called")
    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), APP_FOLDER_NAME)
    if (!file.exists()){
        file.mkdir()
        Log.i(TAG, "Folder is created")
    } else {
        Log.i(TAG, "Folder is already created")
    }
}