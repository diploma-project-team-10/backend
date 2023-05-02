package com.mdsp.backend.app.structure.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.mdsp.backend.app.system.model.DateAudit
import com.mdsp.backend.app.system.model.Util
import com.vladmihalcea.hibernate.type.array.StringArrayType
import com.vladmihalcea.hibernate.type.array.UUIDArrayType
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull


@Entity
@Table(
    name = "structure"
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
@JsonIgnoreProperties(value = ["path"], allowGetters = false)
class Structure: DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private var id: UUID? = null

    @NotNull
    @Column(name = "display_name")
    private var displayName: String? = null

    @Column(name = "parent_id")
    private var parentId: UUID? = null

    @Column(name = "profile_id")
    private var profileId: UUID? = null

    @Column(name = "type")
    private var type: String? = null

    @Type(type = "string-array")
    @Column(
        name = "manager",
        columnDefinition = "character varying(256)[]"
    )
    private var manager: Array<Array<String>> = arrayOf()

    @Type(type = "string-array")
    @Column(
        name = "employee",
        columnDefinition = "character varying(256)[]"
    )
    private var employee: Array<Array<String>> = arrayOf()

    @Type(type = "uuid-array")
    @Column(
        name = "path",
        columnDefinition = "uuid[]"
    )
    private var path: Array<UUID> = arrayOf()

    @Column(name = "sort_order")
    private var sortOrder: Int = 99999

    @Transient
    var profile: ArrayList<MutableMap<String, Any?>>? = arrayListOf()

    @Transient
    var parent: ArrayList<MutableMap<String, Any?>>? = arrayListOf()

    @Transient
    var childrenStructure: ArrayList<Structure>? = arrayListOf()

    @Transient
    var childrenCount: Int? = 0

    constructor(id: UUID?) {
        this.id = id
    }

    fun getId() = this.id
    fun getDisplayName() = this.displayName
    fun getParentId() = this.parentId
    fun getProfileId() = this.profileId
    fun getType() = this.type
    fun getManager() = Util.arrayToMap(this.manager)
    fun getEmployee() = Util.arrayToMap(this.employee)
    fun getPath() = this.path
    fun getSortOrder() = this.sortOrder


    fun setId(id: UUID?) { this.id = id }
    fun setDisplayName(displayName: String?) { this.displayName = displayName }
    fun setParentId(parentId: UUID?) { this.parentId = parentId }
    fun setProfileId(profileId: UUID?) { this.profileId = profileId }
    fun setType(type: String?) {
        this.type = type
        if (this.type != "profile") {
            this.profile = null
        }
    }
    fun setManager(manager: ArrayList<MutableMap<String, Any?>>) { this.manager = Util.mapToArray(manager, true) }
    fun setEmployee(employee: ArrayList<MutableMap<String, Any?>>) { this.employee = Util.mapToArray(employee) }
    fun setPath(path: Array<UUID>) { this.path = path }
    fun setSortOrder(sortOrder: Int) { this.sortOrder = sortOrder }

}
