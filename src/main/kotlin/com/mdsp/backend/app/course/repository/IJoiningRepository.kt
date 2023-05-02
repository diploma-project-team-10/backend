package com.mdsp.backend.app.course.repository

import com.mdsp.backend.app.course.model.Joining
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.util.*
import kotlin.collections.ArrayList

interface IJoiningRepository: JpaRepository<Joining, Long> {
    fun findAllByDeletedAtIsNull(): ArrayList<Joining>

    fun findAllByDeletedAtIsNull(page: Pageable): Page<Joining>

    fun findByIdAndDeletedAtIsNull(@Param("id")  id: UUID): Optional<Joining>

    fun findAllByCourseIdAndProfileIdAndDeletedAtIsNull(@Param("course_id")  courseId: UUID, @Param("profile_id")  profileId: UUID): ArrayList<Joining>

    fun findAllByProfileIdAndDeletedAtIsNull(@Param("profile_id")  profileId: UUID): ArrayList<Joining>

    fun existsByIdAndDeletedAtIsNull(@Param("id")  id: UUID): Boolean
}
