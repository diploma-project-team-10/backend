package com.mdsp.backend.app.community.quiz.payload

import com.fasterxml.jackson.annotation.JsonProperty
import com.mdsp.backend.app.community.question.payload.GeneratedQuestion
import java.util.*

class JsonQuiz{
    //Quiz session id
    @JsonProperty("id")
    var id: UUID? = null

    @JsonProperty("leftPosition")
    var leftPosition: Int? = null

    @JsonProperty("rightPosition")
    var rightPosition: Int? = null

    @JsonProperty("question")
    var questions: GeneratedQuestion? = null
}
