package com.mdsp.backend.app.community.topic.repository

import com.mdsp.backend.app.community.topic.model.Topic
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface ITopicRepository: JpaRepository<Topic, Long> {
    fun findAllByDeletedAtIsNullOrderByOrderNum(): ArrayList<Topic>

    fun findByIdAndDeletedAtIsNull(@Param("id") id: UUID): Optional<Topic>

    fun findByTitleIgnoreCaseAndParentIdAndDeletedAtIsNull(@Param("title") title: String, @Param("parentId")  parentId: UUID?): Optional<Topic>

    fun findAllByParentIdAndDeletedAtIsNull(@Param("id") id: UUID?): ArrayList<Topic>

    fun findAllByDeletedAtIsNull(): ArrayList<Topic>;
}
