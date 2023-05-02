package com.mdsp.backend.app.course.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.vladmihalcea.hibernate.type.array.StringArrayType
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import com.mdsp.backend.app.course.model.payload.QuizResponse
import com.mdsp.backend.app.system.model.Util
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import java.util.*
import javax.persistence.*


@Entity
@Table(name = "quiz")
@TypeDefs(
    TypeDef(
        name = "string-array",
        typeClass = StringArrayType::class
    ),
    TypeDef(
        name = "jsonb",
        typeClass = JsonBinaryType::class
    )
)
class Quiz() : StatusAudit(), QuizResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private var id: UUID? = null

    @Column(name = "course_id")
    @JsonProperty("course_id")
    private var courseId: UUID? = null

    @Column(name = "lesson_id")
    @JsonProperty("lesson_id")
    private var lessonId: UUID? = null

    @Column(
        name = "display_name",
        columnDefinition = "character varying"
    )
    @JsonProperty("display_name")
    private var title: String? = null

    @Column(
        name = "description",
        columnDefinition = "text"
    )
    private var description: String? = null

    @Column(name = "order_num")
    @JsonProperty("order_num")
    private var orderNum: Int = 9999

    @Type(type = "jsonb")
    @Column(
        name = "questions",
        columnDefinition = "jsonb"
    )
    private var questions: String? = null

    @Column(name = "passing_score")
    @JsonProperty("passing_score")
    private var passingScore: Int? = 0

    @Transient
    private var marked: Boolean = false

    override fun getId() = this.id
    fun setId(id: UUID?) { this.id = id }

    fun getCourseId() = this.courseId
    fun setCourseId(courseId: UUID?) { this.courseId = courseId }

    fun getLessonId() = this.lessonId
    fun setLessonId(lessonId: UUID?) { this.lessonId = lessonId }

    override fun getTitle() = this.title
    fun setTitle(title: String?) { this.title = title }

    fun getDescription() = this.description
    fun setDescription(description: String?) { this.description = description }

    override fun getOrderNum() = this.orderNum
    fun setOrderNum(orderNum: Int?) {
        this.orderNum = 9999
        if (orderNum != null) {
            this.orderNum = orderNum
        }
    }

    fun getQuestions(): ArrayList<MutableMap<String, Any?>> {
        return Util.toArrayMap(this.questions)
    }
    fun setQuestions(questions: ArrayList<MutableMap<String, Any?>>) {
        this.questions = Util.mutableMapAsString(questions)
    }

    fun getPassingScore() = this.passingScore
    fun setPassingScore(passingScore: Int?) {
        this.passingScore = 0
        if (passingScore != null) {
            this.passingScore = passingScore
        }
    }

    fun getMarked() = this.marked
    fun setMarked(marked: Boolean?) {
        this.marked = false
        if (marked != null) {
            this.marked = marked
        }
    }

}
