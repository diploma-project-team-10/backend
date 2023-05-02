package com.mdsp.backend.app.course.model.payload

import com.mdsp.backend.app.course.model.Quiz


class QuizShortResponse: QuizResponse {

    private lateinit var quiz: Quiz

    constructor(quiz: Quiz) {
        this.quiz = quiz
    }

    override fun getId() = this.quiz.getId()

    override fun getTitle() = this.quiz.getTitle()

    override fun getOrderNum() = this.quiz.getOrderNum()

    fun getMarked() = this.quiz.getMarked()
    fun setMarked(marked: Boolean) {
        this.quiz.setMarked(marked)
    }

}
