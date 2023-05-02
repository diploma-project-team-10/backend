package com.mdsp.backend.app.community.program.repository

import com.mdsp.backend.app.community.program.model.Program
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.util.*
import kotlin.collections.ArrayList

interface IProgramRepository: JpaRepository<Program, Long> {
    fun findAllByDeletedAtIsNull(pagePR: Pageable):  Page<Program>
    fun findAllByDeletedAtIsNull():  ArrayList<Program>
    fun findByIdAndDeletedAtIsNull(@Param("id") id: UUID): Optional<Program>
}
