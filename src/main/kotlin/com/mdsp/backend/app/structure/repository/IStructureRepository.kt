package com.mdsp.backend.app.structure.repository

import com.mdsp.backend.app.structure.model.Structure
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.collections.ArrayList

interface IStructureRepository: JpaRepository<Structure, UUID> {
    fun findAllByDeletedAtIsNull(): ArrayList<Structure>

    fun findAllByDeletedAtIsNull(page: Pageable): Page<Structure>

    fun findByIdAndDeletedAtIsNull(@Param("id")  id: UUID): Optional<Structure>

    fun findAllByIdOrParentIdAndDeletedAtIsNull(@Param("id")  id: UUID, @Param("parentId")  parentId: UUID): ArrayList<Structure>

    fun findAllByParentIdAndDeletedAtIsNullOrderBySortOrderAsc(@Param("parentId")  parentId: UUID?): ArrayList<Structure>

    fun findAllByParentIdAndTypeAndDeletedAtIsNullOrderBySortOrderAsc(@Param("parentId")  parentId: UUID?, @Param("type")  type: String): ArrayList<Structure>

    fun findAllByTypeAndDeletedAtIsNullOrderBySortOrderAsc(@Param("type")  type: String): ArrayList<Structure>

    fun countAllByParentIdAndDeletedAtIsNull(@Param("parentId")  parentId: UUID?): Int

    fun existsByParentIdAndProfileIdAndType(
        @Param("parentId")  parentId: UUID,
        @Param("profileId")  profileId: UUID,
        @Param("type")  type: String
    ): Boolean

    fun findAllByParentIdAndProfileIdAndType(
        @Param("parentId")  parentId: UUID,
        @Param("profileId")  profileId: UUID,
        @Param("type")  type: String
    ): ArrayList<Structure>

    fun findAllByProfileIdAndDeletedAtIsNull(@Param("profileId")  profileId: UUID): ArrayList<Structure>

    @Query(
        value = "SELECT * FROM structure u WHERE u.deleted_at IS NULL " +
                "AND db_array_key_exists(:profileId, u.manager) = 1",
        nativeQuery = true
    )
    fun getListByManager(@Param("profileId")  profileId: String): ArrayList<Structure>

    @Query(
        value = "SELECT * FROM structure u WHERE u.deleted_at IS NULL " +
                "AND db_in_array(:groupIds, u.path) = 1",
        nativeQuery = true
    )
    fun getListByPath(@Param("groupIds") groupIds: UUID): ArrayList<Structure>
//
//    @Transactional
//    @Modifying
//    @Query(value = "UPDATE structure SET parent_id = :parent_id WHERE parent_id =:id",
//            nativeQuery = true)
//    fun fix(@Param("id") id: UUID, @Param("parent_id") parent_id: UUID)
}
