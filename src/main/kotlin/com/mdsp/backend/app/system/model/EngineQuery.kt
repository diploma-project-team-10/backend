package com.mdsp.backend.app.system.model

import com.mdsp.backend.app.system.config.DataSourceConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource

class EngineQuery {

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

    fun create(primaryKey: String, type: String = "uuid") {
        jdbc.execute(
        "CREATE TABLE $tableName ($primaryKey $type NOT NULL PRIMARY KEY)"
        )
    }

    fun createColumn(name: String, type: String, defaultValue: Any? = null) {
        jdbc.execute(
                "ALTER TABLE $tableName ADD COLUMN $name ${dataTypes[type].toString()} ${defaultValue.toString()}"
        )
    }

    fun drop() {
        jdbc.execute(
            "DROP TABLE IF EXISTS $tableName"
        )
    }

    fun dropColumn(name: String) {
        jdbc.execute(
            "ALTER TABLE $tableName DROP COLUMN $name"
        )
    }

    fun renameColumn(oldFieldName: String, newFieldName: String) {
        jdbc.execute(
            "ALTER TABLE $tableName RENAME COLUMN $oldFieldName TO $newFieldName"
        )
    }

    fun isColumnNotExisted(name: String): Int {
        return jdbc.queryForObject(
            "SELECT count(*) AS total FROM information_schema.columns " +
                    "WHERE table_name = ? AND column_name = ?",
            arrayOf(tableName, name), Integer::class.java
        ).toInt()
    }

    fun setDefaultValue(name: String, defaultValue: Any? = null) {
        jdbc.execute(
            "ALTER TABLE $tableName ALTER COLUMN $name SET DEFAULT ${defaultValue.toString()}"
        )
    }

    fun setColumnNotNull(name: String) {
        jdbc.execute(
            "ALTER TABLE $tableName ALTER COLUMN $name SET NOT NULL"
        )
    }

    fun dropColumnNotNull(name: String) {
        jdbc.execute(
            "ALTER TABLE $tableName ALTER COLUMN $name DROP NOT NULL"
        )
    }

    fun setColumnUnique(name: String) {
        jdbc.execute(
            "ALTER TABLE $tableName ADD CONSTRAINT ${name}_unique UNIQUE ($name)"
        )
    }

    fun dropColumnUnique(name: String) {
        jdbc.execute(
            "ALTER TABLE $tableName DROP CONSTRAINT IF EXISTS ${name}_unique"
        )
    }


    private val dataTypes: MutableMap<String, String> = mutableMapOf<String, String>(
        "uuid"		    to "uuid",
        "integer"       to "bigint",
        "float"         to "double precision",
        "string"        to "character varying",
        "text"          to "text",
        "timestamp"     to "timestamp(0) without time zone",
        "date"          to "date",
        "reference"     to "character varying(256)[][]",
        "structure"     to "character varying(256)[][]",
        "boolean"       to "boolean",
        "enumeration"   to "character varying(256)[][]",
        "file"          to "character varying(256)[][]",
        "image"	        to "character varying(256)[][]",
        "password"      to "character varying",
        "table"		    to "text",
        "serial"	    to "bigserial",
        "array"         to "character varying[]",
        "tsvector"      to "tsvector",
        "jsonb"         to "jsonb"
    )

    companion object {
        fun castColumn(fieldName: String, fieldType: String, rowSeparator: String = ""): String {
            when (fieldType) {
                "reference",
                "structure",
                "file",
                "image",
                "enumeration",
                "integer",
                "float",
                "timestamp",
                "boolean",
                "date" -> {
                    return "$fieldName::character varying"
                }
                "uuid" -> {
                    return "$fieldName::text"
                }
                "table",
                "password",
                "text",
                "string" -> {
                    return fieldName
                }
                else -> {
                    return fieldName
                }
            }
        }

//    public function castColumn($sFieldName, $sFieldType, $sRowSeparator = '')
//    {
//        switch($sFieldType) {
//
//        case 'table':
//        case 'password':
//        case 'text':
//        case 'string':
//        default:
//        return $sFieldName;
//    }
//    }
//
//    public function castValue($sValue, $sType)
//    {
//        switch($sType)
//        {
//            default:
//            return "'" . $sValue . "'::character varying";
//        }
//
//    }
//
//    public function castFieldTo($sPreparedName, $sType)
//    {
//        switch($sType)
//        {
//            case 'string':
//            return $sPreparedName . '::character varying';
//
//            case 'uuid':
//            return $sPreparedName . '::uuid';
//
//            case 'date':
//            return $sPreparedName . '::date';
//
//            default:
//            return $sPreparedName;
//        }
//
//    }
    }

}
