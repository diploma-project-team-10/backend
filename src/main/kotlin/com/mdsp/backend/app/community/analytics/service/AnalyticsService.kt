package com.mdsp.backend.app.community.analytics.service

import com.mdsp.backend.app.community.analytics.model.Analytics
import com.mdsp.backend.app.community.analytics.repository.IAnalyticsRepository
import com.mdsp.backend.app.community.question.repository.IQuestionsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Service
import java.util.*

@EnableAsync
@Service
class AnalyticsService {
    @Autowired
    lateinit var analyticsRepository: IAnalyticsRepository

    @Autowired
    lateinit var questionsRepository: IQuestionsRepository

    @Async
    fun updateUserAnalyticResult(userId: UUID?, questionId: UUID, correct: Boolean) {
        val question = questionsRepository.findByIdAndDeletedAtIsNull(questionId).get()
        val topicId: UUID = question.topicId!!

        if (userId != null) {
            var analytic = Analytics(userId)
            analyticsRepository.findByUserIdAndDeletedAtIsNull(userId).ifPresent { analytic = it }

            if (correct) { analytic.addCorrectAnswer(topicId) } else { analytic.addWrongAnswer(topicId) }

            analyticsRepository.save(analytic)
        }
    }
}
