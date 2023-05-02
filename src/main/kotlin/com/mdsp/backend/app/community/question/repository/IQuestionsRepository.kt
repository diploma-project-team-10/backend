package com.mdsp.backend.app.community.question.repository

import com.mdsp.backend.app.community.question.model.Questions
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*
import javax.transaction.Transactional
import kotlin.collections.ArrayList

interface IQuestionsRepository : JpaRepository<Questions, Long> {

    fun findAllByDeletedAtIsNull(page: Pageable): Page<Questions>

    fun findByIdAndDeletedAtIsNull(@Param("id")  id: UUID): Optional<Questions>

    fun findAllByTopicIdAndDeletedAtIsNull(@Param("topic_id") topic_id: UUID): ArrayList<Questions>

    fun findAllByDeletedAtIsNullOrderByTopic(page: Pageable): Page<Questions>

    @Query("SELECT * FROM community_questions AS q \n" +
            "            INNER JOIN community_topics AS t  \n" +
            "            ON q.topic_id = t.id \n" +
            "            WHERE q.deleted_at is null \n" +
            "            ORDER BY t.topic_version ASC", nativeQuery = true)
    fun findAllByDeletedAtIsNullOrderByTopicVersion(page: Pageable): Page<Questions>

    @Query("SELECT * FROM community_questions AS q \n" +
            "            INNER JOIN community_topics AS t  \n" +
            "            ON q.topic_id = t.id \n" +
            "            WHERE q.deleted_at is null \n" +
            "            ORDER BY t.topic_version ASC", nativeQuery = true)
    fun findAllByDeletedAtIsNullOrderByTopicVersion(): List<Questions>

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE community_questions\n" +
            "SET variables = '[{\n" +
            "        \"id\": \"\",\n" +
            "        \"name\": \"result\",\n" +
            "        \"type\": null,\n" +
            "        \"range\": [\n" +
            "            0,\n" +
            "            0\n" +
            "        ],\n" +
            "        \"isAssign\": false,\n" +
            "        \"condition\": \"1==1\",\n" +
            "        \"delimiter\": 0,\n" +
            "        \"assignText\": \"78\"\n" +
            "    }]'\n" +
            "WHERE type != 7 AND variables IS Null", nativeQuery = true)
    fun updateQuestions()

    @Query("SELECT * FROM community_questions WHERE topic_id = :topic_id", nativeQuery = true)
    fun findQuestionByTopic(@Param("topic_id") topic_id: UUID): ArrayList<Questions>

    @Query("SELECT * FROM community_questions WHERE relative_topics = :relatove_topics", nativeQuery = true)
    fun findQuestionByRelativeTopics(@Param("relatove_topics") relatove_topics: UUID): ArrayList<Questions>
}


