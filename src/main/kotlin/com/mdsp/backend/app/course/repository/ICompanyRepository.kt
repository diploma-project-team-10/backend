package com.mdsp.backend.app.course.repository

import com.mdsp.backend.app.course.model.Company
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.util.*
import kotlin.collections.ArrayList

interface ICompanyRepository: JpaRepository<Company, Long> {
    fun findAllByDeletedAtIsNull(): ArrayList<Company>

    fun findAllByDeletedAtIsNull(page: Pageable): Page<Company>

    fun findByIdAndDeletedAtIsNull(@Param("id")  id: UUID): Optional<Company>

    fun existsByIdAndDeletedAtIsNull(@Param("id")  id: UUID): Boolean
}
