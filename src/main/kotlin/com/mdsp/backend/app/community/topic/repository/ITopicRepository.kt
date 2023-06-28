package com.mdsp.backend.app.community.topic.repository

import com.mdsp.backend.app.community.topic.model.Topic
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface ITopicRepository: JpaRepository<Topic, Long> {
    fun findAllByDeletedAtIsNullOrderByOrderNum(): ArrayList<Topic>

    fun findByIdAndDeletedAtIsNull(id: UUID): Optional<Topic>

    fun countAllByProgramIdAndParentIdAndDeletedAtIsNull(programId: UUID, parentId: UUID?): Int

    fun findAllByDeletedAtIsNull(): ArrayList<Topic>
    fun findByTitleIgnoreCaseAndParentIdAndProgramIdAndDeletedAtIsNull(title: String, parentId: UUID?, programId: UUID): Optional<Topic>
    fun findAllByProgramIdAndDeletedAtIsNullOrderByTopicVersion(programId: UUID): List<Topic>
    fun findAllByParentIdAndDeletedAtIsNull(parentId: UUID): ArrayList<Topic>
    fun findAllByProgramIdAndParentIdAndDeletedAtIsNull(programId: UUID, parentId: UUID?): ArrayList<Topic>
    fun existsByIdAndDeletedAtIsNull(topicId: UUID): Boolean
}
