package com.mdsp.backend.app.calendar.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.mdsp.backend.app.system.model.DateAudit
import org.hibernate.annotations.GenericGenerator
import java.sql.Timestamp
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "calendar_event")
@JsonIgnoreProperties(value = ["deletedAt"], allowGetters = true)
class CalendarEvent: DateAudit() {
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

    @Column(name = "location", columnDefinition = "character varying")
    var location: String? = null

    @Column(name = "is_all_day")
    @JsonProperty("is_all_day")
    var isAllDay: Boolean = false

    @Column(name="start_date")
    @JsonProperty("start_date")
    var startDate: Timestamp? = null

    @Column(name="end_date")
    @JsonProperty("end_date")
    var endDate: Timestamp? = null

    @Column(name = "is_repeat")
    @JsonProperty("is_repeat")
    var isRepeat: Boolean = false

    @Column(name = "repeat_mode")
    @Enumerated(EnumType.STRING)
    @JsonProperty("repeat_mode")
    var repeatMode: RepeatMode = RepeatMode.NEVER

    @Column(name = "repeat_end_mode")
    @Enumerated(EnumType.STRING)
    @JsonProperty("repeat_end_mode")
    var repeatEndMode: RepeatEndMode = RepeatEndMode.NEVER

    @Column(name="repeat_end_date")
    @JsonProperty("repeat_end_date")
    var repeatEndDate: Timestamp? = null

    @Column(name = "color")
    @Enumerated(EnumType.STRING)
    var color: ColorEnum = ColorEnum.BLUE

    @Column(name = "profile_id")
    @JsonIgnoreProperties
    var profileId: UUID? = null

}
