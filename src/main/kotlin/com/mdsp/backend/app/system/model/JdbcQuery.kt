package com.mdsp.backend.app.system.model

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class JdbcQuery {

    @Autowired
    private lateinit var jdbc: JdbcTemplate

    var tableName: String = ""
    private var condition: String = ""
    private var order: Array<String> = arrayOf()
    private var bind: Array<Any?> = arrayOf()
    private var hasCondition: Boolean = false
    private var fields: Array<String> = arrayOf("id")

    fun clearCondition() {
        hasCondition = false
        condition = ""
        bind = arrayOf()
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

    fun clearOrder() {
        order = arrayOf()
    }

    fun addOrder(fieldId: String, type: String) {
        if (
            fieldId.isNotEmpty()
            &&
            (type.lowercase() == "asc" || type.lowercase() == "desc" || type.isEmpty())
        )  {
            this.order = this.order.plus("$fieldId ${type.toUpperCase()}")
        }
    }

    fun clearColumn() {
        this.fields = arrayOf()
    }

    fun addColumn(column: String) {
        this.fields = this.fields.plus(column)

    }

    fun countTotal(): Long {
        var sql = "SELECT COUNT(*) AS total FROM ${this.tableName}"
        if (this.hasCondition && this.condition.isNotEmpty()) {
            sql += " WHERE ${this.condition}"
        }
        val result = jdbc.queryForList(sql, *bind)
        return result[0]["total"].toString().toLong()
    }

    fun getAverage(field: String, conditionExtra: String? = null, bindExtra: Array<Any?> = arrayOf()): Double? {
        var sql = "SELECT AVG($field) AS total FROM ${this.tableName}"
        var bindClone = bind.clone()
        if (this.hasCondition && this.condition.isNotEmpty()) {
            sql += " WHERE ${this.condition}"
        }
        if (conditionExtra != null) {
            sql += conditionExtra
            for (item in bindExtra) {
                bindClone = bindClone.plusElement(item)
            }
        }

        val result = jdbc.queryForList(sql, *bindClone)
        return result[0]["total"].toString().toDoubleOrNull()
    }

    fun getDataPage(conditionExtra: String? = null, bindExtra: Array<Any?> = arrayOf(), pageNumber: PageRequest? = null): MutableList<MutableMap<String, Any?>> {
        var fieldV = ""
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

        if (pageNumber != null) {
            if (pageNumber.pageSize > 0) {
                sql += "LIMIT ${pageNumber.pageSize} "
            }
            sql += "OFFSET ${pageNumber.offset} "
        }
        var result = jdbc.queryForList(sql, *bind)
        return result
    }
}
