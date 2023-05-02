package com.mdsp.backend.app.course.model.payload

import com.mdsp.backend.app.course.model.Quiz
import java.util.*


class QuizClientResponse(private var quiz: Quiz) : QuizResponse {

    override fun getId() = this.quiz.getId()

    override fun getTitle() = this.quiz.getTitle()

    override fun getOrderNum() = this.quiz.getOrderNum()

    fun getDescription() = this.quiz.getDescription()

    fun getQuestions(): ArrayList<MutableMap<String, Any?>> {
        val questions = this.quiz.getQuestions()
        for (question in questions) {
            val answers = question["answers"] as ArrayList<MutableMap<String, Any?>>
            for (answer in answers) {
                answer.remove("isAnswer")
            }
            answers.shuffle()
            question.remove("answer")
            question.remove("expanded")
            question.remove("addingNewAnswer")
        }
        return questions
    }

}
