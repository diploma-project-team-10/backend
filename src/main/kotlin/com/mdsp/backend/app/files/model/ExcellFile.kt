package com.mdsp.backend.app.files.model

import org.apache.poi.common.usermodel.HyperlinkType
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFHyperlink
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream
import kotlin.collections.ArrayList


class ExcellFile: Files(null) {

    private var cellStyle: CellStyle? = null

    fun createExcellFile(
        data: ArrayList<ArrayList<ExcelCell>>,
        path: String,
        sheetName: String = "Excell",
        serverName: String = ""
    ){
        val workbook: Workbook = XSSFWorkbook()
        val creationHelper: CreationHelper = workbook.getCreationHelper()
        val sheet: Sheet = workbook.createSheet(sheetName)

        var indexRow = 0
        for(rowData in data){
            createSheetRow(workbook, sheet, rowData, indexRow++, creationHelper, serverName)
        }

        val fileLocation = path + "\\temp.xlsx"
        val outputStream = FileOutputStream(fileLocation)
        workbook.write(outputStream)
        workbook.close()
    }

    private fun createSheetRow(
        workbook: Workbook,
        sheet: Sheet,
        rowCells: ArrayList<ExcelCell>,
        rowIndex: Int,
        creationHelper: CreationHelper,
        serverName: String = ""
    ){
        val row: Row = sheet.createRow(rowIndex)
        var headerCell = row.createCell(0)
        val cellStyle = workbook.createCellStyle()
        cellStyle.borderBottom = BorderStyle.MEDIUM
        cellStyle.borderLeft = BorderStyle.MEDIUM
        cellStyle.borderTop = BorderStyle.MEDIUM
        cellStyle.borderRight = BorderStyle.MEDIUM
        cellStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
        cellStyle.alignment = HorizontalAlignment.CENTER
        val cellFont = (workbook as XSSFWorkbook).createFont()
        cellFont.fontName = "Arial"
        cellStyle.setFont(cellFont)
        val hlinkstyle = workbook.createCellStyle()
        hlinkstyle.borderBottom = BorderStyle.MEDIUM
        hlinkstyle.borderLeft = BorderStyle.MEDIUM
        hlinkstyle.borderTop = BorderStyle.MEDIUM
        hlinkstyle.borderRight = BorderStyle.MEDIUM
        hlinkstyle.fillPattern = FillPatternType.SOLID_FOREGROUND
        hlinkstyle.alignment = HorizontalAlignment.CENTER
        val hlinkfont = (workbook as XSSFWorkbook).createFont()
        hlinkfont.underline = XSSFFont.U_SINGLE
        hlinkfont.color = IndexedColors.BLUE.index
        hlinkstyle.setFont(hlinkfont)
        for(cell: ExcelCell in rowCells){
            sheet.setColumnWidth(cell.getColumnIndex(), 6000)
            headerCell = row.createCell(cell.getColumnIndex())
            cellStyle.fillForegroundColor = cell.getBackgroundColor().getIndex()
            cellFont.color = cell.getFontColor().getIndex()
            cellFont.fontHeightInPoints = cell.getFontSize().toShort()
            cellFont.bold = cell.getIsBold()
            headerCell.cellStyle = cellStyle
            if(cell.getIsLink()){
                val link = creationHelper.createHyperlink(HyperlinkType.URL) as XSSFHyperlink
                link.address = serverName+"/#/vertical/reference/record/view/ce88aa43-dd6f-495c-9a05-a2fdfcc83fc1/"+cell.getLinkValue()
                headerCell.setHyperlink(link as XSSFHyperlink)
                hlinkstyle.fillForegroundColor = cell.getBackgroundColor().getIndex()
                headerCell.setCellStyle(hlinkstyle);
            }
            headerCell.setCellValue(cell.getValue())

        }
    }


    private fun cellStyle(
        workbook: Workbook,
        excelCell: ExcelCell
    ): CellStyle{
        val cellStyle = workbook.createCellStyle()
        cellStyle.borderBottom = BorderStyle.MEDIUM
        cellStyle.borderLeft = BorderStyle.MEDIUM
        cellStyle.borderTop = BorderStyle.MEDIUM
        cellStyle.borderRight = BorderStyle.MEDIUM
        cellStyle.fillForegroundColor = excelCell.getBackgroundColor().getIndex()
        cellStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
        cellStyle.alignment = HorizontalAlignment.CENTER

        val cellFont = (workbook as XSSFWorkbook).createFont()
        cellFont.fontName = "Arial"
        cellFont.color = excelCell.getFontColor().getIndex()
        cellFont.fontHeightInPoints = excelCell.getFontSize().toShort()
        cellFont.bold = excelCell.getIsBold()
        cellStyle.setFont(cellFont)
        return cellStyle
    }

//    for lingua
    fun createExcellFile2(keys: ArrayList<String>, data: ArrayList<MutableMap<String, String?>>, path: String){
//        val workbook: Workbook = XSSFWorkbook()
//
//        val sheet: Sheet = workbook.createSheet("Lingua Report")
//        val headerStyle = cellStyle(workbook, IndexedColors.WHITE, IndexedColors.ROYAL_BLUE, 14, true)
//        val cellStyle = cellStyle(workbook)
//
//        val headerRow: Row = sheet.createRow(0)
//
//        val headerNamesAndIndex = mutableMapOf<String, Int>()
//        var indexCol = 0
//        for(key in keys){
//            headerNamesAndIndex[key] = indexCol
//            sheet.setColumnWidth(indexCol, 6000)
//            val headerCell: Cell = headerRow.createCell(indexCol++)
//            headerCell.setCellValue(key.capitalize())
//            headerCell.cellStyle = headerStyle
//        }
//
//        var indexRow = 1
//        for(rowData in data){
//            val row: Row = sheet.createRow(indexRow++)
//            for(key in keys){
//                val columnIndex = headerNamesAndIndex[key]!!
//                val dataCell: Cell = row.createCell(columnIndex)
//                dataCell.setCellValue(rowData[key])
//                dataCell.cellStyle = cellStyle
//            }
//        }
//
//        val fileLocation = path + "\\temp.xlsx"
//
//        val outputStream = FileOutputStream(fileLocation)
//        workbook.write(outputStream)
//        workbook.close()
    }
}
