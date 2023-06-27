package com.mdsp.backend.app.community.quiz.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.mdsp.backend.app.community.question.payload.Variant
import com.mdsp.backend.app.system.model.DateAudit
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import javax.persistence.Column
import javax.persistence.EntityListeners
import javax.persistence.MappedSuperclass

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
@JsonIgnoreProperties(value = ["deletedAt"], allowGetters = false)
@TypeDefs(
    TypeDef(
        name = "jsonb",
        typeClass = JsonBinaryType::class
    )
)
abstract class QuizAudit : DateAudit() {

    @Column(name = "description", columnDefinition="TEXT")
    var description: String? = null

    @Column(name = "descriptionRu", columnDefinition="TEXT")
    var descriptionRu: String? = null

    @Column(name = "descriptionEn", columnDefinition="TEXT")
    var descriptionEn: String? = null

    @Type(type = "jsonb")
    @Column(
        name = "variants",
        columnDefinition = "jsonb"
    )
    var variants: ArrayList<Variant> = arrayListOf()
}
