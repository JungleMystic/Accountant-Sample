package com.lrm.accountant.utils

import android.os.Environment
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.lrm.accountant.constants.APP_FOLDER_NAME
import com.lrm.accountant.model.Account
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// This class exports the pdf using itext pdf library
class PdfExport {

    private val titleFont = Font(Font.FontFamily.TIMES_ROMAN, 16f, Font.BOLD)
    private lateinit var pdf: PdfWriter

    private val sdf = SimpleDateFormat("dd-MM-yyy hh-mm a", Locale.getDefault())
    private var date = Date()

    private lateinit var file: File

    // Create and return a File
    private fun createFile(): File {
        createDirectory()
        //Prepare file
        val fileName = "Accounts data ${sdf.format(date)}.pdf"
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/$APP_FOLDER_NAME"
        file = File(path, fileName)
        if (!file.exists()) file.createNewFile()
        return file
    }

    //Create and return Document
    private fun createDocument(): Document {
        val document = Document()
        document.setMargins(24f, 24f, 32f, 32f)
        document.pageSize = PageSize.A4
        return document
    }

    // Setting the PDF writer
    private fun setupPdfWriter(document: Document, file: File) {
        pdf = PdfWriter.getInstance(document, FileOutputStream(file))
        pdf.setFullCompression()
        //Open the document
        document.open()
    }

    private fun createTable(column: Int, columnWidth: FloatArray): PdfPTable {
        val table = PdfPTable(column)
        table.widthPercentage = 100f
        table.setWidths(columnWidth)
        table.headerRows = 1
        table.defaultCell.verticalAlignment = Element.ALIGN_CENTER
        table.defaultCell.horizontalAlignment = Element.ALIGN_CENTER
        return table
    }

    private fun createCell(content: String): PdfPCell {
        val cell = PdfPCell(Phrase(content))
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.verticalAlignment = Element.ALIGN_MIDDLE
        //setup padding
        cell.setPadding(8f)
        cell.isUseAscender = true
        cell.paddingLeft = 4f
        cell.paddingRight = 4f
        cell.paddingTop = 8f
        cell.paddingBottom = 8f
        return cell
    }

    // This creates a table
    fun createUserTable(
        data: List<Account>,
        onFinish: (file: File) -> Unit,
        onError: (Exception) -> Unit
    ) {
        //Define the document
        val file = createFile()
        val document = createDocument()

        //Setup PDF Writer
        setupPdfWriter(document, file)

        //Add table title
        document.add(Paragraph("Accounts Data", titleFont))
        document.add(Paragraph(" "))

        //Define Table
        val actIdWidth = 0.2f
        val actNameWidth = 1f
        val amountWidth = 0.5f
        val columnWidth = floatArrayOf(actIdWidth, actNameWidth, amountWidth)
        val table = createTable(3, columnWidth)

        //Table header (first row)
        val tableHeaderContent = listOf("ActID", "Account Name", "Amount")

        //write table header into table
        tableHeaderContent.forEach {
            //define a cell
            val cell = createCell(it)
            //add our cell into our table
            table.addCell(cell)
        }
        //write user data into table
        data.forEach {
            val actIdCell = createCell(it.actId.toString())
            table.addCell(actIdCell)

            val actNameCell = createCell(it.actName)
            table.addCell(actNameCell)

            val amountCell = createCell(it.amount.toString())
            table.addCell(amountCell)
        }
        document.add(table)
        document.add(Paragraph(" "))
        document.add(Paragraph("Created on ${sdf.format(date)}"))
        document.close()

        try {
            pdf.close()
        } catch (ex: Exception) {
            onError(ex)
        } finally {
            onFinish(file)
        }
    }
}