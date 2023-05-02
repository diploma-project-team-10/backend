package com.mdsp.backend.app.course.controller

import com.mdsp.backend.app.system.model.Status
import com.mdsp.backend.app.system.model.Util
import com.mdsp.backend.app.system.service.Acl.Acl
import com.mdsp.backend.app.course.model.Joining
import com.mdsp.backend.app.course.repository.ICourseRepository
import com.mdsp.backend.app.course.repository.IJoiningRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid


@RestController
@RequestMapping("/api/project/course/joining")

class JoiningController {
    @Autowired
    lateinit var joiningRepository: IJoiningRepository

    @Autowired
    lateinit var courseRepository: ICourseRepository


    @PostMapping("/new")
    @PreAuthorize("isAuthenticated()")
    fun createJoining(@Valid @RequestBody newJoining: Joining): ResponseEntity<*> {
        val status = Status()
        // TODO Может сделать access check function
        status.message = "You don't have access"
        if (newJoining.getCourseId() != null
            && courseRepository.findByIdAndDeletedAtIsNull(newJoining.getCourseId()!!).isPresent
            && newJoining.getProfileId() != null
            && joiningRepository.findAllByCourseIdAndProfileIdAndDeletedAtIsNull(newJoining.getCourseId()!!, newJoining.getProfileId()!!).isEmpty()
        ) {
            val joiningCourse = courseRepository.findByIdAndDeletedAtIsNull(newJoining.getCourseId()!!)

            if (joiningCourse.isPresent && (
                        Acl.hasAccess(Util.mapToArray(joiningCourse.get().getAccess()), newJoining.getAccessAcl()).isNotEmpty()
                        || joiningCourse.get().getAccessCourse() == newJoining.getAccessCourse()
                        || joiningCourse.get().getAccessCourse() == null
                        )) {
                joiningRepository.save(newJoining)
                status.status = 1
                status.message = "New Joining created!"
                status.value = 2
            }
        }

        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun editCompany(
        @PathVariable(value = "id") id: UUID,
        @Valid @RequestBody newJoining: Joining
    ): ResponseEntity<*> {
        val status = Status()
        status.message = "Joining doesn't save!"
        if (
            newJoining.getId() == id
            && joiningRepository.findByIdAndDeletedAtIsNull(id).isPresent
            && courseRepository.findByIdAndDeletedAtIsNull(newJoining.getCourseId()!!).isPresent
        ) {
            joiningRepository.save(newJoining)

            status.status = 1
            status.message = "New Joining created!"
            status.value = 2
        }

        return ResponseEntity(status, HttpStatus.OK)
    }
}
