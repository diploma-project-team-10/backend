package com.mdsp.backend.app.course.repository

import com.mdsp.backend.app.course.model.ModuleCourse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.util.*
import kotlin.collections.ArrayList

interface IModulesRepository: JpaRepository<ModuleCourse, Long> {
    fun findAllByDeletedAtIsNull(): ArrayList<ModuleCourse>

    fun findAllByDeletedAtIsNull(page: Pageable): Page<ModuleCourse>

    fun findByIdAndDeletedAtIsNull(@Param("id")  id: UUID): Optional<ModuleCourse>

    fun existsByIdAndDeletedAtIsNull(@Param("id")  id: UUID): Boolean

    fun findAllByCourseIdAndDeletedAtIsNullOrderByOrderNum(@Param("course_id")  courseId: UUID): ArrayList<ModuleCourse>
}
