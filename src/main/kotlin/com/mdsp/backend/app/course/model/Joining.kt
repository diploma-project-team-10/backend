package com.mdsp.backend.app.course.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.mdsp.backend.app.system.model.DateAudit
import java.sql.Timestamp
import java.util.*
import javax.persistence.*
import kotlin.jvm.Transient

@Entity
@Table(name = "joining")
class Joining() : DateAudit() {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private var id: UUID? = null

    @Column(name = "profile_id")
    @JsonProperty("profile_id")
    private var profileId: UUID? = null

    @Column(name = "course_id")
    @JsonProperty("course_id")
    private var courseId: UUID? = null

    @Column(name = "start_date")
    @JsonProperty("start_date")
    private var startDate: Timestamp = Timestamp(System.currentTimeMillis())

    @Column(name = "end_date")
    @JsonProperty("end_date")
    private var endDate: Timestamp? = null

    @Column(name="progress_percent")
    @JsonProperty("progress_percent")
    private var progressPercent: Int = 0

    @Transient
    @JsonProperty("access_course")
    private var accessCourse: Int? = null

    @Transient
    @JsonProperty("accessAcl")
    private var accessAcl: Array<UUID> = arrayOf()

    fun getId() = this.id
    fun setId(id: UUID?) {
        this.id = id
    }

    fun getProfileId() = this.profileId
    fun setProfileId(profileId: UUID) {
        this.profileId = profileId
    }

    fun getCourseId() = this.courseId
    fun setCourseId(courseId: UUID) {
        this.courseId = courseId
    }

    fun getStartDate() = this.startDate
    fun setStartDate(startDate: Timestamp?) {
        this.startDate = Timestamp(System.currentTimeMillis())
        if (startDate != null) {
            this.startDate = startDate
        }
    }

    fun getEndDate() = this.endDate
    fun setEndDate(endDate: Timestamp?) {
        this.endDate = endDate
    }

    fun getProgressPercent() = this.progressPercent
    fun setProgressPercent(progressPercent: Int?) {
        this.progressPercent = 0
        if (progressPercent != null) {
            this.progressPercent = progressPercent
        }
    }

    fun getAccessCourse() = this.accessCourse
    fun setAccessCourse(accessCourse: Int?) {
        this.accessCourse = accessCourse
    }

    fun getAccessAcl() = this.accessAcl
    fun setAccessAcl(accessAcl: Array<UUID>) {
        this.accessAcl = accessAcl
    }
}
