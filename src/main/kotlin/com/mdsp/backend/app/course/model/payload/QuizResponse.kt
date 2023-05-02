package com.mdsp.backend.app.course.model.payload

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

interface QuizResponse {

    fun getId(): UUID?

    @JsonProperty("display_name")
    fun getTitle(): String?

    @JsonProperty("order_num")
    fun getOrderNum(): Int


}
