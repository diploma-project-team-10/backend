package com.mdsp.backend.trash.calendar.repository

import com.mdsp.backend.trash.calendar.model.Habit
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface IHabitRepository: JpaRepository<Habit, UUID> {
    fun findAllByDeletedAtIsNull(sort: Sort): ArrayList<Habit>

    fun findAllByDeletedAtIsNull(page: Pageable): Page<Habit>

    fun findByIdAndDeletedAtIsNull(@Param("id") id: UUID): Optional<Habit>

    fun existsByIdAndDeletedAtIsNull(@Param("id") id: UUID): Boolean

//    fun findAllByProfileIdAndDeletedAtIsNull(@Param("profileId") profileId: UUID, sort: Sort): ArrayList<Habit>
}
