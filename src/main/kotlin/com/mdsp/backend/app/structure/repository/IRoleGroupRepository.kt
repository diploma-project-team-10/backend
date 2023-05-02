package com.mdsp.backend.app.structure.repository

import com.mdsp.backend.app.structure.model.RolesGroup
import com.mdsp.backend.app.structure.model.Structure
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.util.*
import kotlin.collections.ArrayList

interface IRoleGroupRepository: JpaRepository<RolesGroup, UUID> {
    fun findAllByDeletedAtIsNull(): ArrayList<RolesGroup>

    fun findAllByDeletedAtIsNull(page: Pageable): Page<RolesGroup>

    fun findByIdAndDeletedAtIsNull(@Param("id")  id: UUID): Optional<RolesGroup>

    fun findAllByKeyAndDeletedAtIsNull(key: String): ArrayList<RolesGroup>
}
