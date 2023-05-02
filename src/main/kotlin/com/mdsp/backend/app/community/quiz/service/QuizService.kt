package com.mdsp.backend.app.community.quiz.service

import com.mdsp.backend.app.community.question.model.Questions
import com.mdsp.backend.app.community.question.repository.IQuestionsRepository
import com.mdsp.backend.app.community.quiz.controller.QuizController
import com.mdsp.backend.app.community.quiz.repository.IQuizRepository
import java.util.*

class QuizService {
    companion object {

        fun getRandomQuestionByTopic(topicId: UUID, questionsRepository: IQuestionsRepository): Questions? {
            val questions = questionsRepository.findQuestionByTopic(topicId)
            if(questions.size == 0) return null
            questions.shuffle()
            val resultQuestions = questions[0].clone()
            return resultQuestions /*QuestionService.generateQuestion(resultQuestions)*/
        }

        fun checkAnswer(body: QuizController.JsonQuiz, quizRepository: IQuizRepository): Boolean {
            val quizSession = quizRepository.findByIdAndDeletedAtIsNull(body.id!!)
            var correctVariants = 0
            if(quizSession!!.isPresent){
                val question = quizSession.get().getQuestionById(body.questions!!.id!!)
                val originalAnswer = question!!.answerVariants!!
                val studentAnswer = body.questions!!.studentAnswer!!
                question.studentAnswer = (studentAnswer)
                for(v in originalAnswer){
                    if(v.isAnswer!!){
                        correctVariants++
                        var isContain = false
                        for(answer in studentAnswer){
                            if(v.equals(answer)){
                                isContain = true
                                break
                            }
                        }
                        if(!isContain){
                            question.isCorrect = (false)
                            quizRepository.save(quizSession.get())
                            return false
                        }
                    }
                }
                if(correctVariants != studentAnswer.size){
                    question.isCorrect = (false)
                    quizRepository.save(quizSession.get())
                    return false
                }
                question.isCorrect = (true)
                quizRepository.save(quizSession.get())
                return true
            }
            return false
        }

    }
}
