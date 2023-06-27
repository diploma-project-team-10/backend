package com.mdsp.backend.app.community.quiz.model

import com.mdsp.backend.app.community.question.payload.GeneratedQuestion
import com.mdsp.backend.app.system.model.DateAudit
import com.vladmihalcea.hibernate.type.array.StringArrayType
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import java.util.*
import javax.persistence.*
import kotlin.collections.ArrayList

@Entity
@Table(name = "community_quiz")
@TypeDefs(
    TypeDef(
        name = "string-array", typeClass = StringArrayType::class
    )
)
class CommunityQuiz: DateAudit {
    constructor(
        id: UUID?,
        programId: UUID?
    ){
        this.id = id
        this.programId = programId
    }


    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    var id: UUID? = null

    var programId: UUID? = null
    var studentId: UUID? = null
    var examinator: String? = null

    @Type(type = "jsonb")
    @Column(
        name = "questions",
        columnDefinition = "jsonb"
    )
    var questions: ArrayList<GeneratedQuestion> = arrayListOf()

    @Column(name = "correctAnswers")
    var correctAnswers: Int? = null

    fun addQuestion(questions: GeneratedQuestion) { this.questions.add(questions) }

    fun getQuestionById(id: UUID): GeneratedQuestion?{
        return this.questions.firstOrNull { it.id == id }
    }
}
