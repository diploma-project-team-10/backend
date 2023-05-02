package com.mdsp.backend.app.community.quiz.controller

import com.mdsp.backend.app.community.program.model.Program
import com.mdsp.backend.app.community.question.model.Questions
import com.mdsp.backend.app.community.topic.model.Topic
import com.mdsp.backend.app.community.program.repository.IProgramRepository
import com.mdsp.backend.app.community.program.service.ProgramService
import com.mdsp.backend.app.community.question.payload.GeneratedQuestion
import com.mdsp.backend.app.community.question.repository.IQuestionsRepository
import com.mdsp.backend.app.community.quiz.repository.IQuizRepository
import com.mdsp.backend.app.community.quiz.model.Quiz
import com.mdsp.backend.app.community.quiz.service.QuizService
import com.fasterxml.jackson.annotation.JsonProperty
import com.mdsp.backend.app.community.analytics.model.Analytics
import com.mdsp.backend.app.community.analytics.repository.IAnalyticsRepository
import com.mdsp.backend.app.community.topic.repository.ITopicRepository
import com.mdsp.backend.app.system.model.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/project/community/quiz")
class QuizController {

    @Autowired
    lateinit var quizRepository: IQuizRepository

    @Autowired
    lateinit var programRepository: IProgramRepository

    @Autowired
    lateinit var questionsRepository: IQuestionsRepository

    @Autowired
    lateinit var topicRepository: ITopicRepository

    @Autowired
    lateinit var analyticsRepository: IAnalyticsRepository

    @GetMapping("/get/{quizId}")
    fun getQuiz(@PathVariable("quizId") quizId: UUID?) =
            quizRepository.findByIdAndDeletedAtIsNull(quizId)

    @GetMapping("/start/{id}")
    fun startQuiz(@PathVariable("id") programId: UUID?): Status {
        val status = Status()
        status.status = 0
        status.message = "Something went wrong!(Sorry)"
        if (programId != null){
            val quiz = Quiz(null, programId)
            quizRepository.save(quiz)
            status.status = 1
            status.value = quiz.id
            status.message = "Quiz Started"
            return status
        }
        return status
    }


    @PostMapping("/getQuestion")
    fun getQuestion(@Valid @RequestBody body: JsonQuiz): Status {
        val status = Status()
        status.status = 0
        status.message = ""
        val quizSession = quizRepository.findByIdAndDeletedAtIsNull(body.id)!!.get()
        val program: Optional<Program> = programRepository.findByIdAndDeletedAtIsNull(quizSession.programId!!)
        if(program.isPresent){
            val rootTree = ProgramService.getProgramTopicTree(program.get(), topicRepository)

            if(rootTree.childrenTopics.size == 0){
                status.status = 0
                status.message = "Program does not have enough topic"
                return status
            }

            var left = body.leftPosition
            var right = body.rightPosition
            var mid: Int

            if(left == null && right == null){
                val length = rootTree.childrenTopics.size
                left = 0
                right = length-1
                mid = (left + right) / 2
            }
            else{
                if(left!! > right!!){
                    status.message = "QUIZ END"
                    status.status = 1
                    status.value = null
                    return status
                }

                val userId: UUID = UUID.fromString("d35080e7-c829-4fc9-9308-82ac8ef83e92")
                val question = questionsRepository.findByIdAndDeletedAtIsNull(body.questions!!.id!!).get()
                val topicId: UUID = question.topicId!!
                val analyticOptional: Optional<com.mdsp.backend.app.community.analytics.model.Analytics> = analyticsRepository.findByUserIdAndDeletedAtIsNull(userId)
                var analytic: com.mdsp.backend.app.community.analytics.model.Analytics? = null

                if(analyticOptional.isPresent){
                    analytic = analyticOptional.get()
                }else{
                    analytic = com.mdsp.backend.app.community.analytics.model.Analytics(userId, null)
                }

                val isCorrect = QuizService.checkAnswer(body, quizRepository)
                mid = (left + right) / 2
                if(isCorrect){
                    left = mid + 1
                    status.message = "Correct"
                    analytic.addCorrectAnswer(topicId)
                }
                else{
                    right = mid - 1
                    status.message = "Incorrect"
                    analytic.addWrongAnswer(topicId)
                }
                analyticsRepository.save(analytic)
                if(left > right){
                    status.message = "QUIZ END"
                    status.status = 1
                    status.value = null
                    return status
                }
                mid = (left + right) / 2
            }

            val questionTopic: Topic = rootTree.getTopicAtIndex(mid, topicRepository)
            val resultQuestions: Questions? = QuizService.getRandomQuestionByTopic(questionTopic.id!!, questionsRepository)

            val nextQuestion = GeneratedQuestion()
            nextQuestion.id = (resultQuestions!!.id)
            nextQuestion.description = (resultQuestions.description)
            nextQuestion.descriptionEn = (resultQuestions.descriptionEn)
            nextQuestion.descriptionRu = (resultQuestions.descriptionRu)
            nextQuestion.answerType = (resultQuestions.type)
            nextQuestion.answerVariants = (resultQuestions.variants)
            nextQuestion.answerRelVariants = (null)
            quizSession.addQuestion(nextQuestion)
            quizRepository.save(quizSession)



            body.questions = nextQuestion
            for(v in nextQuestion.answerVariants!!){
                v.isAnswer = (false)
            }
            body.leftPosition = left
            body.rightPosition = right
            status.status = 1
            status.value = body
            return status
        }
        status.status = 0
        status.message = "Program does not exist"
        status.value = null
        return status
    }

    class JsonQuiz{
        //Quiz session id
        @JsonProperty("id")
        var id: UUID? = null

        @JsonProperty("leftPosition")
        var leftPosition: Int? = null

        @JsonProperty("rightPosition")
        var rightPosition: Int? = null

        @JsonProperty("question")
        var questions: GeneratedQuestion? = null
    }
}
