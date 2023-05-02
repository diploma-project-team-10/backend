package com.mdsp.backend.app.course.controller

import com.mdsp.backend.app.course.model.Teacher
import com.mdsp.backend.app.course.repository.ITeacherRepository
import com.mdsp.backend.app.system.model.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid


@RestController
@RequestMapping("/api/project/course/teacher")

class TeacherController {
    @Autowired
    lateinit var teacherRepository: ITeacherRepository


    @GetMapping("/list")
    //@PreAuthorize("isAuthenticated()")
    fun getTeachers() = teacherRepository.findAllByDeletedAtIsNull()

    @GetMapping("/list-page")
    fun getTeachersPage(
        @RequestParam(value = "page") page: Int = 1,
        @RequestParam(value = "size") size: Int = 20
    ): Page<Teacher>? {
        val pageImp: PageRequest = PageRequest.of(page - 1, size)
        return teacherRepository.findAllByDeletedAtIsNull(pageImp)
    }

    @GetMapping("/get/{id}")
//    @PreAuthorize("isAuthenticated()")
    fun getTeacher(@PathVariable(value = "id") id: UUID) = teacherRepository.findByIdAndDeletedAtIsNull(id)

    @PostMapping("/new")
    //@PreAuthorize("hasRole('ADMIN')")
    fun createTeacher(@Valid @RequestBody newTeacher: Teacher): ResponseEntity<*> {
        val status = Status()
        teacherRepository.save(newTeacher)

        status.status = 1
        status.message = "New Company created!"
        status.value = newTeacher.getId()
        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/edit/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    fun editTeacher(
        @PathVariable(value = "id") id: UUID,
        @Valid @RequestBody newTeacher: Teacher
    ): ResponseEntity<*> {
        val status = Status()
        status.message = "Company doesn't save!"
        if (newTeacher.getId() == id && teacherRepository.findByIdAndDeletedAtIsNull(id).isPresent) {
            teacherRepository.save(newTeacher)

            status.status = 1
            status.message = "New Company created!"
            status.value = newTeacher.getId()
        }

        return ResponseEntity(status, HttpStatus.OK)
    }
}
