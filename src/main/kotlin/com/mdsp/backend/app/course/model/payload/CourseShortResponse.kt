package com.mdsp.backend.app.course.model.payload

import com.fasterxml.jackson.annotation.JsonProperty
import com.mdsp.backend.app.course.model.Course
import java.util.*
import javax.persistence.*


class CourseShortResponse: CourseResponse {

    private lateinit var course: Course

    constructor(course: Course) {
        this.course = course
    }

    override fun getId() = this.course.getId()

    override fun getTitle() = this.course.getTitle()

    override fun getOrderNum() = this.course.getOrderNum()

    override fun getRating() = this.course.getRating()

    override fun getPackages() = this.course.getPackages()

    override fun getFeatureImage() = this.course.getFeatureImage()

    override fun getCompany() = this.course.getCompany()

    override fun getTeachers() = this.course.getTeachers()

    override fun getLessonCount() = this.course.getLessonCount()

    @JsonProperty("progress_percent")
    fun getProgressPercent() = this.course.getProgressPercent()

}
