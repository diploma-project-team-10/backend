package com.mdsp.backend.app.quiz.model

import com.mdsp.backend.app.system.model.Util
import java.util.*
import kotlin.collections.ArrayList

class Test: IQuiz {

    override var variantsMap: ArrayList<MutableMap<String, Any?>> = arrayListOf()
    override var errorText: String = ""

    override fun setVariantsMap(variants: String?) {
        if (variants != null) {
            this.variantsMap = Util.toArrayMap(variants)
            var countAnswer = 0
            for (item in this.variantsMap) {
                for (itemMap in item) {
                    if (!arrayListOf("id", "text", "textRu", "textEn", "isAnswer").contains(itemMap.key)) {
                        item.remove(itemMap.key)
                    }
                }
                if (item.containsKey("isAnswer") && item["isAnswer"] as Boolean) {
                    countAnswer++
                }
                if (
                    item.containsKey("id")
                    && (item["id"] == null
                    || item["id"].toString().isEmpty())
                ) {
                    item["id"] = UUID.randomUUID()
                }
            }
            if (countAnswer > 1 || countAnswer == 0) {
                errorText = "Must be one Answer!\n"
            }
        }
    }
}
