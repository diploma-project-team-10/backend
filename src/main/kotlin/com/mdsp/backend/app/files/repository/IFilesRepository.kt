package com.mdsp.backend.app.files.repository

import com.mdsp.backend.app.files.model.Files
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface IFilesRepository: JpaRepository<Files, UUID> {
    fun findByIdAndDeletedAtIsNull(@Param("id") id: UUID): Optional<Files>

    fun findAllByHashFileAndDeletedAtIsNull(@Param("hashFile") hashFile: String): ArrayList<Files>
}
