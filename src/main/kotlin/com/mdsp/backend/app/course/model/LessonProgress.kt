package com.mdsp.backend.app.course.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.mdsp.backend.app.system.model.DateAudit
import java.sql.Timestamp
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "lesson_progress")
class LessonProgress() : DateAudit() {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private var id: UUID? = null

    @Column(name = "profile_id")
    @JsonProperty("profile_id")
    private var profileId: UUID? = null

    @Column(name = "object_id")
    @JsonProperty("object_id")
    private var objectId: UUID? = null

    @Column(name = "type")
    @JsonProperty("type")
    private var type: String? = "lesson"

    @Column(name = "marked_date")
    @JsonProperty("marked_date")
    private var markedDate: Timestamp? = Timestamp(System.currentTimeMillis())

    fun getId() = this.id
    fun setId(id: UUID?) {
        this.id = id
    }

    fun getProfileId() = this.profileId
    fun setProfileId(profileId: UUID) {
        this.profileId = profileId
    }

    fun getObjectId() = this.objectId
    fun setObjectId(objectId: UUID) {
        this.objectId = objectId
    }

    fun getType() = this.type
    fun setType(type: String?) {
        this.type = type
    }

    fun getMarkedDate() = this.markedDate
    fun setMarkedDate(markedDate: Timestamp?) {
        this.markedDate = Timestamp(System.currentTimeMillis())
        if (markedDate != null) {
            this.markedDate = markedDate
        }
    }
}
