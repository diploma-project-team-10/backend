package com.mdsp.backend.app.community.quiz.repository

import com.mdsp.backend.app.community.quiz.model.Quiz
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.util.*


interface IQuizRepository: JpaRepository<Quiz, Long> {

    fun findAllBy(pagePR: Pageable): Page<Quiz>?
    fun findByIdAndDeletedAtIsNull(@Param("id") id: UUID?): Optional<Quiz>?

}
