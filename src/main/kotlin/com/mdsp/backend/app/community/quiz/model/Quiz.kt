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
class Quiz: DateAudit {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    var id: UUID? = null

    @Column(name = "program_id")
    var programId: UUID? = null

    @Type(type = "string-array")
    @Column(
        name = "student",
        columnDefinition = "character varying(256)[]"
    )
    var student: Array<Array<String>>? = null

    @Type(type = "string-array")
    @Column(
        name = "examinator",
        columnDefinition = "character varying(256)[]"
    )
    var examinator: Array<Array<String>>? = null

    @Type(type = "jsonb")
    @Column(
        name = "questions",
        columnDefinition = "jsonb"
    )
    var questions: ArrayList<GeneratedQuestion> = arrayListOf()

    @Column(name = "correctAnswers")
    var correctAnswers: Int? = null

    constructor(
        id: UUID?,
        programId: UUID?
    ){
        this.id = id
        this.programId = programId
    }
    fun addQuestion(questions: GeneratedQuestion) { this.questions.add(questions) }

    fun getQuestionById(id: UUID): GeneratedQuestion?{
        for(q in this.questions!!){
            if(q.id == id){
                return q
            }
        }
        return null
    }
}
