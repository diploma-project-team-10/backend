package com.mdsp.backend.app.quiz.model

interface IQuiz {
    var variantsMap: ArrayList<MutableMap<String, Any?>>
    var errorText: String
    fun setVariantsMap(variants: String?)
}
