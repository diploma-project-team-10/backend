package com.mdsp.backend.app.user.repository

import com.mdsp.backend.app.user.model.UsersRoles
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.util.*

//TODO DON'T USE
interface UsersRoleRepository : JpaRepository<UsersRoles, Long> {
    fun findByUserId(@Param("user_id") userId: UUID): List<UsersRoles>
}
