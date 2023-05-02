package com.mdsp.backend.app.course.controller

import com.mdsp.backend.app.system.model.Status
import com.mdsp.backend.app.course.model.LessonProgress
import com.mdsp.backend.app.course.repository.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid


@RestController
@RequestMapping("/api/project/course/lesson-progress")

class LessonProgressController {
    @Autowired
    lateinit var joiningRepository: IJoiningRepository

    @Autowired
    lateinit var lessonProgressRepository: ILessonProgressRepository

    @Autowired
    lateinit var courseRepository: ICourseRepository

    @Autowired
    lateinit var lessonRepository: ILessonRepository

    @Autowired
    lateinit var moduleRepository: IModulesRepository

    @Autowired
    lateinit var quizRepository: IQuizRepository


    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    fun createLessonProgress(
        @Valid @RequestBody newLessonProgress: LessonProgress
    ): ResponseEntity<*> {
        val status = Status()
        if (
            newLessonProgress.getObjectId() != null
            && newLessonProgress.getProfileId() != null
            && lessonProgressRepository.findAllByObjectIdAndProfileIdAndTypeAndDeletedAtIsNull(newLessonProgress.getObjectId()!!, newLessonProgress.getProfileId()!!, "lesson").isEmpty()
        ) {
            newLessonProgress.setType("lesson")
            val lesson = lessonRepository.findByIdAndDeletedAtIsNull(newLessonProgress.getObjectId()!!)
            if (!lesson.isPresent || lesson.get().getModuleId() == null) {
                status.message = "Lesson not exist"
                return ResponseEntity(status, HttpStatus.OK)
            }
            val module = moduleRepository.findByIdAndDeletedAtIsNull(lesson.get().getModuleId()!!)
            if (!module.isPresent || module.get().getCourseId() == null) {
                status.message = "Module not exist"
                return ResponseEntity(status, HttpStatus.OK)
            }
            val course = courseRepository.findByIdAndDeletedAtIsNull(module.get().getCourseId()!!)
            if (!course.isPresent) {
                status.message = "Course not exist"
                return ResponseEntity(status, HttpStatus.OK)
            }

            val joining = joiningRepository.findAllByCourseIdAndProfileIdAndDeletedAtIsNull(course.get().getId()!!, newLessonProgress.getProfileId()!!)
            if (joining.isEmpty()) {
                status.message = "Joining not exist"
                return ResponseEntity(status, HttpStatus.OK)
            }

            val quizzes = quizRepository.findAllByLessonIdAndDeletedAtIsNullOrderByOrderNum(newLessonProgress.getObjectId()!!)
            for (quiz in quizzes) {
                val progress = lessonProgressRepository.findAllByObjectIdAndProfileIdAndTypeAndDeletedAtIsNull(quiz.getId()!!, newLessonProgress.getProfileId()!!, "quiz")
                if (progress.isEmpty()) {
                    status.message = "Quizzes must complete"
                    return ResponseEntity(status, HttpStatus.OK)
                }
            }

            lessonProgressRepository.save(newLessonProgress)
            joining[0].setProgressPercent(getProgressPercent(course.get().getId()!!, newLessonProgress.getProfileId()!!))
            joiningRepository.save(joining[0])

            status.status = 1
            status.message = "New Joining created!"
            status.value = 3
        }

        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun editCompany(
        @PathVariable(value = "id") id: UUID,
        @Valid @RequestBody newLessonProgress: LessonProgress
    ): ResponseEntity<*> {
        val status = Status()
        status.message = "Joining doesn't save!"
        if (
            newLessonProgress.getId() == id
            && lessonProgressRepository.findByIdAndDeletedAtIsNull(id).isPresent
            && lessonRepository.findByIdAndDeletedAtIsNull(newLessonProgress.getObjectId()!!).isPresent
        ) {
            lessonProgressRepository.save(newLessonProgress)

            status.status = 1
            status.message = "New Joining created!"
            status.value = 2
        }

        return ResponseEntity(status, HttpStatus.OK)
    }

    fun getProgressPercent(courseId: UUID, profileId: UUID): Int {
        val course = courseRepository.findByIdAndDeletedAtIsNull(courseId)
        if (course.isPresent && course.get().getLessonCount() > 0) {
            var countMarkedLessons = 0
            for (module in course.get().getModules()) {
                for (lesson in module.getLessons()) {
                    if (
                        lessonProgressRepository.findAllByObjectIdAndProfileIdAndTypeAndDeletedAtIsNull(
                            lesson.getId()!!,
                            profileId,
                            "lesson"
                        ).isNotEmpty()
                    ) {
                        countMarkedLessons++
                    }
                }
            }
            return (countMarkedLessons * 100) / course.get().getLessonCount()
        }
        return 0
    }
}
