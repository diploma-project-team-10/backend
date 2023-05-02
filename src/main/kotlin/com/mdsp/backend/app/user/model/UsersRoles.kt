package com.mdsp.backend.app.user.model

import java.sql.Timestamp
import java.util.*
import javax.persistence.*

//TODO DON'T USE
@Entity
@Table(name = "users_roles")
data class UsersRoles (

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id")
        val id: Long = 0,

        @Column(name="user_id")
        var userId: UUID? = null,

        @Column(name="role_id")
        val roleId: Long = 0,

        @Column(name="deleted_at")
        var deletedAt: Timestamp? = null

)
