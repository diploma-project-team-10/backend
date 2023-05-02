package com.example.comunitybackend.app.community.quiz.payload

import java.util.*

class QuestionRequest {
    private var id: UUID? = null
    private var topicId: String? = null
    private var type: Int = 0
    private var description: String? = null
    private var descriptionRu: String? = null
    private var descriptionEn: String? = null
    private var variants: Array<MutableMap<String, Any?>>? = null
    private var relativeTopics: Array<String>? = null

    constructor() {}

    fun getId() = this.id
    fun getTopicId() = this.topicId
    fun getType() = this.type
    fun getDescription() = this.description
    fun getDescriptionRu() = this.descriptionRu
    fun getDescriptionEn() = this.descriptionEn
    fun getVariants() = this.variants
    fun getRelativeTopics() = this.relativeTopics

    fun setId(id: UUID?) { this.id = id }
    fun setTopicId(topicId: String?) { this.topicId = topicId }
    fun setType(type: Int) { this.type = type }
    fun setDescription(description: String?) { this.description = description }
    fun setDescriptionRu(descriptionRu: String?) { this.descriptionRu = descriptionRu }
    fun setDescriptionEn(descriptionEn: String?) { this.descriptionEn = descriptionEn }
    fun setVariants(variants: Array<MutableMap<String, Any?>>?) {
        this.variants = variants
        if (variants != null) {
            for (item in variants!!) {
                item["id"] = UUID.randomUUID().toString()
            }
        }
    }
    fun setRelativeTopics(relativeTopics: Array<String>?) { this.relativeTopics = relativeTopics }

}
