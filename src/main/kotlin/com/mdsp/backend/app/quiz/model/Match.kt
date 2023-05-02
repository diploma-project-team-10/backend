package com.mdsp.backend.app.quiz.model

import com.mdsp.backend.app.system.model.Util
import java.util.*
import kotlin.collections.ArrayList

class Match: IQuiz {

    override var variantsMap: ArrayList<MutableMap<String, Any?>> = arrayListOf()
    override var errorText: String = ""

    override fun setVariantsMap(variants: String?) {
        if (variants != null) {
            this.variantsMap = Util.toArrayMap(variants)
            var isEmpty = false
            for (item in this.variantsMap) {
                for (itemMap in item) {
                    if (
                        !arrayListOf("id", "text", "textRu", "textEn",
                            "textRel", "textRuRel", "textEnRel", "relatedId").contains(itemMap.key)
                    ) {
                        item.remove(itemMap.key)
                    }
                }
                if (
                    item.containsKey("text") && item["text"].toString().isEmpty()
                    && item.containsKey("textRu") && item["textRu"].toString().isEmpty()
                    && item.containsKey("textEn") && item["textEn"].toString().isEmpty()
                    && item.containsKey("textRel") && item["textRel"].toString().isEmpty()
                    && item.containsKey("textRuRel") && item["textRuRel"].toString().isEmpty()
                    && item.containsKey("textEnRel") && item["textEnRel"].toString().isEmpty()
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
                if (
                    item.containsKey("relatedId")
                    && (item["relatedId"] == null
                    || item["relatedId"].toString().isEmpty())
                ) {
                    item["relatedId"] = UUID.randomUUID()
                }
            }
            if (isEmpty) {
                errorText = "Must be one Answer!\n"
            }
        }
    }
}
