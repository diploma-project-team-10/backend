package com.mdsp.backend.app.reference.payload

import com.mdsp.backend.app.reference.model.Section
import java.util.*

class SectionRequest {
    private var id: UUID? = null
    private var title: String? = null
    private var hint: String? = null
    private var fields: String? = null
    private var groupField: String? = null
    private var sortField: String? = null
    private var filterField: String? = null
    private var access: Array<Array<String>> = arrayOf()
    private var enableCustomFields: Boolean = false
    private var referenceId: UUID? = null

    constructor() {}

    fun getId() = this.id
    fun getTitle() = this.title
    fun getHint() = this.hint
    fun getFields() = this.fields
    fun getGroupField() = this.groupField
    fun getSortField() = this.sortField
    fun getFilterField() = this.filterField
    fun getAccess() = this.access
    fun getEnableCustomFields() = this.enableCustomFields
    fun getReferenceId() = this.referenceId

    fun setId(id: UUID?) { this.id = id }
    fun setTitle(title: String?) { this.title = title }
    fun setHint(hint: String?) { this.hint = hint }
    fun setFields(fields: String?) { this.fields = fields }
    fun setGroupField(groupField: String?) { this.groupField = groupField }
    fun setSortField(sortField: String?) { this.sortField = sortField }
    fun setFilterField(filterField: String?) { this.filterField = filterField }
    fun setAccess(access: Array<MutableMap<String, String?>>) {
        var ids: Array<String> = arrayOf()
        var names: Array<String> = arrayOf()
        for (item in access) {
            ids = ids.plus(item["id"].toString())
            names = names.plus(item["value"].toString())
        }
        this.access = arrayOf(ids, names)
    }
    fun setEnableCustomFields(enableCustomFields: Boolean) { this.enableCustomFields = enableCustomFields }
    fun setReferenceId(referenceId: UUID?) { this.referenceId = referenceId }
}
