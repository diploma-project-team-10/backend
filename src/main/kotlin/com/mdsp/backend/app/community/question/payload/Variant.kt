package com.mdsp.backend.app.community.question.payload

import java.util.UUID

class Variant {
    var id: UUID? = null
    var text: String? = null
    var textRu: String? = null
    var textEn: String? = null
    var textRel: String? = null
    var textRuRel: String? = null
    var textEnRel: String? = null
    var isAnswer: Boolean? = null
    var orderNum: Int? = null

    override fun toString(): String {
        return "id=${this.id} text=${this.text} isAnswer=${this.isAnswer}"
    }

    fun clone(): Variant {
        val temp = Variant()
        temp.id = (UUID.randomUUID())
        temp.text = (this.text)
        temp.textEn = (this.textEn)
        temp.textRu = (this.textRu)
        temp.isAnswer = (this.isAnswer)
        return temp
    }

    override fun equals(other: Any?): Boolean {
        if (other is Variant) {
            if (
                (other.text == this.text && this.text!!.isNotEmpty()) ||
                (other.textRu == this.textRu && this.textRu!!.isNotEmpty()) ||
                (other.textEn == this.textEn && this.textEn!!.isNotEmpty())
            ) {
                return true
            }
            return false
        }
        return false
    }
}
