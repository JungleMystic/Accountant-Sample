package com.lrm.accountant.utils

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.lrm.accountant.constants.APP_FOLDER_NAME
import com.lrm.accountant.model.Account
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.IndexedColorMap
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExcelExport {

    fun createWorkbook(data: List<Account>): Workbook {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Accounts Data")
        //Create Header Cell Style
        val cellStyle = getHeaderStyle(workbook)

        //Creating sheet header row
        createSheetHeader(cellStyle, sheet)

        //Adding data to the sheet
        addData(1, sheet, data)

        return workbook
    }

    private fun createSheetHeader(cellStyle: CellStyle, sheet: Sheet) {
        //setHeaderStyle is a custom function written below to add header style

        //Create sheet first row
        val row = sheet.createRow(0)

        //Header list
        val tableHeads = listOf("Act Id", "Account Name", "Amount")

        //Loop to populate each column of header row
        for ((index, value) in tableHeads.withIndex()) {

            val columnWidth = (15 * 500)

            //index represents the column number
            sheet.setColumnWidth(index, columnWidth)

            //Create cell
            val cell = row.createCell(index)

            //value represents the header value from HEADER_LIST
            cell?.setCellValue(value)

            //Apply style to cell
            cell.cellStyle = cellStyle
        }
    }

    private fun getHeaderStyle(workbook: Workbook): CellStyle {

        //Cell style for header row
        val cellStyle: CellStyle = workbook.createCellStyle()

        //Apply cell color
        val colorMap: IndexedColorMap = (workbook as XSSFWorkbook).stylesSource.indexedColors
        var color = XSSFColor(IndexedColors.BLUE, colorMap).indexed
        cellStyle.fillForegroundColor = color
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)

        //Apply font style on cell text
        val whiteFont = workbook.createFont()
        color = XSSFColor(IndexedColors.WHITE, colorMap).indexed
        whiteFont.color = color
        whiteFont.bold = true
        cellStyle.setFont(whiteFont)

        return cellStyle
    }

    // This updates cells with our data
    private fun addData(rowIndex: Int, sheet: Sheet, data: List<Account>) {
        var rowInd = rowIndex
        for (account in data){
            //Create row based on row index
            val row = sheet.createRow(rowInd)
            createCell(row, 0, account.actId.toString())
            createCell(row, 1, account.actName)
            createCell(row, 2, account.amount.toString())
            rowInd++
        }
    }

    private fun createCell(row: Row, columnIndex: Int, value: String?) {
        val cell = row.createCell(columnIndex)
        cell?.setCellValue(value)
    }

    fun createExcel(context: Context, workbook: Workbook) {
        //Get App Director, APP_DIRECTORY_NAME is a string
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .toString() + "/$APP_FOLDER_NAME"

        //Create excel file with extension .xlsx
        val sdf = SimpleDateFormat("dd-MM-yyy hh-mm a", Locale.getDefault())
        val date = Date()
        val fileName = "Accounts data ${sdf.format(date)}.xlsx"
        val file = File(path, fileName)
        if (!file.exists()) file.createNewFile()

        //Write workbook to file using FileOutputStream
        try {
            val fileOut = FileOutputStream(file)
            workbook.write(fileOut)
            fileOut.close()
            Toast.makeText(context, "Excel exported successfully", Toast.LENGTH_SHORT).show()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}