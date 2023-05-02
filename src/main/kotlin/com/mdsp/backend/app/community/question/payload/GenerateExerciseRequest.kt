package com.mdsp.backend.app.community.question.payload

import java.util.*
import javax.validation.constraints.NotBlank
import kotlin.collections.ArrayList

class GenerateExerciseRequest {
    private var id: UUID? = null
    @NotBlank(message = "Description cannot be blank")
    var description: String? = null
    var descriptionRu: String? = null
    var descriptionEn: String? = null

    var answerType: Int? = null
    var customVariants: ArrayList<Variant> = arrayListOf()

    var variable: ArrayList<Variable> = arrayListOf()
}
