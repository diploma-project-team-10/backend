package com.mdsp.backend.app.course.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.mdsp.backend.app.system.model.DateAudit
import com.mdsp.backend.app.system.model.Util
import com.vladmihalcea.hibernate.type.array.StringArrayType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "company")
@TypeDefs(
    TypeDef(
        name = "string-array",
        typeClass = StringArrayType::class
    )
)
class Company() : DateAudit() {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private var id: UUID? = null

    @Column(
        name = "display_name",
        columnDefinition = "character varying"
    )
    @JsonProperty("display_name")
    private var title: String? = null

    @Column(
        name = "description",
        columnDefinition = "text"
    )
    @JsonProperty("description")
    private var description: String? = null

    @Column(
        name = "description_kz",
        columnDefinition = "text"
    )
    @JsonProperty("description_kz")
    private var descriptionKz: String? = null

    @Type(type = "string-array")
    @Column(
        name = "logo",
        columnDefinition = "character varying(256)[]"
    )
    @JsonProperty("logo")
    private var logo: Array<Array<String>> = arrayOf()

    fun getId() = this.id
    fun setId(id: UUID?) { this.id = id }

    fun getTitle() = this.title
    fun setTitle(title: String?) { this.title = title }

    fun getDescription() = this.description
    fun setDescription(description: String?) { this.description = description }

    fun getDescriptionKz() = this.descriptionKz
    fun setDescriptionKz(descriptionKz: String?) { this.descriptionKz = descriptionKz }

    fun getLogo(): ArrayList<MutableMap<String, Any?>> = Util.arrayToMap(this.logo)
//    fun setLogo(logo: Array<Array<String>>) { this.logo = logo }
    fun setLogo(logo: ArrayList<MutableMap<String, Any?>>) {
        this.logo = Util.mapToArray(logo, true)
    }

}
