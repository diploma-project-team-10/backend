package com.mdsp.backend.app.files.model

import org.apache.poi.ss.usermodel.IndexedColors

// TODO implement with Builder class
class ExcelCell {
    private var id: String = ""
    private var columnIndex: Int = 0
    private var value: String = ""
    private var fontColor: IndexedColors = IndexedColors.BLACK
    private var backgroundColor: IndexedColors = IndexedColors.WHITE
    private var fontSize: Int = 10
    private var isBold: Boolean = false
    private var isLink: Boolean = false
    private var linkValue: String = ""


    fun setId(id: String) { this.id = id }
    fun setColumnIndex(index: Int) { this.columnIndex = index }
    fun setValue(value: String) { this.value = value }
    fun setFontColor(fontColor: IndexedColors) { this.fontColor = fontColor }
    fun setBackgroundColor(backgroundColor: IndexedColors) { this.backgroundColor = backgroundColor }
    fun setFontSize(fontSize: Int) { this.fontSize = fontSize }
    fun setIsBold(isBold: Boolean) { this.isBold = isBold }
    fun setIsLink(isLink: Boolean) { this.isLink = isLink }
    fun setLinkValue(value: String) { this.linkValue = value }

    fun getId() = this.id
    fun getColumnIndex() = this.columnIndex
    fun getValue() = this.value
    fun getFontColor() = this.fontColor
    fun getBackgroundColor() = this.backgroundColor
    fun getFontSize() = this.fontSize
    fun getIsBold() = this.isBold
    fun getIsLink() = this.isLink
    fun getLinkValue() = this.linkValue
    override fun toString(): String {
        return "ExcelCell(id='$id', columnIndex=$columnIndex, value='$value', fontColor=$fontColor, backgroundColor=$backgroundColor, fontSize=$fontSize, isBold=$isBold, isLink=$isLink, linkValue='$linkValue')"
    }


}
