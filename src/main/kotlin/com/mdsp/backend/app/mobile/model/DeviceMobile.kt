package com.mdsp.backend.app.mobile.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.mdsp.backend.app.system.model.DateAudit
import com.vladmihalcea.hibernate.type.array.StringArrayType
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import java.io.Serializable
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "device_mobile")
@JsonIgnoreProperties(value = ["deletedAt"], allowGetters = true)
@TypeDefs(
    TypeDef(
        name = "string-array",
        typeClass = StringArrayType::class
    )
)
class DeviceMobile: DateAudit() {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = true, nullable = false)
    var id: UUID? = null

    @Column(name="device_id", nullable = false, unique = true, columnDefinition = "character varying")
    @JsonProperty("device_id")
    var deviceId: String? = null

    @Column(name="profile_id", nullable = false)
    @JsonProperty("profile_id")
    var profileId: UUID? = null

}
