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
@Table(name = "teacher")
@TypeDefs(
    TypeDef(
        name = "string-array",
        typeClass = StringArrayType::class
    )
)
class Teacher: DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private var id: UUID? = null

    @Column(name = "fio")
    @JsonProperty("fio")
    private var fio: String? = null

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
        name = "avatar",
        columnDefinition = "character varying(256)[]"
    )
    @JsonProperty("avatar")
    private var avatar: Array<Array<String>> = arrayOf()

    @Column(name = "speciality")
    @JsonProperty("speciality")
    private var speciality: String? = null

    constructor() {}

    fun getId() = this.id
    fun setId(id: UUID?) { this.id = id }

    fun getFio() = this.fio
    fun setFio(fio: String?) { this.fio = fio }

    fun getDescription() = this.description
    fun setDescription(description: String?) { this.description = description }

    fun getDescriptionKz() = this.descriptionKz
    fun setDescriptionKz(descriptionKz: String?) { this.descriptionKz = descriptionKz }

    fun getAvatar() = Util.arrayToMap(this.avatar)
    fun setAvatar(avatar: ArrayList<MutableMap<String, Any?>>) {
        this.avatar = Util.mapToArray(avatar, true)
    }

    fun getSpeciality() = this.speciality
    fun setSpeciality(speciality: String?) { this.speciality = speciality }

}
