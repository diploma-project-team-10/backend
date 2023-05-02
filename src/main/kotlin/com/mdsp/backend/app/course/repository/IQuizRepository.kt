package com.mdsp.backend.app.course.repository

import com.mdsp.backend.app.course.model.Quiz
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.util.*
import kotlin.collections.ArrayList

interface IQuizRepository: JpaRepository<Quiz, Long> {
    fun findAllByDeletedAtIsNull(): ArrayList<Quiz>

    fun findAllByDeletedAtIsNull(page: Pageable): Page<Quiz>

    fun findByIdAndDeletedAtIsNull(@Param("id")  id: UUID): Optional<Quiz>

    fun existsByIdAndDeletedAtIsNull(@Param("id")  id: UUID): Boolean

    fun findAllByCourseIdAndDeletedAtIsNullOrderByOrderNum(@Param("course_id")  courseId: UUID): ArrayList<Quiz>

    fun findAllByLessonIdAndDeletedAtIsNullOrderByOrderNum(@Param("lesson_id")  lessonId: UUID): ArrayList<Quiz>
}
