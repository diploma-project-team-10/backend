package com.mdsp.backend.app.community.program.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.mdsp.backend.app.community.topic.model.Topic
import com.mdsp.backend.app.system.model.DateAudit
import com.vladmihalcea.hibernate.type.array.StringArrayType
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "community_programs")
@TypeDefs(
        TypeDef(
                name = "string-array",
                typeClass = StringArrayType::class
        )
)
@JsonIgnoreProperties(value = ["selected"], allowGetters = false)
class Program : DateAudit {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    var id: UUID? = null

    var image: UUID? = null

    @Column(name = "title")
    var title: String? = null

    @Transient
    var relativeTopics: ArrayList<Topic> = arrayListOf()

    constructor(
            id: UUID?,
            title: String?,
    ) {
        this.id = id
        this.title = title
    }
}
