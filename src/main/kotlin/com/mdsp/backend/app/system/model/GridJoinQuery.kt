package com.mdsp.backend.app.system.model

import com.mdsp.backend.app.reference.model.fields.Field
import com.mdsp.backend.app.reference.model.fields.FieldFactory
import com.mdsp.backend.app.system.config.DataSourceConfiguration
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.JdbcTemplate

class GridJoinQuery {

    private var tableName: String = ""

    private var fields: Array<String> = arrayOf()

    private var fieldsTable: MutableMap<String, Array<String>> = mutableMapOf()
    private var joinTable: MutableMap<String, String> = mutableMapOf()
    private var joinTableSide: MutableMap<String, String> = mutableMapOf()

    private var refFields: MutableMap<String, Any?>  = mutableMapOf()
    private var columnTable: MutableMap<String, Any?> = mutableMapOf()
    private var columnTables: MutableMap<String, MutableMap<String, Any?>> = mutableMapOf()
    private var bind: Array<Any?> = arrayOf()
    private var bindHaving: Array<Any?> = arrayOf()

    private var order: Array<String> = arrayOf()
    private var groupBy: Array<String> = arrayOf()
    private var condition: String = ""
    private var conditionHaving: String = ""
    private var hasCondition: Boolean = false
    private var hasConditionHaving: Boolean = false

    private var pageNumber: PageRequest? = null

    private var removed = false

    private lateinit var jdbc: JdbcTemplate

    private lateinit var dataSourceConfig: DataSourceConfiguration

    constructor(tableNameD: String, dataSourceConfiguration: DataSourceConfiguration) {
        tableName = tableNameD
        fieldsTable[tableName] = arrayOf("id")
        columnTables[tableName] = mutableMapOf()
        dataSourceConfig = dataSourceConfiguration
        jdbc = JdbcTemplate(dataSourceConfig.dataBaseOneTemplate)
        this.getColumnFromTable()
    }

    fun setRefFields(refFields: MutableMap<String, Any?>) {
        //TODO
        this.refFields.putAll(refFields)
    }

    fun setPageRequest(pageNumber: PageRequest) {
        this.pageNumber = pageNumber
    }

    fun getTableName() = tableName
    fun setTableName(tableNameD: String) {
        tableName = tableNameD
    }

    private fun customFields() {
        addColumns("created_at")
        addColumns("updated_at")
        addColumns("creator")
        addColumns("editor")
    }

    fun addJoin(table: String, condition: String, join: String = "INNER") {
        joinTable[table] = condition
        joinTableSide[table] = join
        fieldsTable[table] = arrayOf()
        columnTables[table] = mutableMapOf()
        this.getColumnFromTable(table)
    }

    fun setColumnEmpty() {
        this.fields = arrayOf()
        for ((key, value) in fieldsTable) {
            fieldsTable[key] = arrayOf()
        }
    }

    fun setConditionEmpty() {
        this.condition = ""
    }

    fun addColumns(column: String, table: String = tableName, asName: String = "") {
        if (checkField(column, table)) {
            if (asName.isNotEmpty()) {
                fieldsTable[table] = fieldsTable[table]!!.plus("$column as $asName")
            } else {
                fieldsTable[table] = fieldsTable[table]!!.plus(column)
            }
        } else {
            println("Something wrong in $column, $table")
        }
//        this.fields = this.fields.plus(column)
    }

    fun addColumn(column: String) {
        this.fields = this.fields.plus(column)
    }

    fun addFunctionSelect(function: String, column: String, table: String = tableName) {
        if (checkField(column, table)) {
//            fieldsTable[table] = fieldsTable[tableName]!!.plus(column)
            this.fields = this.fields.plus(function.replace("{col}", "$table.$column"))
        } else {
            println("Something wrong in $column, $table")
        }
    }

    fun addCondition(condition: String?, bind: Array<Any?>) {
        if (condition != null && condition.isNotEmpty())  {
            this.condition += " $condition"
            for (item in bind) {
                this.bind = this.bind.plusElement(item)
            }
            this.hasCondition = true
        }
    }

    fun addOrder(fieldId: String, type: String, table: String = tableName) {
        if (
//            checkField(fieldId, table)
//            &&
            (type.lowercase() == "asc" || type.lowercase() == "desc" || type.isEmpty())
        )  {
            if (table.isBlank()) {
                this.order = this.order.plus("$fieldId ${type.uppercase()}")
            } else {
                this.order = this.order.plus("$table.$fieldId ${type.uppercase()}")
            }

        } else {
            println("Something wrong in $fieldId, $table, $type")
        }
    }

    fun addGroupBy(fieldId: String, table: String = tableName) {
        if (checkField(fieldId, table)) {
            this.groupBy = this.groupBy.plus("$table.$fieldId")
        }
    }

    fun addHaving(condition: String, bind: Array<Any?>) {
        if (condition != null && condition.isNotEmpty())  {
            this.conditionHaving += " $condition"
            for (item in bind) {
                this.bindHaving = this.bindHaving.plusElement(item)
            }
            this.hasConditionHaving = true
        }
    }
    
    fun addGroupByFunction(fieldId: String) {
        this.groupBy = this.groupBy.plus(fieldId)
    }

    private fun checkField(fieldId: String, table: String = tableName): Boolean {

        return fieldId.isNotEmpty() && (table == tableName || joinTable.containsKey(table)) && columnTables[table]!!.containsKey(fieldId)
        /*(if (joinField) */ /* else columnTable.containsKey(fieldId)|| fieldsTable[table]!!.contains(fieldId))*/
    }

    fun countTotal(): Long {
        var sql = "SELECT COUNT(*) AS total FROM ${this.tableName}"
        if (joinTable.isNotEmpty()) {
            for ((key, value) in joinTable) {
                sql += " ${joinTableSide[key]} JOIN $key ON $value "
            }
        }
        if (this.hasCondition && this.condition.isNotEmpty()) {
            sql += " WHERE ${this.condition}"
        }
        val result = jdbc.queryForList(sql, *bind)
        return result[0]["total"].toString().toLong()
    }

    fun getDataPage(customFieldEnabled: Boolean = false): MutableList<MutableMap<String, Any?>> {
        var fieldV = ""

        if (customFieldEnabled) {
            this.customFields()
        }

        for ((key, value) in fieldsTable) {
            value.forEach { a ->
                if (fieldV.isNotEmpty()) {
                    fieldV += ", "
                }
                fieldV += "$key.$a"
             }
        }

        if (this.fields.isNotEmpty()) {
            if (fieldV.isNotEmpty()) {
                fieldV += ", "
            }
            fieldV += this.fields.joinToString(", ")
        }

        var sql = "SELECT $fieldV FROM ${this.tableName} "
        if (joinTable.isNotEmpty()) {
            for ((key, value) in joinTable) {
                sql += "${joinTableSide[key]} JOIN $key ON $value "
            }
        }

        if (this.hasCondition && this.condition.isNotEmpty()) {
            sql += "WHERE ${this.condition} "
        }

        if (this.groupBy.isNotEmpty()) {
            sql += "GROUP BY ${this.groupBy.joinToString(", ")} "
        }

        if (this.hasConditionHaving && this.conditionHaving.isNotEmpty()) {
            sql += "HAVING ${this.conditionHaving} "
            for (item in bindHaving) {
                bind = bind.plusElement(item)
            }
        }

        if (this.order.isNotEmpty()) {
            sql += "ORDER BY ${this.order.joinToString(", ")} "
        }

        if (this.pageNumber != null) {
            if (this.pageNumber!!.pageSize > 0) {
                sql += "LIMIT ${pageNumber!!.pageSize} "
            }
            sql += "OFFSET ${pageNumber!!.offset} "
        }
        var result = jdbc.queryForList(sql, *bind)
        result = this.unserializedToString(result, customFieldEnabled)
        return result
    }

    private fun getColumnFromTable(table: String = tableName) {
        var columns = jdbc.queryForList("SELECT column_name, data_type FROM information_schema.columns " +
                "WHERE table_name = ?", table)
        for (item in columns) {
            columnTables[table]!![item["column_name"].toString()] = item["data_type"].toString().toLowerCase()
        }
    }

    private fun unserializedToString(
        result: MutableList<MutableMap<String, Any?>>,
        customFieldEnabled: Boolean
    ): MutableList<MutableMap<String, Any?>> {
        val refFieldInit: MutableMap<String, Field> = mutableMapOf()
        for (item in refFields) {
            refFieldInit[item.key] = FieldFactory.create(item.key, item.value as MutableMap<String, Any?>)
        }
        for (item in result) {
            for (childRes in item) {
                if (refFieldInit[childRes.key] != null) {
                    refFieldInit[childRes.key]!!.unserialized(childRes.value)
                    when (refFieldInit[childRes.key]!!.getConfig()["type"]) {
                        "enumeration" -> {
                            if (
                                refFieldInit[childRes.key]!!.getConfig()["isBadges"] !== null
                                && refFieldInit[childRes.key]!!.getConfig()["isBadges"] as Boolean
                            ) {
                                childRes.setValue(refFieldInit[childRes.key]!!.getValue())
                            } else {
                                childRes.setValue(refFieldInit[childRes.key]!!.unserializedToString())
                            }
                        }
                        "image" -> {
                            childRes.setValue(refFieldInit[childRes.key]!!.getValue())
                        }
                        else -> {
                            childRes.setValue(refFieldInit[childRes.key]!!.unserializedToString())
                        }
                    }

                } else {
                    if (customFieldEnabled) {
                        when (childRes.key) {
                            "creator", "editor" -> {
                                val fieldF = FieldFactory.create(childRes.key, mutableMapOf("type" to "structure"))
                                fieldF.unserialized(childRes.value)
                                childRes.setValue(fieldF.unserializedToString())
                            }
                        }
                    }
                }
            }
        }
        return result
    }

}
