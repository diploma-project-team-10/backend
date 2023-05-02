package com.mdsp.backend.app.quiz.model

import com.mdsp.backend.app.system.model.Util
import java.util.*
import kotlin.collections.ArrayList

class Order: IQuiz {

    override var variantsMap: ArrayList<MutableMap<String, Any?>> = arrayListOf()
    override var errorText: String = ""

    override fun setVariantsMap(variants: String?) {
        if (variants != null) {
            this.variantsMap = Util.toArrayMap(variants)
            var isEmpty = false
            for (item in this.variantsMap) {
                for (itemMap in item) {
                    if (!arrayListOf("id", "text", "textRu", "textEn").contains(itemMap.key)) {
                        item.remove(itemMap.key)
                    }
                }
                if (
                    item.containsKey("text") && item["text"].toString().isEmpty()
                    && item.containsKey("textRu") && item["textRu"].toString().isEmpty()
                    && item.containsKey("textEn") && item["textEn"].toString().isEmpty()
                ) {
                    isEmpty = true
                }
                if (
                    item.containsKey("id")
                    && (item["id"] == null
                    || item["id"].toString().isEmpty())
                ) {
                    item["id"] = UUID.randomUUID()
                }
            }
            if (isEmpty) {
                errorText = "Must be not Empty!\n"
            }
        }
    }
}
