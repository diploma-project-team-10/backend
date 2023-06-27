package com.mdsp.backend.app.community.quiz.controller

import com.mdsp.backend.app.community.quiz.model.CommunityQuiz
import com.mdsp.backend.app.community.quiz.payload.JsonQuiz
import com.mdsp.backend.app.community.quiz.repository.ICommunityQuizRepository
import com.mdsp.backend.app.community.quiz.service.QuizService
import com.mdsp.backend.app.profile.repository.IProfileRepository
import com.mdsp.backend.app.system.model.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/project/community/quiz")
class QuizController {

    @Autowired
    lateinit var quizRepository: ICommunityQuizRepository

    @Autowired
    lateinit var quizService: QuizService

    @Autowired
    lateinit var profileRepository: IProfileRepository

    @GetMapping("/get/{quizId}")
    fun getQuiz(@PathVariable("quizId") quizId: UUID?) =
            quizRepository.findByIdAndDeletedAtIsNull(quizId)

    @GetMapping("/start/{id}")
    fun startQuiz(@PathVariable("id") programId: UUID): Status {
        val status = Status()
        status.status = 0
        status.message = "Something went wrong!(Sorry)"
        val quiz = CommunityQuiz(null, programId)
        quizRepository.save(quiz)
        status.status = 1
        status.value = quiz.id
        status.message = "Quiz Started"
        return status
    }

    @PostMapping("/getQuestion")
    fun getNextQuestion(
        @Valid @RequestBody body: JsonQuiz,
        authentication: Authentication
    ): Status {
        val profileId = profileRepository.findByUsernameAndDeletedAtIsNull(authentication.name).get().getId()
            ?: return Status()
        return quizService.getNextQuestion(profileId, body)
    }
}
