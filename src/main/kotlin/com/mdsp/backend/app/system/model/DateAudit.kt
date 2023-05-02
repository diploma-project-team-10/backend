package com.mdsp.backend.app.system.model


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.vladmihalcea.hibernate.type.array.StringArrayType
import com.vladmihalcea.hibernate.type.array.UUIDArrayType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.sql.Timestamp
import java.util.*
import javax.persistence.Column
import javax.persistence.EntityListeners
import javax.persistence.MappedSuperclass

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
@JsonIgnoreProperties(value = ["deletedAt"], allowGetters = false)
@TypeDefs(
    TypeDef(
        name = "string-array",
        typeClass = StringArrayType::class
    )
)
abstract class DateAudit : Serializable {
    @CreatedDate
    @Column(
        name="created_at",
        nullable = false,
        updatable = false)
    var createdAt: Timestamp = Timestamp(System.currentTimeMillis())

    @LastModifiedDate
    @Column(name="updated_at")
    var updatedAt: Timestamp? = null

    @Column(name="deleted_at")
    var deletedAt: Timestamp? = null

    @Type(type = "string-array")
    @Column(
        name = "creator",
        columnDefinition = "character varying(256)[]"
    )
    var creator: Array<Array<String>>? = null

    @Type(type = "string-array")
    @Column(
        name = "editor",
        columnDefinition = "character varying(256)[]"
    )
    var editor: Array<Array<String>>? = null

}
