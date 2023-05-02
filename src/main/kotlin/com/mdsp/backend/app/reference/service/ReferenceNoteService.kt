package com.mdsp.backend.app.reference.service

import com.mdsp.backend.app.reference.model.Reference
import com.mdsp.backend.app.reference.repository.IReferenceRepository
import com.mdsp.backend.app.reference.repository.ISectionRepository
import com.mdsp.backend.app.system.config.DataSourceConfiguration
import com.mdsp.backend.app.system.model.EngineQuery
import com.mdsp.backend.app.system.model.Util
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

@Service
class ReferenceNoteService {

    @Autowired
    private lateinit var dataSourceConfig: DataSourceConfiguration

    @Autowired
    lateinit var sectionRepository: ISectionRepository

    @Autowired
    lateinit var referenceRepository: IReferenceRepository

    fun createTableNote(newReference: Reference): String? {
        var result: String? = ""
        val engineQuery = EngineQuery(newReference.getTableName(), dataSourceConfig)
        try {
            engineQuery.create("id")

            engineQuery.createColumn("created_at", "timestamp", "DEFAULT CURRENT_TIMESTAMP")
            engineQuery.createColumn("updated_at", "timestamp", "")
            engineQuery.createColumn("deleted_at", "timestamp", "")
            engineQuery.createColumn("creator", "structure", "")
            engineQuery.createColumn("editor", "structure", "")
            engineQuery.createColumn("owner", "uuid")

            result = "OK"
        }
        catch (e: Exception) {
            result = e.message
            engineQuery.drop()
        }
        return result
    }

    fun fixSortOrders(fields: MutableMap<String, MutableMap<String, Any?>>, fixIndex: Int) {
        for (item in fields) {
            var orderNum = item.value?.get("orderNum") as Int
            if (orderNum > fixIndex) {
                item.value?.set("orderNum", orderNum - 1)
            }
        }
    }

    fun createFieldTable(reference: Reference, fieldId: String, field: MutableMap<String, Any?>): String? {
        var result: String? = ""
        val engineQuery = EngineQuery(reference.getTableName(), dataSourceConfig)
        try {
            var dValue = field["defaultValue"]
            when (field["type"]) {
                "integer" -> {}
                "string", "date", "timestamp" -> {
                    dValue = if (dValue != null && dValue.toString().isNotEmpty()) {
                        "'${dValue}'"
                    } else {
                        null
                    }
                }
                "enumeration", "reference", "structure", "image", "file" -> {
                    dValue = "'${Util.getMutableToArraySerialize(dValue as ArrayList<MutableMap<String, Any?>>)}'"
                }
            }

            var defaultVal: String? = null
            if (field["defaultValue"] != null) {
                defaultVal = "DEFAULT $dValue"
            }

            engineQuery.createColumn(fieldId, field["type"] as String, defaultVal)
            result = "OK"
        }
        catch (e: Exception) {
            result = e.message
        }
        return result
    }

    fun removeFieldTable(reference: Reference, fieldId: String): String? {
        val engineQuery = EngineQuery(reference.getTableName(), dataSourceConfig)
        var result = try {
            engineQuery.dropColumn(fieldId)
            "OK"
        } catch (e: Exception) {
            e.message
        }
        return result
    }

    fun changeFieldIdTable(reference: Reference, fieldOldId: String, fieldNewId: String): String? {
        var result: String? = ""
        val engineQuery = EngineQuery(reference.getTableName(), dataSourceConfig)
        try {
            engineQuery.renameColumn(fieldOldId, fieldNewId)
            result = "OK"
        }
        catch (e: Exception) {
            result = e.message
        }
        return result
    }

    fun changeFieldDefaultTable(reference: Reference, fieldId: String, defaultValue: Any?, type: String): String? {
        var result: String? = ""
        val engineQuery = EngineQuery(reference.getTableName(), dataSourceConfig)
        try {
            var dValue = defaultValue
            when (type) {
                "integer" -> {}
                "string", "date", "timestamp" -> {
                    dValue = if (dValue != null && dValue.toString().isNotEmpty()) {
                        "'${defaultValue}'"
                    } else {
                        null
                    }
                }
                "enumeration", "reference", "structure", "image", "file" -> {
                    dValue = "'${Util.getMutableToArraySerialize(dValue as ArrayList<MutableMap<String, Any?>>)}'"
                }
                "text" -> {
                    dValue = null
                }
            }
            engineQuery.setDefaultValue(fieldId, dValue)
            result = "OK"
        }
        catch (e: Exception) {
            result = e.message
        }
        return result
    }

    fun getReferenceFields(referenceId: UUID, sectionId: UUID? = null): MutableList<MutableMap<String, Any?>>  {
        val reference = referenceRepository.findByIdAndDeletedAtIsNull(referenceId)
        if (reference.isPresent) {
            val allField = reference.get().getAllFields()
            var headerTable: MutableList<MutableMap<String, Any?>> = mutableListOf()
            if (sectionId != null) {
                val section = sectionRepository.findByIdAndDeletedAtIsNull(sectionId)
                if (!section.isPresent) {
                    return arrayListOf()
                }
                headerTable = mutableListOf()
                for (headerField in section.get().getFields()) {
                    (allField[headerField["id"]] as MutableMap<String, Any?>)["id"] = headerField["id"]
                    headerTable.add(allField[headerField["id"]] as MutableMap<String, Any?>)
                }
                return headerTable
            }
            for ((key, headerField) in allField) {
                val item: MutableMap<String, Any?> = headerField as MutableMap<String, Any?>
                item["id"] = key
                headerTable.add(item)
            }
            return headerTable
        }
        return mutableListOf()
    }

    companion object {
        fun isSystemReference(id: UUID): Boolean {
            val systemReference: java.util.ArrayList<UUID> = arrayListOf(
                UUID.fromString("00000000-0000-0000-0000-000000000017")
            )
            return systemReference.contains(id)
        }
    }
}
