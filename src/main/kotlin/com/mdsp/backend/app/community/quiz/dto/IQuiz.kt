package com.mdsp.backend.app.community.quiz.dto

interface IQuiz {
    var variantsMap: ArrayList<MutableMap<String, Any?>>
    var errorText: String
    fun setVariantsMap(variants: String?)
}
