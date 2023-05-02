package com.mdsp.backend.app.community.analytics.payload

import java.util.*

class TopicAnalytics {

    var topicId: UUID? = null
    var correctAnswer: Int = 0
    var wrongAnswer: Int = 0
    var percentage: Double = 0.0

    constructor(topicId: UUID){
        this.topicId = topicId
    }

    constructor(
            topicId: UUID,
            correct: Int,
            wrong: Int,
            percentage: Double
    ) {
        this.topicId = topicId
        this.correctAnswer = correct
        this.wrongAnswer = wrong
        this.percentage = percentage
    }

    fun addCorrectAnswer(){
        this.correctAnswer++
        this.percentage = (this.correctAnswer) * 1.0 / (this.correctAnswer + this.wrongAnswer)
    }

    fun addWrongAnswer(){
        this.wrongAnswer++
        this.percentage = (this.correctAnswer) * 1.0 / (this.correctAnswer + this.wrongAnswer)
    }

    override fun toString(): String {
        return "TopicId=${this.topicId} correctAnswer=${this.correctAnswer} wrongAnswer=${this.wrongAnswer}"
    }
}
