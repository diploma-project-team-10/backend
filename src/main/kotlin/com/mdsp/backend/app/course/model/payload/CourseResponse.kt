package com.mdsp.backend.app.course.model.payload

import com.fasterxml.jackson.annotation.JsonProperty
import java.sql.Timestamp
import java.util.*

interface CourseResponse {
    fun getId(): UUID?

    @JsonProperty("display_name")
    fun getTitle(): String?

    @JsonProperty("order_num")
    fun getOrderNum(): Long

    fun getRating(): Double
    fun getPackages(): ArrayList<MutableMap<String, Any?>>
    fun getCompany(): ArrayList<MutableMap<String, Any?>>
    fun getTeachers(): ArrayList<MutableMap<String, Any?>>

    @JsonProperty("feature_image")
    fun getFeatureImage(): ArrayList<MutableMap<String, Any?>>

    @JsonProperty("lesson_count")
    fun getLessonCount(): Int
}
