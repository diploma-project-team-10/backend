package com.mdsp.backend.app.community.question.controller

import com.mdsp.backend.app.community.question.service.QuestionService
import com.mdsp.backend.app.community.question.model.Questions
import com.mdsp.backend.app.community.question.payload.GenerateExerciseRequest
import com.mdsp.backend.app.community.question.payload.GeneratedQuestion
import com.mdsp.backend.app.community.question.payload.Variable
import com.mdsp.backend.app.community.question.payload.Variant
import com.mdsp.backend.app.community.question.repository.IQuestionsRepository
import com.mdsp.backend.app.community.topic.repository.ITopicRepository
import com.mdsp.backend.app.system.model.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Async
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.sql.Timestamp
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/project/community/questions")
class QuestionController {

    @Autowired
    lateinit var questionsRepository: IQuestionsRepository

    @Autowired
    lateinit var topicRepository: ITopicRepository

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    fun getQuestions(
        @RequestParam(value = "page") page: Int = 1,
        @RequestParam(value = "size") size: Int = 20
    ): Page<Questions> {
        val pagePR: PageRequest = PageRequest.of(page - 1, size)
        return questionsRepository.findAllByDeletedAtIsNullOrderByTopicVersion(pagePR)
    }

    @GetMapping("/filter")
    @PreAuthorize("isAuthenticated()")
    fun getQuestionsById(@Valid @RequestBody topicsArray: Array<String>): Status {
        val status = Status()
        val resultQuestions: ArrayList<Questions> =  arrayListOf()
        for(id in topicsArray){
            val questions: ArrayList<Questions> = questionsRepository.findAllByTopicIdAndDeletedAtIsNull(UUID.fromString(id))
            resultQuestions += questions
        }
        status.value = resultQuestions
        return status
    }

    @GetMapping("/get/{problemId}")
    @PreAuthorize("isAuthenticated()")
    fun getReference(@PathVariable(value = "problemId") problemId: UUID) = questionsRepository.findByIdAndDeletedAtIsNull(problemId)

    @DeleteMapping("/delete/{problemId}")
    @PreAuthorize("isAuthenticated()")
    fun deleteProblem(@PathVariable(value = "problemId") problemId: UUID): Status {
        val status = Status()
        val questionCandidate: Optional<Questions> = questionsRepository.findByIdAndDeletedAtIsNull(problemId)

        if (questionCandidate.isPresent) {
            questionCandidate.get().deletedAt = (Timestamp(System.currentTimeMillis()))
            questionsRepository.save(questionCandidate.get())
        } else {
            status.status = 0
            status.message = "Question does not exist!"
            return status
        }
        status.status = 1
        status.message = "Question deleted!"
        return status
    }

    @PostMapping("/create/not-generated")
    @PreAuthorize("isAuthenticated()")
    fun saveQuestion(
        @Valid @RequestBody newQuestion: Questions,
    ): Status {
        val status = Status()
        status.message = ""
        status.status = 0
        if (newQuestion.topicId == null) {
            status.message = "Select Topic!"
            return status
        }

        val children = topicRepository.findAllByParentIdAndDeletedAtIsNull(newQuestion.topicId)
        if(children.size != 0){
            status.status = 0
            status.message = "Children topic exist!"
            return status
        }

        if (newQuestion.errorText.isEmpty()) {
            if (newQuestion.id != null) {
                newQuestion.updatedAt = (Timestamp(System.currentTimeMillis()))
                status.message = "Question saved!"
            } else {
                status.message = "Question created!"
            }
            questionUpdateRating()
            questionsRepository.save(newQuestion)
            status.status = 1
        } else {
            status.message = newQuestion.errorText
        }

        status.value = newQuestion
        return status
    }

    @Async
    fun questionUpdateRating() {
        questionsRepository.findAllByDeletedAtIsNullOrderByTopicVersion().forEachIndexed { index, question ->
            question.rating = 600 + (index + 1) * 15
            questionsRepository.save(question)
        }
    }


    @PostMapping("/generated")
    @PreAuthorize("isAuthenticated()")
    private fun getGeneratedExercise(
        @Valid @RequestBody requestExercise: GenerateExerciseRequest,
    ): Status {
        val status = Status()
        status.status = 1
        status.message = ""

        try {
            val variables: ArrayList<Variable> = requestExercise.variable
            val customVariants: ArrayList<Variant> = requestExercise.customVariants
            var correctAnswer: String? = "Incorrect"

            var stepSolving = 0;
            while (correctAnswer.equals("Incorrect")) {
                if (stepSolving > 10) throw Exception("Condition or range is wrong! (result)")

                QuestionService.generateVariables(variables)

                correctAnswer = QuestionService.getResultAnswer(variables)
                stepSolving++;
            }

            val responseExercise = GeneratedQuestion()
            responseExercise.answerType = (requestExercise.answerType)
            responseExercise.description = (QuestionService.reconstructStatement(requestExercise.description?:"", variables))
            responseExercise.descriptionEn = (QuestionService.reconstructStatement(requestExercise.descriptionEn?:"", variables))
            responseExercise.descriptionRu = (QuestionService.reconstructStatement(requestExercise.descriptionRu?:"", variables))
            responseExercise.answerVariants = (QuestionService.getAnswerVariants(variables, customVariants))

            status.status = 1
            status.message = "Generated successful!"
            status.value = responseExercise

            return status
        }catch (ex: Exception){
            status.status = 0
            status.message = ex.message
        }

        status.status = 1
        status.message = "Comming soon"
        return status
    }
}
