package com.mdsp.backend.app.course.model.payload

import java.util.*


class QuizPassResponse {

    private var id: UUID? = null
    private var answered: ArrayList<MutableMap<String, Any?>> = arrayListOf()

    fun getId() = this.id
    fun setId(id: UUID?) { this.id = id }

    fun getAnswered() = this.answered
    fun setAnswered(answered: ArrayList<MutableMap<String, Any?>>) { this.answered = answered }

}
