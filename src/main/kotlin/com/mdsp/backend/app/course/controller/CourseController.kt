package com.mdsp.backend.app.course.controller

import com.mdsp.backend.app.system.model.Status
import com.mdsp.backend.app.course.model.Course
import com.mdsp.backend.app.course.model.ModuleCourse
import com.mdsp.backend.app.course.model.StatusCourse
import com.mdsp.backend.app.course.model.payload.CourseResponse
import com.mdsp.backend.app.course.model.payload.CourseShortResponse
import com.mdsp.backend.app.course.repository.*
import com.mdsp.backend.app.profile.repository.IProfileRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/project/course")
class CourseController {
    @Autowired
    lateinit var courseRepository: ICourseRepository

    @Autowired
    lateinit var moduleRepository: IModulesRepository

    @Autowired
    lateinit var lessonRepository: ILessonRepository

    @Autowired
    lateinit var joiningRepository: IJoiningRepository

    @Autowired
    lateinit var lessonProgressRepository: ILessonProgressRepository

    @Autowired
    lateinit var profileRepository: IProfileRepository

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    fun getCourses() = courseRepository.findAllByDeletedAtIsNull()

    @GetMapping("/list-page")
    @PreAuthorize("isAuthenticated()")
    fun getCoursesPage(
        @RequestParam(value = "page") page: Int = 1,
        @RequestParam(value = "size") size: Int = 20,
    ): Page<CourseResponse>? {
        val pageImp: PageRequest = PageRequest.of(page - 1, size)
        return courseRepository.findAllByDeletedAtIsNullOrderByOrderNum(pageImp)
    }

    @GetMapping("/list/client")
    @PreAuthorize("isAuthenticated()")
    fun getCourseClient(
        @RequestParam(value = "page") page: Int = 1,
        @RequestParam(value = "size") size: Int = 20,
        @RequestParam(value = "s") searchValue: String? = "",
        authentication: Authentication,
    ): Page<CourseShortResponse> {
        println("Course")
        val pageImp: PageRequest = PageRequest.of(page - 1, size, Sort.by("order_num"))
        val search = if (!searchValue.isNullOrEmpty()) { "%${searchValue}%" } else { "%%" }
        val pageData = courseRepository.getCurrentCoursesClient(search, pageImp)
        val profile = profileRepository.findByUsernameAndDeletedAtIsNull(authentication.name)
        val profileId = profile.get().getId()
        if (profileId != null) {
            for (item in pageData.content) {
                val progression = joiningRepository.findAllByCourseIdAndProfileIdAndDeletedAtIsNull(item.getId()!!, profileId )
                if (progression.isNotEmpty()) {
                    item.setProgressPercent(progression[0].getProgressPercent())
                }
            }
        }
        return pageData.map(this::convertToCourseShort)
    }

    @GetMapping("/list/{packageId}")
    @PreAuthorize("isAuthenticated()")
    fun getCourseClientByPackage(
        @PathVariable(value = "packageId") packageId: UUID,
        @RequestParam(value = "page") page: Int = 1,
        @RequestParam(value = "size") size: Int = 20,
        authentication: Authentication,
    ): Page<CourseShortResponse> {
        val pageImp: PageRequest = PageRequest.of(page - 1, size, Sort.by("order_num"))
        val pageData = courseRepository.getCoursesByPackage(packageId.toString(), pageImp)
        val profileId = profileRepository.findByUsernameAndDeletedAtIsNull(authentication.name).get().getId()
        if (profileId != null) {
            for (item in pageData.content) {
                val progression = joiningRepository.findAllByCourseIdAndProfileIdAndDeletedAtIsNull(item.getId()!!, profileId)
                if (progression.isNotEmpty()) {
                    item.setProgressPercent(progression[0].getProgressPercent())
                }
            }
        }
        return pageData.map(this::convertToCourseShort)
    }

    @GetMapping("/my/list")
    @PreAuthorize("isAuthenticated()")
    fun getMyCourseClient(
        @RequestParam(value = "page") page: Int = 1,
        @RequestParam(value = "size") size: Int = 20,
        authentication: Authentication,
    ): Page<CourseShortResponse> {
        val profileId = profileRepository.findByUsernameAndDeletedAtIsNull(authentication.name).get().getId()
        val pageImp: PageRequest = PageRequest.of(page - 1, size, Sort.by("order_num"))
        if (profileId != null) {
            var ids: Array<UUID> = arrayOf()
            val joiningCourse = joiningRepository.findAllByProfileIdAndDeletedAtIsNull(profileId)
            for (idP in joiningCourse) {
                ids = ids.plus(idP.getCourseId()!!)
            }
            if (ids.isEmpty()) {
                return Page.empty()
            }
            val pageData = courseRepository.getCurrentMyCoursesClient(ids, pageImp)
            for (item in pageData.content) {
                val progression = joiningRepository.findAllByCourseIdAndProfileIdAndDeletedAtIsNull(item.getId()!!, profileId)
                if (progression.isNotEmpty()) {
                    item.setProgressPercent(progression[0].getProgressPercent())
                }
            }
            return pageData.map(this::convertToCourseShort)
        }
        return Page.empty()
    }

    @GetMapping("/my/course/count")
    @PreAuthorize("isAuthenticated()")
    fun getMyCourseCount(
        authentication: Authentication,
    ): Long {
        val profileId = profileRepository.findByUsernameAndDeletedAtIsNull(authentication.name).get().getId()
        if (profileId != null) {
            var ids: Array<UUID> = arrayOf()
            val joiningCourse = joiningRepository.findAllByProfileIdAndDeletedAtIsNull(profileId)
            return joiningCourse.size.toLong()
        }
        return 0
    }


    private fun convertToCourseShort(course: Course): CourseShortResponse {
        return CourseShortResponse(course)
    }

    @GetMapping("/get/{id}")
    @PreAuthorize("isAuthenticated()")
    fun getCourse(
        @PathVariable(value = "id") id: UUID,
        authentication: Authentication,
    ): Optional<Course> {
        val profileId = profileRepository.findByUsernameAndDeletedAtIsNull(authentication.name).get().getId()
        val result = courseRepository.findByIdAndDeletedAtIsNull(id)
        if (profileId != null && result.isPresent) {
            val progression = joiningRepository.findAllByCourseIdAndProfileIdAndDeletedAtIsNull(result.get().getId()!!, profileId)
            result.get().setAccess(arrayListOf())
            if (progression.isNotEmpty()) {
                result.get().setProgressPercent(progression[0].getProgressPercent())

                for (module in result.get().getModules()) {
                    module.setDescription(null)
                    for (lesson in module.getLessons()) {
                        lesson.setMarked(
                            lessonProgressRepository.findAllByObjectIdAndProfileIdAndTypeAndDeletedAtIsNull(
                                lesson.getId()!!,
                                profileId,
                                "lesson"
                            ).isNotEmpty()
                        )
                        lesson.setDescription(null)
                    }
                }
            }
        }
        return result
    }

    @PostMapping("/new")
    //@PreAuthorize("hasRole('ADMIN')")
    fun createCourse(
        @Valid @RequestBody newCourse: Course,
    ): ResponseEntity<*> {
        val status = Status()
        courseRepository.save(newCourse)

        status.status = 1
        status.message = "New Company created!"
        status.value = newCourse.getId()
        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/edit/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    fun editCourse(
        @PathVariable(value = "id") id: UUID,
        @Valid @RequestBody newCourse: Course,
    ): ResponseEntity<*> {
        val status = Status()
        status.message = "Company doesn't save!"
        if (newCourse.getId() == id && courseRepository.findByIdAndDeletedAtIsNull(id).isPresent) {
            courseRepository.save(newCourse)

            status.status = 1
            status.message = "New Company created!"
            status.value = newCourse.getId()
        }

        return ResponseEntity(status, HttpStatus.OK)
    }

    @GetMapping("/modules/builders/{id}")
    fun getModulesByCourse(@PathVariable(value = "id") id: UUID): ArrayList<ModuleCourse> {
        return moduleRepository.findAllByCourseIdAndDeletedAtIsNullOrderByOrderNum(id)
    }

    @PostMapping("/order/{type}")
    //@PreAuthorize("hasRole('ADMIN')")
    fun setOrder(
        @PathVariable(value = "type") type: String,
        @Valid @RequestBody data: MutableMap<String, Any?>,
    ): ResponseEntity<*> {
        val status = Status()
        status.message = ""
        try {
            if (type == "lesson") {
                val firstLesson = lessonRepository.findByIdAndDeletedAtIsNull(UUID.fromString(data["firstId"].toString()))
                val secondLesson = lessonRepository.findByIdAndDeletedAtIsNull(UUID.fromString(data["secondId"].toString()))
                if (firstLesson.isPresent && secondLesson.isPresent) {
                    firstLesson.get().setOrderNum(data["firstNum"].toString().toIntOrNull())
                    secondLesson.get().setOrderNum(data["secondNum"].toString().toIntOrNull())

                    lessonRepository.save(firstLesson.get())
                    lessonRepository.save(secondLesson.get())
                    status.status = 1
                }
            } else if (type == "module") {
                val firstModule = moduleRepository.findByIdAndDeletedAtIsNull(UUID.fromString(data["firstId"].toString()))
                val secondModule = moduleRepository.findByIdAndDeletedAtIsNull(UUID.fromString(data["secondId"].toString()))
                if (firstModule.isPresent && secondModule.isPresent) {
                    firstModule.get().setOrderNum(data["firstNum"].toString().toIntOrNull())
                    secondModule.get().setOrderNum(data["secondNum"].toString().toIntOrNull())

                    moduleRepository.save(firstModule.get())
                    moduleRepository.save(secondModule.get())
                    status.status = 1
                }
            }
        } catch (err: Error) { }


        return ResponseEntity(status, HttpStatus.OK)
    }
}
