package com.mdsp.backend.app.community.quiz.model

import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "user_rating")
class UserRating {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    var id: UUID? = null

    var programId: UUID? = null
    var userId: UUID? = null
    var rating: Int? = null

    @Type(type = "jsonb")
    @Column(
        name = "changes",
        columnDefinition = "jsonb"
    )
    var changes: List<RatingChange> = listOf()
}
