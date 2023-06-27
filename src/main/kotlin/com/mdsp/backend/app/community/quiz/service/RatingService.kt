package com.mdsp.backend.app.community.quiz.service

import com.mdsp.backend.app.community.question.repository.IQuestionsRepository
import com.mdsp.backend.app.community.quiz.model.UserRating
import com.mdsp.backend.app.community.quiz.repository.IRatingRepository
import com.mdsp.backend.app.profile.service.ProfileService

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Service
import java.util.*


@Service
@EnableAsync
class RatingService {
    @Autowired
    lateinit var questionRepository: IQuestionsRepository

    @Autowired
    lateinit var userRatingRepository: IRatingRepository

    @Autowired
    lateinit var userService: ProfileService

    fun calculateUserRating(questionId: UUID, answeredCorrectly: Boolean, userRating: Int): Int {

        val question = questionRepository.findByIdAndDeletedAtIsNull(questionId)
            .orElseThrow { RuntimeException("Question not found") }
        val expectedScore = calculateExpectedScore(question.topic?.rating, userRating)
        val actualScore = if (answeredCorrectly) 1 else 0
        val newRating: Int = (question.topic?.rating?:100) + K_FACTOR * (actualScore - expectedScore)
        userService.setUserRating(newRating)
        return newRating
    }

    private fun calculateExpectedScore(questionRating: Int?, userRating: Int): Int {
        val ratingDifference = (userRating - (questionRating?:100)).toDouble()
        val exponent = ratingDifference / 400.0
        return (1 / (1 + Math.pow(10.0, exponent))).toInt()
    }

    fun getUserRatingByProgramId(userId: UUID, programId: UUID): UserRating {
        return userRatingRepository.findByUserIdAndProgramId(userId, programId)
            .orElseGet {
                val rating = UserRating()
                rating.userId = userId
                rating.programId = programId
                rating.rating = INITIAL_RATING
                userRatingRepository.save(rating)
                rating
            }
    }

    companion object {
        private const val INITIAL_RATING = 1500
        private const val K_FACTOR = 32
    }
}
