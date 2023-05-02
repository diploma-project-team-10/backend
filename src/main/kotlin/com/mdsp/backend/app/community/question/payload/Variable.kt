package com.mdsp.backend.app.community.question.payload

import lombok.AllArgsConstructor
import lombok.Data
import java.util.*
import kotlin.collections.ArrayList

@Data
@AllArgsConstructor
open class Variable {
//    TODO validation
    private var id: UUID? = null
    private var name: String = ""
    private var type: String? = ""
    private var condition: String = ""
    private var range: ArrayList<Long> = arrayListOf()
    private var delimiter: Int = 0
    private var isAssign: Boolean = false
    private var assignText: String = ""
    private var result: String = ""
    private var latexFormatResult: String = ""

    constructor(name: String, type: String, condition: String, range: ArrayList<Long>, delimiter: Int, isAssign: Boolean, assignText: String) {
        this.name = name
        this.type = type
        this.condition = condition
        this.range = range
        this.delimiter = delimiter
        this.isAssign = isAssign
        this.assignText = assignText
    }

    constructor()

    fun getId(): UUID? { return this.id }
    fun setId(id: UUID?) { this.id = id }

    fun getName(): String { return this.name }
    fun setName(name: String) { this.name = name }

    fun getType(): String? { return this.type }
    fun setType(type: String?) { this.type = type }

    fun getCondition(): String { return this.condition }
    fun setCondition(condition: String) { this.condition = condition }

    fun getRange(): ArrayList<Long> { return this.range }
    fun setRange(range: ArrayList<Long>) { this.range = range }

    fun getDelimiter(): Int { return this.delimiter }
    fun setDelimiter(delimiter: Int) { this.delimiter = delimiter }

    fun getIsAssign(): Boolean { return this.isAssign }
    fun setIsAssign(isAssign: Boolean) { this.isAssign = isAssign }

    fun getAssignText(): String { return this.assignText }
    fun setAssignText(assignText: String) { this.assignText = assignText }

    fun getResult(): String { return this.result }
    fun setResult(result: String) { this.result = result }

    fun getLatexFormatResult(): String { return this.latexFormatResult }
    fun setLatexFormatResult(latexFormatResult: String) { this.latexFormatResult = latexFormatResult }

    override fun toString(): String {
        return "Variable(name='$name', type='$type', condition='$condition', range=$range, delimiter=$delimiter, isAssign=$isAssign, assignText='$assignText', result='$result', latexFormatResult='$latexFormatResult')"
    }

    fun clone(): Variable {
        var temp = Variable()
        temp.setId(this.id)
        temp.setName(this.name)
        temp.setRange(this.range)
        temp.setCondition(this.condition)
        temp.setDelimiter(this.delimiter)
        temp.setIsAssign(this.isAssign)
        temp.setType(this.type)
        temp.setAssignText(this.assignText)
        temp.setResult(this.result)
        temp.setLatexFormatResult(this.latexFormatResult)
        return temp
    }

}


