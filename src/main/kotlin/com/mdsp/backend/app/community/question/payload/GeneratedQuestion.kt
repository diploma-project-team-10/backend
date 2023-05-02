package com.mdsp.backend.app.community.question.payload

import java.util.*

class GeneratedQuestion {
    var id: UUID? = null
    var description: String? = null
    var descriptionRu: String? = null
    var descriptionEn: String? = null

    var answerType: Int? = null
    var answerVariants: ArrayList<Variant>? = null
    var answerRelVariants: ArrayList<Variant>? = null
    var studentAnswer: ArrayList<Variant>? = null
    var isCorrect: Boolean = false

    constructor()

    constructor(
        id: UUID?,
        description: String?,
        descriptionRu: String?,
        descriptionEn: String?,
        answerType: Int?,
        answerVariants: ArrayList<Variant>?,
        answerRelVariants: ArrayList<Variant>?
    ) {
        this.id = id
        this.description = description
        this.descriptionRu = descriptionRu
        this.descriptionEn = descriptionEn
        this.answerType = answerType
        this.answerVariants = answerVariants
        this.answerRelVariants = answerRelVariants
    }
}
