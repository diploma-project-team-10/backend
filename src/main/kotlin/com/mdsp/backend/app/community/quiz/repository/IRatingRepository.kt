package com.mdsp.backend.app.community.quiz.repository

import com.mdsp.backend.app.community.quiz.model.UserRating
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface IRatingRepository: JpaRepository<UserRating, UUID> {
    fun findByUserIdAndProgramId(userId: UUID, programId: UUID): Optional<UserRating>
}
