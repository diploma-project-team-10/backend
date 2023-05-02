package com.mdsp.backend.app.course.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.mdsp.backend.app.system.model.Util
import com.vladmihalcea.hibernate.type.array.StringArrayType
import com.mdsp.backend.app.course.repository.ICourseRepository
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import java.sql.Timestamp
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "course")
@TypeDefs(
    TypeDef(
        name = "string-array",
        typeClass = StringArrayType::class
    )
)
class Course() : StatusAudit() {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private var id: UUID? = null

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

    @Type(type = "string-array")
    @Column(
        name = "bg_image",
        columnDefinition = "character varying(256)[]"
    )
    @JsonProperty("bg_image")
    private var bgImage: Array<Array<String>> = arrayOf()

    @Type(type = "string-array")
    @Column(
        name = "feature_image",
        columnDefinition = "character varying(256)[]"
    )
    @JsonProperty("feature_image")
    private var featureImage: Array<Array<String>> = arrayOf()

    @Column(
        name = "intro_video",
        columnDefinition = "character varying"
    )
    @JsonProperty("intro_video")
    private var introVideo: String? = null

    @Column(
        name = "intro_description",
        columnDefinition = "text"
    )
    @JsonProperty("intro_description")
    private var introDescription: String? = null

    @Type(type = "string-array")
    @Column(
        name = "language",
        columnDefinition = "character varying(256)[]"
    )
    private var language: Array<Array<String>> = arrayOf()

    @Column(name = "publish_date")
    @JsonProperty("publish_date")
    private var publishDate: Timestamp? = null

    @Column(name = "order_num")
    @JsonProperty("order_num")
    private var orderNum: Long = 99999

    @Column(name = "rating")
    private var rating: Double = 0.0

    @Type(type = "string-array")
    @Column(
        name = "packages",
        columnDefinition = "character varying(256)[]"
    )
    private var packages: Array<Array<String>> = arrayOf()

    @Type(type = "string-array")
    @Column(
        name = "company",
        columnDefinition = "character varying(256)[]"
    )
    private var company: Array<Array<String>> = arrayOf()

    @Type(type = "string-array")
    @Column(
        name = "teachers",
        columnDefinition = "character varying(256)[]"
    )
    private var teachers: Array<Array<String>> = arrayOf()

    @Type(type = "string-array")
    @Column(
        name = "access",
        columnDefinition = "character varying(256)[]"
    )
    private var access: Array<Array<String>> = arrayOf()

    @Type(type = "jsonb")
    @Column(
        name = "access_by_service",
        columnDefinition = "jsonb"
    )
    private var accessByService: MutableMap<String, Array<Array<String>>> = mutableMapOf()

    @Column(name = "access_course")
    @JsonProperty("access_course")
    private var accessCourse: Int? = null

    @Column(name = "is_prerequisites")
    @JsonProperty("is_prerequisites")
    private var isPrerequisites: Boolean = false

    //Any Selected = 1
    //The user must complete any one of the selected courses in order to access this course
    //
    //All Selected = 2
    //The user must complete all selected course in order to access this course
    @Column(name = "prerequisites_mode")
    @JsonProperty("prerequisites_mode")
    @Enumerated(EnumType.STRING)
    private var prerequisitesMode: Prerequisites = Prerequisites.ANY

    //Courses must learn
    @Type(type = "string-array")
    @Column(
        name = "prerequisites_courses",
        columnDefinition = "character varying(256)[]"
    )
    @JsonProperty("prerequisites_courses")
    private var prerequisitesCourses: Array<Array<String>> = arrayOf()

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "course_id", referencedColumnName = "id", insertable = false, updatable = false)
    @OrderBy("order_num ASC")
    private var modules: Collection<ModuleCourse> = listOf()

    @Transient
    @JsonProperty("progress_percent")
    private var progressPercent: Int? = null

    @Transient
    @JsonProperty("serviceId")
    private var serviceId: UUID? = null

    fun getId() = this.id
    fun setId(id: UUID?) { this.id = id }

    fun getTitle() = this.title
    fun setTitle(title: String) {
        this.title = title
    }

    fun getIsPrerequisites() = this.isPrerequisites
    fun setIsPrerequisites(isPrerequisites: Boolean?) {
        this.isPrerequisites = false
        if (isPrerequisites != null) {
            this.isPrerequisites = isPrerequisites
        }
    }

    fun getPrerequisitesMode() = this.prerequisitesMode
    fun setPrerequisitesMode(prerequisitesMode: Prerequisites?) {
        this.prerequisitesMode = Prerequisites.ANY
        if (prerequisitesMode != null) {
            this.prerequisitesMode = prerequisitesMode
        }
    }

    fun getPrerequisitesCourses() = Util.arrayToMap(this.prerequisitesCourses)
    fun setPrerequisitesCourses(prerequisitesCourses: ArrayList<MutableMap<String, Any?>>) {
        this.prerequisitesCourses = Util.mapToArray(prerequisitesCourses, true)
    }

    fun getOrderNum() = this.orderNum
    fun setOrderNum(orderNum: Long?) {
        this.orderNum = 9999
        if (orderNum != null) {
            this.orderNum = orderNum
        }
    }

    fun getDescription() = this.description
    fun setDescription(description: String?) {
        this.description = description
    }

    fun getBgImage() = Util.arrayToMap(this.bgImage)
    fun setBgImage(bgImage: ArrayList<MutableMap<String, Any?>>) {
        this.bgImage = Util.mapToArray(bgImage, true)
    }

    fun getFeatureImage() = Util.arrayToMap(this.featureImage)
    fun setFeatureImage(featureImage: ArrayList<MutableMap<String, Any?>>) {
        this.featureImage = Util.mapToArray(featureImage, true)
    }

    fun getIntroVideo() = this.introVideo
    fun setIntroVideo(introVideo: String?) {
        this.introVideo = introVideo
    }

    fun getIntroDescription() = this.introDescription
    fun setIntroDescription(introDescription: String?) {
        this.introDescription = introDescription
    }

    fun getLanguage() = Util.arrayToMap(this.language)
    fun setLanguage(languages: ArrayList<MutableMap<String, Any?>>) {
        this.language = Util.mapToArray(languages, true)
    }

    fun getPublishDate() = this.publishDate
    fun setPublishDate(publishDate: Timestamp?) {
        this.publishDate = publishDate
    }

    fun getRating() = this.rating
    fun setRating(rating: Double?) {
        this.rating = 0.0
        if (rating != null) {
            this.rating
        }
    }

    fun getPackages() = Util.arrayToMap(this.packages)
    fun setPackages(packages: ArrayList<MutableMap<String, Any?>>) {
        this.packages = Util.mapToArray(packages)
    }

    fun getCompany() = Util.arrayToMap(this.company)
    fun setCompany(company: ArrayList<MutableMap<String, Any?>>) {
        this.company = Util.mapToArray(company)
    }

    fun getTeachers() = Util.arrayToMap(this.teachers)
    fun setTeachers(teachers: ArrayList<MutableMap<String, Any?>>) {
        this.teachers = Util.mapToArray(teachers)
    }

    fun getAccess() : ArrayList<MutableMap<String, Any?>> {
        if (serviceId != null && this.accessByService.containsKey(serviceId.toString())) {
            return Util.arrayToMap(this.accessByService[this.serviceId.toString()]!!)
        } else if (!this.access.isNullOrEmpty()) {
            return Util.arrayToMap(this.access)
        }
        return arrayListOf()
    }
    fun setAccess(access: ArrayList<MutableMap<String, Any?>>) {
        if (serviceId != null && !this.accessByService.isNullOrEmpty() && this.accessByService.containsKey(serviceId.toString())) {
            this.access = this.accessByService[this.serviceId.toString()]!!
        } else if (!access.isNullOrEmpty()) {
            this.access = Util.mapToArray(access)
        }else {
            this.access = arrayOf()
        }
    }

    fun getAccessCourse() = this.accessCourse
    fun setAccessCourse(accessCourse: Int?) {
        this.accessCourse = accessCourse
    }

    fun getModules() = this.modules.filter { module -> module.deletedAt == null }

    @JsonProperty("module_count")
    fun getModuleCount() = this.modules.size

    @JsonProperty("lesson_count")
    fun getLessonCount(): Int {
        var result = 0
        for (module in this.modules) {
            result += module.getLessonCount()
        }
        return result
    }

    @JsonProperty("quiz_count")
    fun getQuizCount(): Int {
        var result = 0
        for (module in this.modules) {
            for (lesson in module.getLessons()) {
                result += lesson.getQuizzes().size
            }
        }
        return result
    }

    fun getProgressPercent() = this.progressPercent
    fun setProgressPercent(progressPercent: Int?) {
        this.progressPercent = progressPercent
    }

    fun setAccesses(courseRepository: ICourseRepository, access: ArrayList<MutableMap<String, Any?>>, serviceId: UUID) {
        val course = courseRepository.findByIdAndDeletedAtIsNull(this.id!!)
        if (course.isPresent) {
            this.accessByService = course.get().accessByService
            this.accessByService[serviceId.toString()] = Util.mapToArray(access)
        }
    }

    fun setServiceId(serviceId: UUID?) {
        this.serviceId = serviceId
    }
}
