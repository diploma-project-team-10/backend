package com.mdsp.backend.trash.calendar.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.mdsp.backend.app.system.model.DateAudit
import com.mdsp.backend.app.system.model.Util
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.*
import kotlin.jvm.Transient

@Entity
@Table(name = "habit")
@JsonIgnoreProperties(value = ["deletedAt", "isActiveProfile"], allowGetters = true)
class Habit: DateAudit() {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = true, nullable = false)
    var id: UUID? = null

    @Column(name = "display_name", columnDefinition = "character varying")
    @JsonProperty("display_name")
    var title: String? = null

    @Column(name = "description")
    var description: String? = ""

    @Type(type = "string-array")
    @Column(name = "is_active_profile", columnDefinition = "character varying[][]", nullable = false)
    private var calendarEvent: Array<Array<String>> = arrayOf()

    fun getCalendarEvent() = Util.arrayToMap(this.calendarEvent)
    fun setCalendarEvent(isActiveProfile: ArrayList<MutableMap<String, Any?>>) {
        this.calendarEvent = Util.mapToArray(isActiveProfile)
    }

    @Transient
    var isActive: Boolean = false;
}
