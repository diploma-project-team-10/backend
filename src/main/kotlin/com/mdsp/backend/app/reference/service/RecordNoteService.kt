package com.mdsp.backend.app.reference.service

import com.mdsp.backend.app.reference.model.RefRecord
import com.mdsp.backend.app.reference.model.Reference
import com.mdsp.backend.app.reference.repository.IReferenceRepository
import com.mdsp.backend.app.system.config.DataSourceConfiguration
import com.mdsp.backend.app.system.model.EngineQuery
import com.mdsp.backend.app.system.model.GridQuery
import com.mdsp.backend.app.system.model.Util
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.lang.Exception
import java.util.*

@Service
class RecordNoteService {

    @Autowired
    private lateinit var dataSourceConfig: DataSourceConfiguration

    @Autowired
    private lateinit var referenceRepository: IReferenceRepository

    fun getRecordById(id: UUID, reference: Reference, type: String = "view", fields: Array<String> = arrayOf()): MutableMap<String, Any?> {
        var result: MutableMap<String, Any?> = mutableMapOf()
        val record = RefRecord(id, reference, dataSourceConfig)
        if (record.isExisted()) {
            record.load()
            if (type == "edit") {
                result = record.getDataField()
                for (item in getPasswordFields(reference)) {
                    if (result.containsKey(item)) {
                        result[item] = null
                    }
                }
            } else {
                result = record.viewData(fields)
            }
        }
        return result
    }

    fun getPasswordFields(reference: Reference): ArrayList<String> {
        val passwords: ArrayList<String> = arrayListOf()
        for ((key, item) in reference.getAllFields()) {
            if ((item as MutableMap<String, Any?>)["type"] == "password") {
                passwords.add(key)
            }
        }
        return passwords
    }

    fun getRecordByValue(
        referenceId: UUID,
        value: String,
        fields: String,
        template: String?,
        conditionExtra: MutableMap<String, Any?> = mutableMapOf()
    ): MutableList<MutableMap<String, Any?>> {
        val pagePR: PageRequest = PageRequest.of(0, 10)
        val reference = referenceRepository.findByIdAndDeletedAtIsNull(referenceId)
        val result: MutableList<MutableMap<String, Any?>> = arrayListOf()
        if (reference.isPresent) {
            val allField = Util.mergeMutableMap(Util.toJson(reference.get().getUserFields()), Util.toJson(reference.get().getSysFields()))
            val grid = GridQuery(reference.get().getTableName(), dataSourceConfig)
            grid.setPageRequest(pagePR)
            grid.setRefFields(allField)
            grid.addCondition("deleted_at IS NULL", arrayOf())
            var condition = ""
            var valueBind: Array<Any?> = arrayOf()
            var column: Array<String> = arrayOf()
            for ((key, item) in fields.split(",").withIndex()) {
                if (key == 0) {
                    condition += " lower(${EngineQuery.castColumn(item, (allField[item] as MutableMap<String, Any?>)["type"].toString())}) LIKE lower(?)"
                    column = column.plusElement(item)
                } else {
                    condition += " OR lower(${EngineQuery.castColumn(item, (allField[item] as MutableMap<String, Any?>)["type"].toString())}) LIKE lower(?)"
                    column = column.plusElement("' = '")
                    column = column.plusElement(item)
                }
                valueBind = valueBind.plusElement("%${value.toLowerCase()}%")
                grid.addColumn(item)
            }
            grid.addColumn("CONCAT(${column.joinToString(", ")}) AS value")
            grid.addCondition(" AND ($condition)", valueBind)
            if (!conditionExtra.isNullOrEmpty() && conditionExtra["condition"].toString().isNotEmpty()) {
                grid.addCondition(" AND (${conditionExtra["condition"]})", conditionExtra["value"] as Array<Any?>)
            }
            result.addAll(grid.getDataPage(false))

            var rTemplate = template
            for ((key, item) in allField) {
                rTemplate = rTemplate!!.replace("{${(item as MutableMap<String, Any?>)["title"]}}", "{$key}")
            }

            for ((key, item) in result.withIndex()) {
                var vTemplate = rTemplate!!
                for ((key2, item2) in item) {
                    var sItem = item2
                    if (sItem == null) {
                        sItem = ""
                    }
                    vTemplate = vTemplate.replace("{$key2}", sItem.toString())
                }
                result[key]["value"] = vTemplate.trim()
            }
        }
        return result
    }

    fun createTableNote(newReference: Reference): String? {
        var result: String? = ""
        val engineQuery = EngineQuery(newReference.getTableName(), dataSourceConfig)
        try {
            engineQuery.create("id")
            engineQuery.createColumn("title", "string")

            engineQuery.createColumn("created_at", "timestamp", "DEFAULT CURRENT_TIMESTAMP")
            engineQuery.createColumn("updated_at", "timestamp", "")
            engineQuery.createColumn("deleted_at", "timestamp", "")
            engineQuery.createColumn("creator", "structure", "")
            engineQuery.createColumn("editor", "structure", "")

            result = "OK"
        }
        catch (e: Exception) {
            result = e.message
            engineQuery.drop()
        }
        return result
    }
}
