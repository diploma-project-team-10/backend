package com.mdsp.backend.app.course.repository

import com.mdsp.backend.app.course.model.Teacher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface ITeacherRepository: JpaRepository<Teacher, Long> {
    fun findAllByDeletedAtIsNull(): ArrayList<Teacher>

    fun findAllByDeletedAtIsNull(page: Pageable): Page<Teacher>

    fun findByIdAndDeletedAtIsNull(@Param("id")  id: UUID): Optional<Teacher>

    fun existsByIdAndDeletedAtIsNull(@Param("id")  id: UUID): Boolean
}
