package com.mdsp.backend.trash.calendar.repository

import com.mdsp.backend.trash.calendar.model.CalendarEvent
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.util.*
import kotlin.collections.ArrayList

interface ICalendarEventRepository: JpaRepository<CalendarEvent, UUID> {
    fun findAllByDeletedAtIsNull(sort: Sort): ArrayList<CalendarEvent>

    fun findAllByDeletedAtIsNull(page: Pageable): Page<CalendarEvent>

    fun findByIdAndDeletedAtIsNull(@Param("id") id: UUID): Optional<CalendarEvent>

    fun existsByIdAndDeletedAtIsNull(@Param("id") id: UUID): Boolean

    fun findAllByProfileIdAndDeletedAtIsNull(@Param("profileId") profileId: UUID, sort: Sort): ArrayList<CalendarEvent>

}
