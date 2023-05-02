package com.mdsp.backend.app.course.repository

import com.mdsp.backend.app.course.model.LessonProgress
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.util.*
import kotlin.collections.ArrayList

interface ILessonProgressRepository: JpaRepository<LessonProgress, Long> {
    fun findAllByDeletedAtIsNull(): ArrayList<LessonProgress>

    fun findAllByDeletedAtIsNull(page: Pageable): Page<LessonProgress>

    fun findByIdAndDeletedAtIsNull(@Param("id")  id: UUID): Optional<LessonProgress>

    fun findAllByObjectIdAndProfileIdAndTypeAndDeletedAtIsNull(
        @Param("object_id")  objectId: UUID,
        @Param("profile_id")  profileId: UUID,
        @Param("type")  type: String
    ): ArrayList<LessonProgress>

    fun existsByIdAndDeletedAtIsNull(@Param("id")  id: UUID): Boolean
}
