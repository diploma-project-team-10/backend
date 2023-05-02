package com.mdsp.backend.app.course.repository

import com.mdsp.backend.app.course.model.Lesson
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.util.*
import kotlin.collections.ArrayList

interface ILessonRepository: JpaRepository<Lesson, Long> {
    fun findAllByDeletedAtIsNull(): ArrayList<Lesson>

    fun findAllByDeletedAtIsNull(page: Pageable): Page<Lesson>

    fun findByIdAndDeletedAtIsNull(@Param("id")  id: UUID): Optional<Lesson>

    fun existsByIdAndDeletedAtIsNull(@Param("id")  id: UUID): Boolean

}
