package com.mdsp.backend.app.course.repository

import com.mdsp.backend.app.course.model.Course
import com.mdsp.backend.app.course.model.StatusCourse
import com.mdsp.backend.app.course.model.payload.CourseResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.sql.Timestamp
import java.util.*
import kotlin.collections.ArrayList

interface ICourseRepository: JpaRepository<Course, Long> {
    fun findAllByDeletedAtIsNull(): ArrayList<CourseResponse>

    fun findAllByDeletedAtIsNull(page: Pageable): Page<CourseResponse>

    fun findAllByDeletedAtIsNullOrderByOrderNum(page: Pageable): Page<CourseResponse>

    fun findAllByStatusAndPublishDateAfterAndDeletedAtIsNullOrderByOrderNum(
        @Param("status") status: StatusCourse,
        @Param("publish") publish: Timestamp?,
        page: Pageable
    ): Page<CourseResponse>

    @Query("SELECT * FROM course c WHERE c.deleted_at IS NULL " +
            "AND (lower(c.display_name) LIKE lower(:search)  OR lower(c.description) LIKE lower(:search) " +
            "OR lower(c.intro_description) LIKE lower(:search))", nativeQuery = true)
    fun getCurrentCoursesClient(
        @Param("search") search: String?,
        page: Pageable
    ): Page<Course>

    @Query("SELECT * FROM course c WHERE c.deleted_at IS NULL AND id IN :ids", nativeQuery = true)
    fun getCurrentMyCoursesClient(
        @Param("ids") ids: Array<UUID>,
        page: Pageable
    ): Page<Course>

    @Query("SELECT * FROM course c WHERE c.deleted_at IS NULL " +
            "AND db_array_key_exists(:packageId, c.packages) = 1", nativeQuery = true)
    fun getCoursesByPackage(
        @Param("packageId") packageId: String,
        page: Pageable
    ): Page<Course>

    fun findAllByStatusAndPublishDateBeforeAndDeletedAtIsNullOrderByOrderNum(
        @Param("status") status: StatusCourse,
        @Param("publish") publish: Timestamp?,
        page: Pageable
    ): Page<CourseResponse>

    fun findAllByStatusAndDeletedAtIsNullOrderByOrderNum(
        @Param("status") status: StatusCourse,
        page: Pageable
    ): Page<CourseResponse>

    fun findByIdAndDeletedAtIsNull(@Param("id") id: UUID): Optional<Course>

    fun existsByIdAndDeletedAtIsNull(@Param("id") id: UUID): Boolean
}
