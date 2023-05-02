package com.mdsp.backend.app.course.controller

import com.mdsp.backend.app.system.model.Status
import com.mdsp.backend.app.course.model.ModuleCourse
import com.mdsp.backend.app.course.repository.ICourseRepository
import com.mdsp.backend.app.course.repository.IJoiningRepository
import com.mdsp.backend.app.course.repository.ILessonProgressRepository
import com.mdsp.backend.app.course.repository.IModulesRepository
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
@RequestMapping("/api/project/course/module")
class ModuleController {
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
    fun getModules() = moduleRepository.findAllByDeletedAtIsNull()

    @GetMapping("/list-page")
    fun getModulesPage(
        @RequestParam(value = "page") page: Int = 1,
        @RequestParam(value = "size") size: Int = 20
    ): Page<ModuleCourse>? {
        val pageImp: PageRequest = PageRequest.of(page - 1, size)
        return moduleRepository.findAllByDeletedAtIsNull(pageImp)
    }

    @GetMapping("/get/{id}")
    @PreAuthorize("isAuthenticated()")
    fun getModule(
        @PathVariable(value = "id") id: UUID,
        authentication: Authentication,
    ): Optional<ModuleCourse> {
        val profileId = profileRepository.findByEmailAndDeletedAtIsNull(authentication.name).get().getId()
        val result = moduleRepository.findByIdAndDeletedAtIsNull(id)
        val navigate: MutableMap<String, MutableMap<String, String>> = mutableMapOf()
        if (result.isPresent && result.get().getCourseId() != null) {
            val course = courseRepository.findByIdAndDeletedAtIsNull(result.get().getCourseId()!!)
            if (course.isPresent) {
                result.get().setModuleCount(course.get().getModuleCount())

                if (result.get().getLessonCount() > 0) {
                    navigate["next"] = mutableMapOf("type" to "lesson", "id" to result.get().getLessons()[0].getId().toString())
                } else if (course.get().getModuleCount() > result.get().getOrderNum() + 1) {
                    navigate["next"] = mutableMapOf(
                        "type" to "module",
                        "id" to course.get().getModules()[result.get().getOrderNum() + 1].getId().toString()
                    )
                }
                if (result.get().getOrderNum() > 0) {
                    if (course.get().getModules()[result.get().getOrderNum() - 1].getLessonCount() > 0) {
                        navigate["prev"] = mutableMapOf(
                            "type" to "lesson",
                            "id" to course.get().getModules()[result.get().getOrderNum() - 1].getLessons().last().getId().toString()
                        )
                    } else {
                        navigate["prev"] = mutableMapOf(
                            "type" to "module",
                            "id" to course.get().getModules()[result.get().getOrderNum() - 1].getId().toString()
                        )
                    }
                }

                result.get().setNavigate(navigate)
                if (
                    profileId != null
                    && joiningRepository.findAllByCourseIdAndProfileIdAndDeletedAtIsNull(course.get().getId()!!, profileId).isNotEmpty()
                ) {
                    for (lesson in result.get().getLessons()) {
                        lesson.setMarked(
                            lessonProgressRepository.findAllByObjectIdAndProfileIdAndTypeAndDeletedAtIsNull(
                                lesson.getId()!!,
                                profileId,
                                "lesson"
                            ).isNotEmpty()
                        )
                    }
                }

            }
        }
        return result
    }

    @PostMapping("/new")
    //@PreAuthorize("hasRole('ADMIN')")
    fun createModule(@Valid @RequestBody newModule: ModuleCourse): ResponseEntity<*> {
        val status = Status()
        moduleRepository.save(newModule)

        status.status = 1
        status.message = "New Module created!"
        status.value = newModule.getId()
        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/edit/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    fun editModule(
        @PathVariable(value = "id") id: UUID,
        @Valid @RequestBody newModule: ModuleCourse
    ): ResponseEntity<*> {
        val status = Status()
        status.message = "Module doesn't save!"
        if (moduleRepository.existsByIdAndDeletedAtIsNull(id)) {
            if (newModule.getCourseId() == null) {
                val moduleC = moduleRepository.findByIdAndDeletedAtIsNull(id).get()
                newModule.setCourseId(moduleC.getCourseId())
            }
            moduleRepository.save(newModule)

            status.status = 1
            status.message = "New Module created!"
            status.value = newModule.getId()
        }

        return ResponseEntity(status, HttpStatus.OK)
    }
}
