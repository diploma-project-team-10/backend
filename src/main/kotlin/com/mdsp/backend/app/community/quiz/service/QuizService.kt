package com.mdsp.backend.app.community.quiz.service

import com.mdsp.backend.app.community.analytics.service.AnalyticsService
import com.mdsp.backend.app.community.program.model.Program
import com.mdsp.backend.app.community.program.repository.IProgramRepository
import com.mdsp.backend.app.community.program.service.ProgramService
import com.mdsp.backend.app.community.question.payload.GeneratedQuestion
import com.mdsp.backend.app.community.question.repository.IQuestionsRepository
import com.mdsp.backend.app.community.quiz.model.CommunityQuiz
import com.mdsp.backend.app.community.quiz.model.UserRating
import com.mdsp.backend.app.community.quiz.payload.JsonQuiz
import com.mdsp.backend.app.community.quiz.repository.ICommunityQuizRepository
import com.mdsp.backend.app.community.quiz.repository.IRatingRepository
import com.mdsp.backend.app.community.topic.model.Topic
import com.mdsp.backend.app.community.topic.repository.ITopicRepository
import com.mdsp.backend.app.system.model.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class QuizService {
    @Autowired
    lateinit var programService: ProgramService

    lateinit var topicRepository: ITopicRepository

    @Autowired
    lateinit var analyticsService: AnalyticsService

    @Autowired
    lateinit var programRepository: IProgramRepository

    @Autowired
    lateinit var quizRepository: ICommunityQuizRepository

    @Autowired
    lateinit var questionsRepository: IQuestionsRepository

    @Autowired
    lateinit var ratingService: RatingService

    fun getNextQuestion(userId: UUID, quizPayload: JsonQuiz): Status {
        val status = Status()
        val quizSession = quizRepository.findByIdAndDeletedAtIsNull(quizPayload.id).get()

        // Retrieve the user's rating from the UserRating table
        var userRating = ratingService.getUserRatingByProgramId(userId, quizSession.programId!!).rating!!

        if (quizPayload.questions != null) {
            val isCorrect = checkAnswer(quizPayload)
            analyticsService.updateUserAnalyticResult(userId, quizPayload.questions!!.id!!, isCorrect)

            // Calculate the user's updated rating for the current question
            userRating = ratingService.calculateUserRating(quizPayload.questions!!.id!!, isCorrect, userRating)
        }


        val questionTopic: Topic = Topic() //rootTree.getTopicAtIndex(mid, topicRepository)
        questionTopic.id = UUID.randomUUID()

        // Select the first question from the sorted list as the next question
        val nextQuestion = getRandomQuestionByTopic(questionTopic.id!!)

        saveNewQuestion(nextQuestion, quizSession)

        nextQuestion.answerVariants.forEach { v -> v.isAnswer = (false) }

        quizPayload.questions = nextQuestion
        status.status = 1
        status.value = quizPayload
        return status
    }

    fun getRandomQuestionByTopic(topicId: UUID): GeneratedQuestion {
        val questions = questionsRepository.findQuestionByTopic(topicId)
        if (questions.size == 0) return GeneratedQuestion()
        val randomInteger = (0 until (questions.size - 1)).random()
        val resultQuestions = questions[randomInteger].clone()

        /*questionService.generateQuestion(questions[randomInteger].clone())*/
        return GeneratedQuestion(
            resultQuestions.id,
            resultQuestions.description,
            resultQuestions.descriptionEn,
            resultQuestions.descriptionRu,
            resultQuestions.type,
            resultQuestions.variants,
        )
    }

    fun saveNewQuestion(nextQuestion: GeneratedQuestion, quizSession: CommunityQuiz) {
        quizSession.addQuestion(nextQuestion)
        quizRepository.save(quizSession)
    }

    fun checkAnswer(body: JsonQuiz): Boolean {
        var result = false
        quizRepository.findByIdAndDeletedAtIsNull(body.id!!).ifPresent { quizSession ->
            val question = quizSession.getQuestionById(body.questions!!.id!!)
            val originalAnswer = question!!.answerVariants
            question.studentAnswer = body.questions!!.studentAnswer
            val studentAnswerId = question.studentAnswer.mapNotNull { it.id }

            val stuCorrectVariants = originalAnswer.count { studentAnswerId.contains(it.id) && it.isAnswer == true }
            val correctVariants = originalAnswer.count { it.isAnswer == true }

            result = correctVariants == stuCorrectVariants
            question.isCorrect = result
            quizRepository.save(quizSession)
            return@ifPresent
        }
        return result
    }

}

/*    fun getQuestion(quizPayload: JsonQuiz): Status {
        val status = Status()
        status.status = 0
        status.message = ""
        val quizSession = quizRepository.findByIdAndDeletedAtIsNull(quizPayload.id)!!.get()
        val program: Optional<Program> = programRepository.findByIdAndDeletedAtIsNull(quizSession.programId!!)
        if(program.isPresent) {
            val rootTree = programService.getProgramTopicTree(program.get())

            if(rootTree.childrenTopics.size == 0){
                status.status = 0
                status.message = "Program does not have enough topic"
                return status
            }

            var left = quizPayload.leftPosition
            var right = quizPayload.rightPosition
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
                val question = questionsRepository.findByIdAndDeletedAtIsNull(quizPayload.questions!!.id!!).get()
                val topicId: UUID = question.topicId!!
                val analyticOptional: Optional<Analytics> = analyticsRepository.findByUserIdAndDeletedAtIsNull(userId)

                val analytic = if(analyticOptional.isPresent){
                    analyticOptional.get()
                }else{
                    Analytics(userId, null)
                }

                val isCorrect = checkAnswer(quizPayload)
                mid = (left + right) / 2
                if(isCorrect){
                    left = mid + 1
                    status.message = "Correct"
                    analytic.addCorrectAnswer(topicId)
                } else{
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
            val resultQuestions: Questions? = getRandomQuestionByTopic(questionTopic.id!!)

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



            quizPayload.questions = nextQuestion
            for(v in nextQuestion.answerVariants!!){
                v.isAnswer = (false)
            }
            quizPayload.leftPosition = left
            quizPayload.rightPosition = right
            status.status = 1
            status.value = quizPayload
            return status
        }
        status.status = 0
        status.message = "Program does not exist"
        status.value = null
        return status
    }
*/
