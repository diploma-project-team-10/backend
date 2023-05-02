package com.mdsp.backend.app.course.controller

import com.mdsp.backend.app.system.model.Status
import com.mdsp.backend.app.course.model.Lesson
import com.mdsp.backend.app.course.repository.*
import com.mdsp.backend.app.profile.repository.IProfileRepository
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


@RestController
@RequestMapping("/api/project/course/lesson")
class LessonController {
    @Autowired
    lateinit var lessonRepository: ILessonRepository

    @Autowired
    lateinit var moduleRepository: IModulesRepository

    @Autowired
    lateinit var courseRepository: ICourseRepository

    @Autowired
    lateinit var joiningRepository: IJoiningRepository

    @Autowired
    lateinit var lessonProgressRepository: ILessonProgressRepository

    @Autowired
    lateinit var profileRepository: IProfileRepository

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    fun getLessons() = lessonRepository.findAllByDeletedAtIsNull()

    @GetMapping("/list-page")
    fun getLessonsPage(
        @RequestParam(value = "page") page: Int = 1,
        @RequestParam(value = "size") size: Int = 20
    ): Page<Lesson>? {
        val pageImp: PageRequest = PageRequest.of(page - 1, size)
        return lessonRepository.findAllByDeletedAtIsNull(pageImp)
    }

    @GetMapping("/get/{id}")
    @PreAuthorize("isAuthenticated()")
    fun getLesson(
        @PathVariable(value = "id") id: UUID,
        authentication: Authentication,
    ): Optional<Lesson> {
        val profileId = profileRepository.findByUsernameAndDeletedAtIsNull(authentication.name).get().getId()
        val result = lessonRepository.findByIdAndDeletedAtIsNull(id)
        if (result.isPresent) {
            val module = moduleRepository.findByIdAndDeletedAtIsNull(result.get().getModuleId()!!)
            if (module.isPresent) {
                result.get().setModuleOrderNum(module.get().getOrderNum())

                val navigate: MutableMap<String, MutableMap<String, String>> = mutableMapOf()
                val course = courseRepository.findByIdAndDeletedAtIsNull(module.get().getCourseId()!!)
                if (course.isPresent) {

                    // Navigator PREV & NEXT
                    if (module.get().getLessonCount() > result.get().getOrderNum() + 1) {
                        navigate["next"] = mutableMapOf(
                            "type" to "lesson",
                            "id" to module.get().getLessons()[result.get().getOrderNum() + 1].getId().toString()
                        )
                    } else if (course.get().getModuleCount() > module.get().getOrderNum() + 1) {
                        navigate["next"] = mutableMapOf(
                            "type" to "module",
                            "id" to course.get().getModules()[module.get().getOrderNum() + 1].getId().toString()
                        )
                    }
                    if (result.get().getOrderNum() > 0) {
                        navigate["prev"] = mutableMapOf(
                            "type" to "lesson",
                            "id" to module.get().getLessons()[result.get().getOrderNum() - 1].getId().toString()
                        )
                    } else {
                        navigate["prev"] = mutableMapOf(
                            "type" to "module",
                            "id" to module.get().getId().toString()
                        )
                    }

                    result.get().setNavigate(navigate)
                    // Navigator PREV & NEXT END

                    // Progress Percentage and Count Marked Lessons
                    if (
                        profileId != null
                        && joiningRepository.findAllByCourseIdAndProfileIdAndDeletedAtIsNull(course.get().getId()!!, profileId).isNotEmpty()
                    ) {
                        var countMarked = 0
                        for (lesson in module.get().getLessons()) {
                            if (
                                lessonProgressRepository.findAllByObjectIdAndProfileIdAndTypeAndDeletedAtIsNull(
                                    lesson.getId()!!,
                                    profileId,
                                    "lesson"
                                ).isNotEmpty()
                            ) {
                                countMarked++
                                if (lesson.getId() != null && lesson.getId() === result.get().getId()) {
                                    result.get().setMarked(true)
                                }
                            }
                        }
                        if (module.get().getLessonCount() > 0) {
                            result.get().setProgressPercent((countMarked * 100) / module.get().getLessonCount())
                        }
                        if (result.get().getQuizzes().isNotEmpty()) {
                            for ((key, quiz) in result.get().getQuizzes().withIndex()) {
                                if (
                                    lessonProgressRepository.findAllByObjectIdAndProfileIdAndTypeAndDeletedAtIsNull(
                                        quiz.getId()!!,
                                        profileId,
                                        "quiz"
                                    ).isNotEmpty()
                                ) {
                                    result.get().getQuizzes()[key].setMarked(true)
                                }
                            }
                        }
                    }
                    // Progress Percentage and Count Marked Lessons - END
                }
            }
        }
        return result
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    fun createLesson(@Valid @RequestBody newLesson: Lesson): ResponseEntity<*> {
        val status = Status()
        lessonRepository.save(newLesson)

        status.status = 1
        status.message = "New Lesson created!"
        status.value = newLesson.getId()
        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun editLesson(
        @PathVariable(value = "id") id: UUID,
        @Valid @RequestBody newLesson: Lesson
    ): ResponseEntity<*> {
        val status = Status()
        status.message = "Lesson doesn't save!"
        if (lessonRepository.existsByIdAndDeletedAtIsNull(id)) {
            if (newLesson.getModuleId() == null) {
                val lesson = lessonRepository.findByIdAndDeletedAtIsNull(id).get()
                newLesson.setModuleId(lesson.getModuleId())
            }
            lessonRepository.save(newLesson)

            status.status = 1
            status.message = "New Lesson created!"
            status.value = newLesson.getId()
        }

        return ResponseEntity(status, HttpStatus.OK)
    }
}
