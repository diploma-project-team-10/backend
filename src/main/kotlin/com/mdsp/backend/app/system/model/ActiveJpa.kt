package com.mdsp.backend.app.system.model

import com.mdsp.backend.app.system.config.DataSourceConfiguration
import org.springframework.jdbc.core.JdbcTemplate
import java.util.*

open class ActiveJpa {

    protected var id: UUID? = null
    protected var data: MutableMap<String, Any?> = mutableMapOf()
    protected var isExist: Boolean = false
    private var tableName: String = ""

    private lateinit var jdbc: JdbcTemplate

    private lateinit var dataSourceConfig: DataSourceConfiguration

    constructor(tableNameD: String, dataSourceConfiguration: DataSourceConfiguration) {
        tableName = tableNameD
        dataSourceConfig = dataSourceConfiguration
        jdbc = JdbcTemplate(dataSourceConfig.dataBaseOneTemplate)
    }

    fun getTableName() = tableName
    fun setTableName(tableNameD: String) {
        tableName = tableNameD
    }

    fun getDataField() = this.data

    open fun newRecord() {
//        if (this.id === null) {
        this.id = UUID.randomUUID()
        this.data["id"] = this.id
//        }
    }

    open fun isExisted(): Boolean {
        if (this.id != null) {
            val result = jdbc.queryForList(
                "SELECT COUNT(*) AS total FROM ${this.tableName} WHERE id = ?",
                this.id
            )
            if (result[0]["total"].toString().toLong() > 0) {
                isExist = true
                return true
            }
        }
        isExist = false
        return false
    }

    open fun save() {
        if (isExist) {
            update()
        } else {
            insert()
        }
    }

    fun insert() {
        var key: Array<String> = arrayOf()
        var value: Array<Any?> = arrayOf()
        var valueQuestion: Array<Any?> = arrayOf()

        for (item in this.data) {
            key = key.plus(item.key)
            value = value.plus(item.value)
            valueQuestion = valueQuestion.plus("?")
        }

        val result = jdbc.update("INSERT INTO ${this.tableName} (${key.joinToString(", ")}) " +
                "VALUES (${valueQuestion.joinToString(", ")})", *value)
        if (result > 0) {}
    }

    fun update() {
        var key: Array<String> = arrayOf()
        var value: Array<Any?> = arrayOf()
        var valueQuestion: Array<Any?> = arrayOf()

        for (item in this.data) {
            if (item.key == "id") {
                continue
            }
            key = key.plus(item.key)
            value = value.plus(item.value)
            valueQuestion = valueQuestion.plus("${item.key} = ?")
        }
        val result = jdbc.update("UPDATE ${this.tableName} SET ${valueQuestion.joinToString(", ")} " +
                "WHERE id = ?", *value, this.id)
        if (result > 0) {}
    }

    open fun load() {
        this.data = this.dbLoad()
    }

    open fun dbLoad(): MutableMap<String, Any?>  {
        return jdbc.queryForMap(
            "SELECT * FROM ${this.tableName} WHERE id = ?",
            this.id
        )
    }

//    private function dbLoad() {
//        return $this->oDb->add('SELECT')
//        ->add('*')
//        ->add('FROM')
//        ->add($this->sTable)
//        ->add('WHERE')
//        ->add('"' . $this->sPK . '"')
//        ->add(' = ?')
//        ->execute(array($this[$this->sPK]))
//        ->fetch();
//    }

}
