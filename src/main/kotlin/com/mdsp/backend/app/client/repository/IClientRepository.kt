package com.mdsp.backend.app.client.repository

import com.mdsp.backend.app.client.model.Client
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.util.*
import kotlin.collections.ArrayList

interface IClientRepository: JpaRepository<Client, Long> {
    fun findByIdAndDeletedAtIsNull(@Param("id")  id: UUID): Optional<Client>

    fun findAllByDeletedAtIsNull(): ArrayList<Client>
}
