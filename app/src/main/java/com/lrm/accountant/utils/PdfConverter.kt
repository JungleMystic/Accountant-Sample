package com.lrm.accountant.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.lrm.accountant.R
import com.lrm.accountant.adapter.PdfExportListAdapter
import com.lrm.accountant.constants.APP_FOLDER_NAME
import com.lrm.accountant.model.Account
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// This class create a bitmap from layout and then converts into PDF.
class PdfConverter {

    fun createPdf(
        activity: Activity,
        context: Context,
        data: List<Account>
        ) {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_home, null)
        val adapter = PdfExportListAdapter()
        val bitmap = createBitmapFromView(activity, context, view, data, adapter)
        convertBitmapToPdf(bitmap, activity, "Accounts Data v2")
    }

    private fun createBitmapFromView(
        activity: Activity,
        context: Context,
        view: View,
        data: List<Account>,
        adapter: PdfExportListAdapter,
    ): Bitmap {
        val totalTv = view.findViewById<TextView>(R.id.total_amount)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.adapter = adapter
        var amount = 0.0
        data.forEach { amount += it.amount }
        totalTv.text = HtmlCompat
            .fromHtml(activity.resources.getString(R.string.total_amount, amount.toString()), HtmlCompat.FROM_HTML_MODE_LEGACY)
        data.let { adapter.submitList(it) }
        return createBitmap(activity, context, view)
    }

    private fun createBitmap(
        activity: Activity,
        context: Context,
        view: View
    ): Bitmap {

        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.getRealMetrics(displayMetrics)
            displayMetrics.densityDpi
        } else {
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        }

        view.measure(
            View.MeasureSpec.makeMeasureSpec(
                displayMetrics.widthPixels, View.MeasureSpec.EXACTLY
            ),
            View.MeasureSpec.makeMeasureSpec(
                displayMetrics.heightPixels, View.MeasureSpec.EXACTLY
            )
        )

        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)

        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight, Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return Bitmap.createScaledBitmap(bitmap, 1080, 1920, true)
    }

    fun convertBitmapToPdf(bitmap: Bitmap, context: Context, name: String) {
        createDirectory()

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        page.canvas.drawBitmap(bitmap, 0F, 0F, null)
        pdfDocument.finishPage(page)

        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/$APP_FOLDER_NAME"
        val sdf = SimpleDateFormat("dd-MM-yyy hh-mm a", Locale.getDefault())
        val date = Date()
        val fileName = "$name ${sdf.format(date)}.pdf"
        val file = File(path, fileName)
        if (!file.exists()) file.createNewFile()

        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        Toast.makeText(context, "PDF v2 exported successfully", Toast.LENGTH_SHORT).show()
    }
}