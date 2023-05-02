package com.mdsp.backend.app.structure.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.mdsp.backend.app.system.model.DateAudit
import com.mdsp.backend.app.system.model.Util
import com.vladmihalcea.hibernate.type.array.StringArrayType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull


@Entity
@Table(
    name = "roles_group"
)
@TypeDefs(
    TypeDef(
        name = "string-array",
        typeClass = StringArrayType::class
    )
)
@JsonIgnoreProperties(value = [], allowGetters = false)
class RolesGroup: DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private var id: UUID? = null

    @NotNull
    @Column(name = "display_name")
    private var displayName: String? = null

    @Column(name = "type")
    private var type: String? = "group"

    @Type(type = "string-array")
    @Column(
        name = "members",
        columnDefinition = "character varying(256)[]"
    )
    private var members: Array<Array<String>> = arrayOf()

    @Column(name = "sort_order")
    private var sortOrder: Int? = 99999

    @Column(name = "key")
    private var key: String? = ""

    @Type(type = "string-array")
    @Column(
        name = "child_role",
        columnDefinition = "character varying(256)[]"
    )
    private var childRoles: Array<Array<String>> = arrayOf()

    constructor(id: UUID?) {
        this.id = id
    }

    fun getId() = this.id
    fun getDisplayName() = this.displayName
    fun getType() = this.type
    fun getMembers() = Util.arrayToMap(this.members)
    fun getKey() = this.key
    fun getSortOrder() = this.sortOrder
    fun getChildRoles() = Util.arrayToMap(this.childRoles)

    fun setId(id: UUID?) { this.id = id }
    fun setDisplayName(displayName: String?) { this.displayName = displayName }
    fun setType(type: String?) { this.type = type }
    fun setKey(key: String?) {
        this.key = key
        if (key != null) {
            this.key = key.trim().uppercase()
        }
    }
    fun setMembers(members: Array<Array<String>>) { this.members = members }
    fun setSortOrder(sortOrder: Int?) {
        this.sortOrder = 99999
        if (sortOrder != null) {
            this.sortOrder = sortOrder
        }
    }
//    fun getChildRoles(childRoles: ArrayList<MutableMap<String, Any?>>) {
//        this.childRoles = Util.mapToArray(childRoles)
//    }
}
