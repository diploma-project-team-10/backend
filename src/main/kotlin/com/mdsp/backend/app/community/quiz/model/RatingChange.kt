package com.mdsp.backend.app.community.quiz.model

import java.time.LocalDate

data class RatingChange (
    var date: LocalDate?,
    var rating: Int?
)
