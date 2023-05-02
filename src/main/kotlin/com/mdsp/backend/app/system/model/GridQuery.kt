package com.mdsp.backend.app.system.model

import com.mdsp.backend.app.reference.model.fields.Field
import com.mdsp.backend.app.reference.model.fields.FieldFactory
import com.mdsp.backend.app.system.config.DataSourceConfiguration
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.math.floor

open class GridQuery {

    private var tableName: String = ""
    private var condition: String = ""
    private var order: Array<String> = arrayOf()
    private var bind: Array<Any?> = arrayOf()
    private var hasCondition: Boolean = false
    private var pageNumber: PageRequest? = null
    private var fields: Array<String> = arrayOf("id")
    private var refFields: MutableMap<String, Any?>  = mutableMapOf()
    private var columnTable: MutableMap<String, Any?> = mutableMapOf()
    private var removed = false

    private lateinit var jdbc: JdbcTemplate

    private lateinit var dataSourceConfig: DataSourceConfiguration

    constructor(tableNameD: String, dataSourceConfiguration: DataSourceConfiguration) {
        tableName = tableNameD
        dataSourceConfig = dataSourceConfiguration
        jdbc = JdbcTemplate(dataSourceConfig.dataBaseOneTemplate)
        this.getColumnFromTable()
    }

    fun setRefFields(refFields: MutableMap<String, Any?>) {
        this.refFields = refFields
    }

    fun setPageRequest(pageNumber: PageRequest) {
        this.pageNumber = pageNumber
    }

    fun getTableName() = tableName
    fun setTableName(tableNameD: String) {
        tableName = tableNameD
    }

    private fun customFields() {
        addColumn("created_at")
        addColumn("updated_at")
        addColumn("creator")
        addColumn("editor")
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

    fun setColumnEmpty() {
        this.fields = arrayOf()
    }

    fun addColumn(column: String) {
//        if (this.columnTable.isNotEmpty()) {
//            this.fields = this.fields.plus(column)
//        } else {
//            this.fields = this.fields.plus(column)
//        }
        this.fields = this.fields.plus(column)

    }

    fun addOrder(fieldId: String, type: String) {
        if (
            fieldId.isNotEmpty()
            &&
            (type.toLowerCase() == "asc" || type.toLowerCase() == "desc" || type.isEmpty())
        )  {
            this.order = this.order.plus("$fieldId ${type.toUpperCase()}")
        }
    }


    open fun countTotal(): Long {
        var sql = "SELECT COUNT(*) AS total FROM ${this.tableName}"
        if (this.hasCondition && this.condition.isNotEmpty()) {
            sql += " WHERE ${this.condition}"
        }
        val result = jdbc.queryForList(sql, *bind)
        return result[0]["total"].toString().toLong()
    }

    open fun getDataPage(customFieldEnabled: Boolean = false): MutableList<MutableMap<String, Any?>> {
        var fieldV = ""
        if (customFieldEnabled) {
            this.customFields()
        }
        if (this.fields.isNotEmpty()) {
            fieldV = this.fields.joinToString(", ")
        }

        var sql = "SELECT $fieldV  FROM ${this.tableName} "
        if (this.hasCondition && this.condition.isNotEmpty()) {
            sql += "WHERE ${this.condition} "
        }
        // TODO ORDER BY
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

    private fun getColumnFromTable() {
        var columns = jdbc.queryForList("SELECT column_name, data_type FROM information_schema.columns " +
                "WHERE table_name = ?", this.tableName)
        for (item in columns) {
            columnTable[item["column_name"].toString()] = item["data_type"].toString().toLowerCase()
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
