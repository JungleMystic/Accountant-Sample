package com.lrm.accountant.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.lrm.accountant.R
import com.lrm.accountant.constants.TAG
import com.zynksoftware.documentscanner.ScanActivity
import com.zynksoftware.documentscanner.model.DocumentScannerErrorModel
import com.zynksoftware.documentscanner.model.ScannerResults

class ScanDocActivity : ScanActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_doc)
        addFragmentContentLayout()
    }

    override fun onClose() {
        val intent = Intent()
        intent.putExtra("IMAGE_URI", "")
        setResult(RESULT_CANCELED, intent)
        finish()
    }

    override fun onError(error: DocumentScannerErrorModel) {
        Toast.makeText(this, "${error.errorMessage?.error}", Toast.LENGTH_SHORT).show()
    }

    override fun onSuccess(scannerResults: ScannerResults) {
        val file = scannerResults.croppedImageFile!!
        Log.i(TAG, "onSuccess: -> file -> $file")
        val uri = Uri.fromFile(file)
        Log.i(TAG, "onSuccess: -> uri -> $uri")
        val intent = Intent()
        intent.putExtra("IMAGE_URI", uri.toString())
        setResult(RESULT_OK, intent)
        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
        finish()
    }
}