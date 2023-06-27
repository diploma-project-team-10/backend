package com.mdsp.backend.app.community.quiz.dto


import com.mdsp.backend.app.system.model.Util
import kotlin.collections.ArrayList

class Generate: IQuiz {

    override var variantsMap: ArrayList<MutableMap<String, Any?>> = arrayListOf()
    override var errorText: String = ""

    override fun setVariantsMap(variants: String?) {
        if (variants != null) {
            this.variantsMap = Util.toArrayMap(variants)
        }
    }
}
