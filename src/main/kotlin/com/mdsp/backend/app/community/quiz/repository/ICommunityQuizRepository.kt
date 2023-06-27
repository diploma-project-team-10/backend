package com.mdsp.backend.app.community.quiz.repository

import com.mdsp.backend.app.community.quiz.model.CommunityQuiz
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.util.*


interface ICommunityQuizRepository: JpaRepository<CommunityQuiz, Long> {

    fun findAllBy(pagePR: Pageable): Page<CommunityQuiz>
    fun findByIdAndDeletedAtIsNull(@Param("id") id: UUID?): Optional<CommunityQuiz>

}
