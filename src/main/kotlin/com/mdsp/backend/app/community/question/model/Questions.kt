package com.mdsp.backend.app.community.question.model

import com.mdsp.backend.app.community.topic.model.Topic
import com.mdsp.backend.app.community.question.payload.Variable
import com.mdsp.backend.app.community.question.payload.Variant
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.mdsp.backend.app.community.quiz.dto.IQuiz
import com.mdsp.backend.app.community.quiz.dto.QuizAudit
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.*
import kotlin.collections.ArrayList

@Entity
@Table(name = "community_questions")
@JsonIgnoreProperties(
    value = ["errorText", "typeQuiz", "variantTest", "variantFIB", "variantMatch", "variantOrder"],
    allowGetters = false
)
open class Questions : QuizAudit() {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    var id: UUID? = null

    @Column(name = "type")
    var type: Int = 0

    @Column(name = "topic_id")
    var topicId: UUID? = null

    @Type(type = "string-array")
    @Column(
        name = "relative_topics",
        columnDefinition = "text[]"
    )
    var relativeTopics: Array<String>? = null

    @Type(type = "jsonb")
    @Column(
        name = "variables",
        columnDefinition = "jsonb"
    )
    var variables: ArrayList<Variable> = arrayListOf()

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "topic_id", referencedColumnName = "id", updatable = false, insertable = false)
    var topic: Topic? = null

    @Transient
    var errorText = ""

    @Transient
    lateinit var typeQuiz: IQuiz

    override fun toString(): String {
        return "ID=${this.id}\ndescription=${this.description}\nvariables=${this.variables}\n" +
                "variants=${variants}"
    }

    fun clone(): Questions {
        val question = Questions()
        question.id = (this.id)
        question.type = (this.type)
        question.description = (description)
        question.descriptionRu = (descriptionRu)
        question.descriptionEn = (descriptionEn)
        question.topicId = (this.topicId)
        question.relativeTopics = (this.relativeTopics)

        val variablesClone: ArrayList<Variable> = arrayListOf()
        this.variables.forEach {v ->
            variablesClone.add(v.clone())
        }
        question.variables = variablesClone

        val variantsClone: ArrayList<Variant> = arrayListOf()
        this.variants.forEach {v ->
            variantsClone.add(v.clone())
        }
        question.variants = variantsClone

        return question
    }
}
