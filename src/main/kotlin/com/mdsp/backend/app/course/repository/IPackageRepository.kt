package com.mdsp.backend.app.course.repository

import com.mdsp.backend.app.course.model.PackageCourse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.util.*
import kotlin.collections.ArrayList

interface IPackageRepository: JpaRepository<PackageCourse, Long> {
    fun findAllByDeletedAtIsNull(): ArrayList<PackageCourse>

    fun findAllByDeletedAtIsNull(page: Pageable): Page<PackageCourse>

    fun findByIdAndDeletedAtIsNull(@Param("id")  id: UUID): Optional<PackageCourse>

    fun existsByIdAndDeletedAtIsNull(@Param("id")  id: UUID): Boolean
}
