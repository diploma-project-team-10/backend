package com.mdsp.backend.app.course.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.vladmihalcea.hibernate.type.array.StringArrayType
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import java.sql.Timestamp
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "module")
@TypeDefs(
    TypeDef(
        name = "string-array",
        typeClass = StringArrayType::class
    )
)
class ModuleCourse() : StatusAudit() {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private var id: UUID? = null

    @Column(name = "course_id")
    @JsonProperty("course_id")
    private var courseId: UUID? = null

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

    @Column(name = "publish_date")
    @JsonProperty("publish_date")
    private var publishDate: Timestamp? = null

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "module_id", referencedColumnName = "id", insertable = false, updatable = false)
    @OrderBy("order_num ASC")
    private var lessons: Collection<Lesson> = listOf()

    @Transient
    private var moduleCount: Int = 0

    @Transient
    private var navigate: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

    fun getId() = this.id
    fun setId(id: UUID?) { this.id = id }

    fun getCourseId() = this.courseId
    fun setCourseId(courseId: UUID?) { this.courseId = courseId }

    fun getTitle() = this.title
    fun setTitle(title: String?) { this.title = title }

    fun getDescription() = this.description
    fun setDescription(description: String?) { this.description = description }

    fun getOrderNum() = this.orderNum
    fun setOrderNum(orderNum: Int?) {
        this.orderNum = 9999
        if (orderNum != null) {
            this.orderNum = orderNum
        }
    }

    fun getPublishDate() = this.publishDate
    fun setPublishDate(publishDate: Timestamp?) {
        this.publishDate = publishDate
    }

    fun getLessons() = this.lessons.filter { lesson -> lesson.deletedAt == null }

    @JsonProperty("lesson_count")
    fun getLessonCount(): Int {
        return this.lessons.filter { lesson -> lesson.deletedAt == null }.size
    }

    @JsonProperty("quiz_count")
    fun getQuizCount(): Int {
        var result = 0
        for (lesson in this.getLessons()) {
            result += lesson.getQuizzes().size
        }
        return result
    }

    @JsonProperty("module_count")
    fun getModuleCount() = this.moduleCount
    fun setModuleCount(moduleCount: Int) {
        this.moduleCount = moduleCount
    }

    fun getNavigate() = this.navigate
    fun setNavigate(navigate: MutableMap<String, MutableMap<String, String>>) {
        this.navigate = navigate
    }

}
