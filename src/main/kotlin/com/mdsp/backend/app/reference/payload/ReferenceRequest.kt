package com.mdsp.backend.app.reference.payload

import java.util.*

class ReferenceRequest {
    private var id: UUID? = null
    private var title: String? = null
    private var hint: String? = null
    private var description: String? = null
    private var tableName: String? = null
    private var userFields: String? = null
    private var sysFields: String? = null
    private var isSystem: Int = 0

    constructor() {}

    fun getId() = this.id
    fun getTitle() = this.title
    fun getHint() = this.hint
    fun getDescription() = this.description
    fun getTableName() = this.tableName
    fun getUserFields() = this.userFields
    fun getSysFields() = this.sysFields
    fun getIsSystem() = this.isSystem

    fun setId(id: UUID?) { this.id = id }
    fun setTitle(title: String?) { this.title = title }
    fun setHint(hint: String?) { this.hint = hint }
    fun setDescription(description: String?) { this.description = description }
    fun setTableName(tableName: String?) { this.tableName = tableName }
    fun setUserFields(userFields: String?) { this.userFields = userFields }
    fun setSysFields(sysFields: String?) { this.sysFields = sysFields }
    fun setIsSystem(isSystem: Int) { this.isSystem = isSystem }
}
