package com.lrm.accountant.utils

import android.os.Environment
import android.util.Log
import com.lrm.accountant.constants.APP_FOLDER_NAME
import com.lrm.accountant.constants.TAG
import java.io.File

// This checks and creates the App folder in Downloads folder
fun createDirectory() {
    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), APP_FOLDER_NAME)
    if (!file.exists()){
        file.mkdir()
        Log.i(TAG, "Folder is created")
    } else {
        Log.i(TAG, "Folder is already created")
    }
}