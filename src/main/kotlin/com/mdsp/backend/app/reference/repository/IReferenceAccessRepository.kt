package com.mdsp.backend.app.reference.repository

import com.mdsp.backend.app.reference.model.Access
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.collections.ArrayList

interface IReferenceAccessRepository: JpaRepository<Access, UUID> {
    fun findAllByDeletedAtIsNull(): ArrayList<Access>

    fun findAllByDeletedAtIsNull(page: Pageable): Page<Access>

    fun findByIdAndDeletedAtIsNull(@Param("id") id: UUID): Optional<Access>

    fun findAllByReferenceIdAndDeletedAtIsNull(@Param("referenceId") referenceId: UUID): ArrayList<Access>

    @Transactional
    fun deleteByReferenceId(@Param("referenceId") referenceId: UUID)

}
