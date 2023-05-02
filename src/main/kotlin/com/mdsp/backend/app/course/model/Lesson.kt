package com.mdsp.backend.app.course.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.vladmihalcea.hibernate.type.array.StringArrayType
import com.mdsp.backend.app.course.model.payload.QuizShortResponse
import com.mdsp.backend.app.system.model.Util
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import java.sql.Timestamp
import java.util.*
import javax.persistence.*
import kotlin.collections.ArrayList

@Entity
@Table(name = "lesson")
@TypeDefs(
    TypeDef(
        name = "string-array",
        typeClass = StringArrayType::class
    )
)
class Lesson() : StatusAudit() {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private var id: UUID? = null

    @Column(name = "module_id")
    @JsonProperty("module_id")
    private var moduleId: UUID? = null

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

    @Type(type = "string-array")
    @Column(
        name = "teachers",
        columnDefinition = "character varying(256)[]"
    )
    private var teachers: Array<Array<String>> = arrayOf()

    @Column(name = "publish_date")
    @JsonProperty("publish_date")
    private var publishDate: Timestamp? = null

    @Column(name = "is_video_progression")
    @JsonProperty("is_video_progression")
    private var isVideoProgression: Boolean = false

    @Column(
        name = "video_progression_url",
        columnDefinition = "character varying"
    )
    @JsonProperty("video_progression_url")
    private var videoProgressionUrl: String? = null

    @Column(name = "video_progression_mode")
    @JsonProperty("video_progression_mode")
    @Enumerated(EnumType.STRING)
    private var videoProgressionMode: VideoProgression = VideoProgression.BEFORE

    @Column(name = "is_autostart")
    @JsonProperty("is_autostart")
    private var isAutostart: Boolean = false

    @Column(name = "is_video_controls_display")
    @JsonProperty("is_video_controls_display")
    private var isVideoControlsDisplay: Boolean = false

    @Column(name = "is_video_pause_unf")
    @JsonProperty("is_video_pause_unf")
    private var isVideoPauseUnf: Boolean = false

    @Column(name = "is_video_resume")
    @JsonProperty("is_video_resume")
    private var isVideoResume: Boolean = false

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "lesson_id", referencedColumnName = "id", insertable = false, updatable = false)
    @OrderBy("order_num ASC")
    private var quizzes: Collection<Quiz> = listOf()

    @Transient
    private var moduleOrderNum: Int = 0

    @Transient
    private var navigate: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

    @Transient
    @JsonProperty("progress_percent")
    private var progressPercent: Int = 0

    @Transient
    private var marked: Boolean = false

    fun getId() = this.id
    fun setId(id: UUID?) { this.id = id }

    fun getModuleId() = this.moduleId
    fun setModuleId(moduleId: UUID?) { this.moduleId = moduleId }

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

    fun getTeachers() = Util.arrayToMap(this.teachers)
    fun setTeachers(teachers: ArrayList<MutableMap<String, Any?>>) {
        this.teachers = Util.mapToArray(teachers)
    }

    fun getPublishDate() = this.publishDate
    fun setPublishDate(publishDate: Timestamp?) {
        this.publishDate = publishDate
    }

    fun getIsVideoProgression() = this.isVideoProgression
    fun setIsVideoProgression(isVideoProgression: Boolean?) {
        this.isVideoProgression = false
        if (isVideoProgression != null) {
            this.isVideoProgression = isVideoProgression
        }
    }

    fun getVideoProgressionUrl() = this.videoProgressionUrl
    fun setVideoProgressionUrl(videoProgressionUrl: String?) { this.videoProgressionUrl = videoProgressionUrl }

    fun getVideoProgressionMode() = this.videoProgressionMode
    fun setVideoProgressionMode(videoProgressionMode: VideoProgression?) {
        this.videoProgressionMode = VideoProgression.BEFORE
        if (videoProgressionMode != null) {
            this.videoProgressionMode = videoProgressionMode
        }
    }

    fun getIsAutostart() = this.isAutostart
    fun setIsAutostart(isAutostart: Boolean?) {
        this.isAutostart = false
        if (isAutostart != null) {
            this.isAutostart = isAutostart
        }
    }

    fun getIsVideoControlsDisplay() = this.isVideoControlsDisplay
    fun setIsVideoControlsDisplay(isVideoControlsDisplay: Boolean?) {
        this.isVideoControlsDisplay = false
        if (isVideoControlsDisplay != null) {
            this.isVideoControlsDisplay = isVideoControlsDisplay
        }
    }

    fun getIsVideoPauseUnf() = this.isVideoPauseUnf
    fun setIsVideoPauseUnf(isVideoPauseUnf: Boolean?) {
        this.isVideoPauseUnf = false
        if (isVideoPauseUnf != null) {
            this.isVideoPauseUnf = isVideoPauseUnf
        }
    }

    fun getIsVideoResume() = this.isVideoResume
    fun setIsVideoResume(isVideoResume: Boolean?) {
        this.isVideoResume = false
        if (isVideoResume != null) {
            this.isVideoResume = isVideoResume
        }
    }

    fun getQuizzes(): ArrayList<QuizShortResponse> {
        val data = this.quizzes.filter { quizzes -> quizzes.deletedAt == null }
        val result: ArrayList<QuizShortResponse> = arrayListOf()
        for (item in data) {
            result.add(QuizShortResponse(item))
        }
        return result
    }
    fun setQuizzes(quizzes: Collection<Quiz>) {
        this.quizzes = quizzes
    }

    @JsonProperty("module_order_num")
    fun getModuleOrderNum() = this.moduleOrderNum
    fun setModuleOrderNum(moduleOrderNum: Int) {
        this.moduleOrderNum = moduleOrderNum
    }

    fun getNavigate() = this.navigate
    fun setNavigate(navigate: MutableMap<String, MutableMap<String, String>>) {
        this.navigate = navigate
    }

    fun getProgressPercent() = this.progressPercent
    fun setProgressPercent(progressPercent: Int?) {
        this.progressPercent = 0
        if (progressPercent != null) {
            this.progressPercent = progressPercent
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
