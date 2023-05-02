package com.mdsp.backend.app.reference.model

import com.mdsp.backend.app.reference.model.fields.Field
import com.mdsp.backend.app.reference.model.fields.FieldFactory
import com.mdsp.backend.app.system.config.DataSourceConfiguration
import com.mdsp.backend.app.system.model.ActiveJpa
import com.mdsp.backend.app.system.model.Util
import java.sql.Timestamp
import java.util.*

class RefRecord: ActiveJpa {
    private var reference: Reference
    private var userFields: MutableMap<String, Field> = mutableMapOf()
    private var sysFields: MutableMap<String, Field> = mutableMapOf()
    private var fieldsError: String = ""
    private var errorText: String = ""
    private var removeType = false

    private var createdAt: Timestamp = Timestamp(System.currentTimeMillis())
    private var updatedAt: Timestamp? = null
    private var deletedAt: Timestamp? = null
    private var creator: Array<Array<String>>? = null
    private var editor: Array<Array<String>>? = null
    private var owner: UUID? = null

    constructor(
        id: UUID?,
        reference: Reference,
        dataSourceConfiguration: DataSourceConfiguration
    ) : super(reference.getTableName(), dataSourceConfiguration) {
        this.id = id
        this.reference = reference
        this.initFields()
        this.data["id"] = this.id
    }

    fun getRecordId() = this.id

    fun getRefId() = reference.getId()

    fun getUserFields() = userFields

    fun getSysFields() = sysFields

    private fun initFields() {
        for (item in Util.toJson(reference.getUserFields())) {
            userFields[item.key] = FieldFactory.create(item.key, item.value as MutableMap<String, Any?>)
        }
        for (item in Util.toJson(reference.getSysFields())) {
            sysFields[item.key] = FieldFactory.create(item.key, item.value as MutableMap<String, Any?>)
        }

        setFields("created_at", "Created", "timestamp")
        setFields("updated_at", "Updated", "timestamp")
        setFields("deleted_at", "Deleted", "timestamp")

        setFields("creator", "Creator", "structure")
        setFields("editor", "Editor", "structure")
        setFields("owner", "Host", "uuid")
    }

    fun setDataField(data: MutableMap<String, Any?>) {
        for (item in userFields) {
            if (data.containsKey(item.key)) {
                item.value.setValue(data[item.key])
            } else {
                item.value.setValue(item.value.getValue())
            }
            this.data[item.key] = item.value.serialized()
        }
        for (item in sysFields) {
            if (arrayOf("creator", "editor", "owner").contains(item.key)) {
                continue
            }
            if (data.containsKey(item.key)) {
                item.value.setValue(data[item.key])
            } else {
                item.value.setValue(item.value.getValue())
            }
            this.data[item.key] = item.value.serialized()
        }
    }

    override fun newRecord() {
        super.newRecord()
        createdAt = Timestamp(System.currentTimeMillis())
        updatedAt = null
        deletedAt = null
        editor = null

        data["created_at"] = createdAt
        data["updated_at"] = updatedAt
        data["deleted_at"] = deletedAt
        data["editor"] = editor
    }

    fun setCreator(user: Array<Array<String>>?) {
        creator = user
        data["creator"] = creator
        if (user != null && user.size > 1 && user[0].isNotEmpty()) {
            data["owner"] = UUID.fromString(user[0][0])
        }
    }

    fun setEditor(user: Array<Array<String>>?) {
        editor = user
        data["editor"] = editor
    }

    override fun isExisted(): Boolean {
        if (this.id != null) {
            return super.isExisted()
        }
        return false;
    }

    override fun save() {
        data.remove("created_at")
        if (isExist && !removeType) {
            data.remove("creator")
            data.remove("deleted_at")
            data.remove("owner")
            data["updated_at"] = Timestamp(System.currentTimeMillis())
        } else {
            data["created_at"] = Timestamp(System.currentTimeMillis())
        }
        super.save()
    }

    override fun load() {
        super.load()
        this.fieldsData(userFields)
        this.fieldsData(sysFields)
        this.removeExcessFields()
        data.remove("creator")
        data.remove("editor")
    }

    fun isValid(): Boolean {
        var isValid = true
        errorText = ""
        for (item in getAllFields()) {
            if (item.value.getError().isNotEmpty()) {
                errorText += item.value.getError()
                isValid = false
            }
        }
        return isValid
    }

    fun getErrorText(): String {
        return errorText
    }

    private fun fieldsData(urFields: MutableMap<String, Field>) {
        for (item in urFields) {
            item.value.unserialized(this.data[item.key])
            this.data[item.key] = item.value.getValue()
        }
    }

    fun viewData(fields: Array<String> = arrayOf()): MutableMap<String, Any?> {
        val result: MutableMap<String, Any?> = mutableMapOf()
        for (item in getAllFields()) {
            if (fields.isNotEmpty() && !fields.contains(item.key)) {
                continue
            }
            if (
                arrayListOf("structure", "image", "file").contains(item.value.getConfig()["type"])
                ||
                    (
                        item.value.getConfig()["type"] == "enumeration"
                        && item.value.getConfig()["isBadges"] !== null
                        && item.value.getConfig()["isBadges"] as Boolean
                    )
            ) {
                result[item.key] = item.value.getValue()
            } else {
                result[item.key] = item.value.unserializedToString()
            }
        }
        return result
    }

    fun getAllFields(): MutableMap<String, Field> {
        var result: MutableMap<String, Field> = mutableMapOf()
        result.putAll(userFields)
        result.putAll(sysFields)
        return result
    }

    fun setFields(name: String, title: String, type: String) {
        val config: MutableMap<String, Any?> = mutableMapOf("type" to type, "title" to title)
        sysFields[name] = FieldFactory.create(name, config)
        sysFields[name]!!.newField()
    }

    fun removeRecord() {
        data["deleted_at"] = Timestamp(System.currentTimeMillis())
        removeType = true
    }

    fun removeKeyData(key: String) {
        data.remove(key)
    }

    fun isDeleted(): Boolean {
        return data["deleted_at"] != null
    }

    private fun removeExcessFields() {
        val dataExtra: MutableMap<String, Any?> = mutableMapOf()
        for ((key, item) in data) {
            if (
                userFields.containsKey(key)
                || sysFields.containsKey(key)
                || arrayOf("id", "created_at", "creator", "deleted_at", "editor", "updated_at").contains(key)
            ) {
                dataExtra[key] = item
            }
        }
        data = dataExtra
    }

}
