package com.mdsp.backend.app.community.question.payload

import java.util.*

class GeneratedQuestion {
    var id: UUID? = null
    var description: String? = null
    var descriptionRu: String? = null
    var descriptionEn: String? = null

    var answerType: Int? = null
    var answerVariants: ArrayList<Variant> = arrayListOf()
    var answerRelVariants: ArrayList<Variant> = arrayListOf()
    var studentAnswer: ArrayList<Variant> = arrayListOf()
    var isCorrect: Boolean = false

    constructor()

    constructor(
        id: UUID?,
        description: String?,
        descriptionRu: String?,
        descriptionEn: String?,
        answerType: Int?,
        answerVariants: ArrayList<Variant> = arrayListOf(),
        answerRelVariants: ArrayList<Variant> = arrayListOf()
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
