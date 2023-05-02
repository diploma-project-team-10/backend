package com.mdsp.backend.app.course.model.payload

import com.fasterxml.jackson.annotation.JsonProperty
import com.mdsp.backend.app.course.model.Lesson
import java.util.*


class LessonShortResponse(lesson: Lesson) {
    
    var id: UUID? = null

    @JsonProperty("module_id")
    var moduleId: UUID? = null

    @JsonProperty("display_name")
    var title: String? = null

    @JsonProperty("order_num")
    var orderNum: Int = 9999

    var quizzes: ArrayList<QuizShortResponse> = arrayListOf()

    @JsonProperty("module_order_num")
    var moduleOrderNum: Int = 0

    @JsonProperty("progress_percent")
    var progressPercent: Int = 0

    var marked: Boolean = false

    init {
        id = lesson.getId()
        moduleId = lesson.getModuleId()
        title = lesson.getTitle()
        orderNum = lesson.getOrderNum()
        quizzes = lesson.getQuizzes()
        moduleOrderNum = lesson.getModuleOrderNum()
        progressPercent = lesson.getProgressPercent()
        marked = lesson.getMarked()
    }

}
