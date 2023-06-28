package com.mdsp.backend.app.community.topic.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.mdsp.backend.app.system.model.DateAudit
import com.vladmihalcea.hibernate.type.array.IntArrayType
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "community_topics")
@TypeDef(
        name = "int-array",
        typeClass = IntArrayType::class
)
@JsonIgnoreProperties(value = ["children", "hidden", "isLeaf", "selected"], allowGetters = false)
class Topic : DateAudit {

    constructor()

    constructor(id: UUID?, programId:UUID?, parentId: UUID?, title: String) {
        this.id = id
        this.programId = programId
        this.parentId = parentId
        this.title = title
    }

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    var id: UUID? = null

    var parentId: UUID? = null
    var programId: UUID? = null

    var title: String? = null

    var orderNum: Int? = 1

    @Column(
            name = "topic_version",
            columnDefinition = "int[]"
    )
    @Type(type = "int-array")
    var topicVersion: Array<Int>? = null
    var rating: Int? = 1

    override fun toString(): String {
        return "Title=${title} parentId=${parentId} orderNum=${orderNum} topicVersion=${Arrays.toString(topicVersion)}"
    }

}

