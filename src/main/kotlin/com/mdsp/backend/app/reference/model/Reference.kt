package com.mdsp.backend.app.reference.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.mdsp.backend.app.system.model.DateAudit
import com.mdsp.backend.app.system.model.Util
import com.vladmihalcea.hibernate.type.array.StringArrayType
import com.vladmihalcea.hibernate.type.array.UUIDArrayType
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.*

@Entity
@Table(
    name = "reference",
    uniqueConstraints = [
        UniqueConstraint(columnNames = arrayOf("table_name"))
    ]
)
@TypeDefs(
    TypeDef(
        name = "string-array",
        typeClass = StringArrayType::class
    ),
    TypeDef(
        name = "uuid-array",
        typeClass = UUIDArrayType::class
    ),
    TypeDef(
        name = "jsonb",
        typeClass = JsonBinaryType::class
    )
)
@JsonIgnoreProperties(value = ["tableName"], allowGetters = false)
class Reference(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private var id: UUID?
) : DateAudit() {

    @Column(name = "title")
    private var title: String? = null

    @Column(name = "hint")
    private var hint: String? = null

    @Column(
        name = "description",
        columnDefinition = "text"
    )
    private var description: String? = null

    @Column(name = "table_name")
    private var tableName: String = ""

    @Type(type = "jsonb")
    @Column(
        name = "user_fields",
        columnDefinition = "jsonb"
    )
    private var userFields: String? = null

    @Type(type = "jsonb")
    @Column(
        name = "sys_fields",
        columnDefinition = "jsonb"
    )
    private var sysFields: String? = null

    @Type(type = "string-array")
    @Column(
        name = "folder_fields",
        columnDefinition = "character varying(256)[]"
    )
    private var folderFields: Array<String> = arrayOf()

    @Column(name = "is_system")
    private var isSystem: Int = 0

    fun getId() = this.id
    fun getTitle() = this.title
    fun getHint() = this.hint
    fun getDescription() = this.description
    fun getTableName() = this.tableName
    fun getUserFields() = this.userFields
    fun getSysFields() = this.sysFields
    fun getAllFields(): MutableMap<String, Any?> {
        val result: MutableMap<String, Any?> = mutableMapOf()
        result.putAll(Util.toJson(this.userFields))
        result.putAll(Util.toJson(this.sysFields))
        return result
    }
    fun getFolderFields() = this.folderFields
    fun getIsSystem() = this.isSystem

    fun setId(id: UUID?) { this.id = id }
    fun setTitle(title: String?) { this.title = title }
    fun setHint(hint: String?) { this.hint = hint }
    fun setDescription(description: String?) { this.description = description }
    fun setTableName(tableName: String) { this.tableName = tableName }
    fun setUserFields(userFields: String?) { this.userFields = userFields }
    fun setSysFields(sysFields: String?) { this.sysFields = sysFields }
    fun setFolderFields(folderFields: Array<String>) { this.folderFields = folderFields }
    fun setIsSystem(isSystem: Int) { this.isSystem = isSystem }

    fun getFolderSerialize(folderFields: Array<String>): MutableList<MutableMap<String, Any?>> {
        var folder: MutableList<MutableMap<String, Any?>> = mutableListOf()
        val uFields: MutableMap<String, Any?> = Util.toJson(userFields)
        val sFields: MutableMap<String, Any?> = Util.toJson(sysFields)
        for (item in folderFields) {
            val itemMap: MutableMap<String, Any?> = mutableMapOf()
            itemMap["id"] = item
            if (uFields.containsKey(item)) {
                itemMap["title"] = (uFields[item] as MutableMap<String, Any?>)["title"].toString()
            }
            if (sFields.containsKey(item)) {
                itemMap["title"] = (sFields[item] as MutableMap<String, Any?>)["title"].toString()
            }
            folder.add(itemMap)
        }
        return folder
    }

}
