package com.mdsp.backend.app.reference.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.mdsp.backend.app.system.model.DateAudit
import com.mdsp.backend.app.system.model.Util
import com.vladmihalcea.hibernate.type.array.StringArrayType
import com.vladmihalcea.hibernate.type.array.UUIDArrayType
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "reference_access")
@JsonIgnoreProperties(value = ["deletedAt", "reference_id"], allowGetters = true)
@TypeDefs(
    TypeDef(
        name = "jsonb",
        typeClass = JsonBinaryType::class
    )
)
class Access() : DateAudit() {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Type(type="org.hibernate.type.PostgresUUIDType")
    @Column(name = "id", updatable = false, nullable = false)
    var id: UUID? = null

    @Column(name = "reference_id", nullable = false)
    @JsonProperty("reference_id")
    var referenceId: UUID? = null

    @Column(name = "subject_id", nullable = false)
    @JsonProperty("subject_id")
    var subjectId: UUID? = null

    @Column(name = "title", columnDefinition = "character varying")
    @JsonProperty("title")
    var title: String? = null

    @Column(name = "view_menu")
    @JsonProperty("view_menu")
    var viewMenu: Boolean = false

    @Column(name = "may_view")
    @JsonProperty("may_view")
    var mayView: Boolean = false

    @Column(name = "may_add")
    @JsonProperty("may_add")
    var mayAdd: Boolean = false

    @Type(type = "jsonb")
    @Column(
        name = "objects",
        columnDefinition = "jsonb"
    )
    var objects: List<ObjectAccess> = listOf()

}
