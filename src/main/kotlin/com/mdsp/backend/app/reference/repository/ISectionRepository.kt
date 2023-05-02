package com.mdsp.backend.app.reference.repository

import com.mdsp.backend.app.reference.model.Reference
import com.mdsp.backend.app.reference.model.Section
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.util.*
import kotlin.collections.ArrayList

interface ISectionRepository: JpaRepository<Section, UUID> {
    fun findAllByDeletedAtIsNull(): ArrayList<Section>

    fun findAllByDeletedAtIsNull(page: Pageable): Page<Section>

    fun findByIdAndDeletedAtIsNull(@Param("id")  id: UUID): Optional<Section>

    fun findAllByReferenceIdAndDeletedAtIsNullOrderByOrderNum(@Param("referenceId")  referenceId: UUID): ArrayList<Section>

//    fun findByParentIdAndDeletedAtIsNull(@Param("parentId")  parentId: UUID?): Optional<Group>

//    @Query("SELECT DISTINCT CAST(profile_id AS character varying) FROM educations " +
//            "WHERE year_start <=:now AND year_end >=:now AND LOWER(speciality) IN (:specs)",
//            nativeQuery = true)
//    fun getListSpeciality(@Param("now") now: Date, @Param("specs") specs: List<Any>): List<Any>
}
