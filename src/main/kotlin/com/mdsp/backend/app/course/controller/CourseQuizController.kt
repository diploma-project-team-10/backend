package com.mdsp.backend.app.course.controller

import com.mdsp.backend.app.course.model.LessonProgress
import com.mdsp.backend.app.course.model.Quiz
import com.mdsp.backend.app.course.model.payload.QuizClientResponse
import com.mdsp.backend.app.course.model.payload.QuizPassResponse
import com.mdsp.backend.app.course.repository.ILessonProgressRepository
import com.mdsp.backend.app.course.repository.IQuizRepository
import com.mdsp.backend.app.profile.repository.IProfileRepository
import com.mdsp.backend.app.system.model.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid
import kotlin.collections.ArrayList
import kotlin.math.roundToInt


@RestController
@RequestMapping("/api/project/course/quiz")
class CourseQuizController {
    @Autowired
    lateinit var quizRepository: IQuizRepository

    @Autowired
    lateinit var lessonProgressRepository: ILessonProgressRepository

    @Autowired
    lateinit var profileRepository: IProfileRepository

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    fun getQuizs() = quizRepository.findAllByDeletedAtIsNull()


    @GetMapping("/list-page")
    @PreAuthorize("isAuthenticated()")
    fun getQuizzesPage(
        @RequestParam(value = "page") page: Int = 1,
        @RequestParam(value = "size") size: Int = 20
    ): Page<Quiz>? {
        val pageImp: PageRequest = PageRequest.of(page - 1, size)
        return quizRepository.findAllByDeletedAtIsNull(pageImp)
    }

    @GetMapping("/get/{id}")
    @PreAuthorize("isAuthenticated()")
    fun getQuiz(@PathVariable(value = "id") id: UUID) = quizRepository.findByIdAndDeletedAtIsNull(id)

    @GetMapping("/get/client/{id}")
    @PreAuthorize("isAuthenticated()")
    fun getQuizClient(@PathVariable(value = "id") id: UUID): Optional<QuizClientResponse> {
        val quiz = quizRepository.findByIdAndDeletedAtIsNull(id)
        if (quiz.isPresent) {
            return Optional.of(QuizClientResponse(quiz.get()))
        }
        return Optional.empty()
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    fun createQuiz(@Valid @RequestBody newQuiz: Quiz): ResponseEntity<*> {
        val status = Status()
        quizRepository.save(newQuiz)

        status.status = 1
        status.message = "New Quiz created!"
        status.value = newQuiz.getId()
        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun editQuiz(
        @PathVariable(value = "id") id: UUID,
        @Valid @RequestBody newQuiz: Quiz
    ): ResponseEntity<*> {
        val status = Status()
        status.message = "Quiz doesn't save!"
        val quiz = quizRepository.findByIdAndDeletedAtIsNull(id)
        if (quiz.isPresent) {
            if (newQuiz.getCourseId() == null) {
                newQuiz.setCourseId(quiz.get().getCourseId())
            }
            if (newQuiz.getLessonId() == null) {
                newQuiz.setLessonId(quiz.get().getLessonId())
            }
            quizRepository.save(newQuiz)

            status.status = 1
            status.message = "New Quiz created!"
            status.value = newQuiz.getId()
        }

        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/check")
    @PreAuthorize("hasRole('ADMIN')")
    fun checkQuiz(
        @Valid @RequestBody payloadQuiz: QuizPassResponse,
        authentication: Authentication,
    ): ResponseEntity<*> {
        val status = Status()
        status.message = "Result!"
        var correct = 0
        val profileId = profileRepository.findByUsernameAndDeletedAtIsNull(authentication.name).get().getId()
        if (payloadQuiz.getId() != null && profileId != null) {
            val quiz = quizRepository.findByIdAndDeletedAtIsNull(payloadQuiz.getId()!!)
            if (quiz.isPresent) {
                for (question in quiz.get().getQuestions()) {
                    when (question["type"]) {
                        "test" -> {
                            val answered = payloadQuiz.getAnswered().filter { answer -> answer["id"] == question["id"] }
                            if (answered.size == 1 && answered[0]["selectedAnswers"] == question["answer"]) {
                                correct++
                            }
                        }
                        "multiple" -> {
                            val answered = payloadQuiz.getAnswered().filter { answer -> answer["id"] == question["id"] }
                            val answers = (question["answers"] as ArrayList<MutableMap<String, Any?>>).filter { answer -> answer["isAnswer"] == true }

                            if (
                                answered.size == 1
                                && (answered[0]["selectedAnswers"] as ArrayList<Any>).isNotEmpty()
                                && (answered[0]["selectedAnswers"] as ArrayList<Any>).size == answers.size
                            ) {
                                val ansPass = (answered[0]["selectedAnswers"] as ArrayList<Any>)
                                var isTrue = true
                                for (answer in answers) {
                                    if (!ansPass.contains(answer["id"])) {
                                        isTrue = false
                                        break
                                    }
                                }
                                if (isTrue) {
                                    correct++
                                }
                            }
                        }
                    }
                }
                val correctPercent = ((correct * 100.0) / quiz.get().getQuestions().size).roundToInt()
                status.value = mutableMapOf("correct" to correct, "total" to quiz.get().getQuestions().size, "percent" to correctPercent)
                if (quiz.get().getPassingScore() != null && quiz.get().getPassingScore()!! <= correctPercent) {
                    status.status = 1
                    val progress = lessonProgressRepository.findAllByObjectIdAndProfileIdAndTypeAndDeletedAtIsNull(quiz.get().getId()!!, profileId, "quiz")
                    if (progress.isEmpty()) {
                        val newProgress = LessonProgress()
                        newProgress.setProfileId(profileId)
                        newProgress.setObjectId(quiz.get().getId()!!)
                        newProgress.setMarkedDate(null)
                        newProgress.setType("quiz")
                        lessonProgressRepository.save(newProgress)
                    }
                }
            }
        }

        return ResponseEntity(status, HttpStatus.OK)
    }
}
