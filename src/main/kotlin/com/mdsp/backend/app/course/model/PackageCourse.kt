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
@Table(name = "package")
@TypeDefs(
    TypeDef(
        name = "string-array",
        typeClass = StringArrayType::class
    )
)
class PackageCourse: DateAudit {
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
        name = "display_name_kz",
        columnDefinition = "character varying"
    )
    @JsonProperty("display_name_kz")
    private var titleKz: String? = null

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
        name = "bg_image",
        columnDefinition = "character varying(256)[]"
    )
    @JsonProperty("bg_image")
    private var bgImage: Array<Array<String>> = arrayOf()

    constructor() {}

    fun getId() = this.id
    fun setId(id: UUID?) { this.id = id }

    fun getTitle() = this.title
    fun setTitle(title: String?) { this.title = title }

    fun getTitleKz() = this.titleKz
    fun setTitleKz(titleKz: String?) { this.titleKz = titleKz }

    fun getDescription() = this.description
    fun setDescription(description: String?) { this.description = description }

    fun getDescriptionKz() = this.descriptionKz
    fun setDescriptionKz(descriptionKz: String?) { this.descriptionKz = descriptionKz }

    fun getBgImage() = Util.arrayToMap(this.bgImage)
    fun setBgImage(bgImage: ArrayList<MutableMap<String, Any?>>) {
        this.bgImage = Util.mapToArray(bgImage, true)
    }

}
