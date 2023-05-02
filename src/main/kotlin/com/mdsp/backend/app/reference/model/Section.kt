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
import javax.validation.constraints.NotBlank

@Entity
@Table(
    name = "sections"
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
class Section: DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private var id: UUID? = null

    @Column(name = "title")
    private var title: String? = null

    @Column(name = "hint")
    private var hint: String? = null

    @Type(type = "jsonb")
    @Column(
        name = "fields",
        columnDefinition = "jsonb"
    )
    private var fields: String? = null

    @Type(type = "jsonb")
    @Column(
        name = "group_field",
        columnDefinition = "jsonb"
    )
    private var groupField: String? = null

    @Type(type = "jsonb")
    @Column(
        name = "sort_field",
        columnDefinition = "jsonb"
    )
    private var sortField: String? = null

    @Type(type = "jsonb")
    @Column(
        name = "filter_field",
        columnDefinition = "jsonb"
    )
    private var filterField: String? = null

    @Type(type = "string-array")
    @Column(
        name = "access",
        columnDefinition = "character varying(256)[]"
    )
    private var access: Array<Array<String>> = arrayOf()

    @Column(name = "enable_custom_fields")
    private var enableCustomFields: Boolean = false

    @Column(name = "reference_id")
    private var referenceId: UUID? = null

    @Column(name = "orderNum")
    private var orderNum: Long? = 99999

    constructor(id: UUID?) {
        this.id = id
    }

    fun getId() = this.id
    fun getTitle() = this.title
    fun getHint() = this.hint
    fun getFields() = Util.toArrayMap(this.fields)
    fun getGroupField() = Util.toArrayMap(this.groupField)
    fun getSortField() = Util.toArrayMap(this.sortField)
    fun getFilterField() = Util.jsonToObject(this.filterField)
    fun getAccess() = Util.arrayToMap(this.access)
    fun getEnableCustomFields() = this.enableCustomFields
    fun getReferenceId() = this.referenceId
    fun getOrderNum() = this.orderNum

    fun setId(id: UUID?) { this.id = id }
    fun setTitle(title: String?) { this.title = title }
    fun setHint(hint: String?) { this.hint = hint }
    fun setFields(fields: String?) { this.fields = fields }
    fun setGroupField(groupField: String?) { this.groupField = groupField }
    fun setSortField(sortField: String?) { this.sortField = sortField }
    fun setFilterField(filterField: String?) { this.filterField = filterField }
//    fun setAccess(access: Array<Array<String>>) { this.access = access }
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
    fun setOrderNum(orderNum: Long?) { this.orderNum = orderNum }

}
