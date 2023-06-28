package com.mdsp.backend.app.community.program.controller

import com.mdsp.backend.app.community.program.model.Program
import com.mdsp.backend.app.community.program.repository.IProgramRepository
import com.mdsp.backend.app.system.model.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.sql.Timestamp
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/project/community/program")
class ProgramController {

    @Autowired
    lateinit var programRepository: IProgramRepository

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    fun getPrograms() = programRepository.findAllByDeletedAtIsNull()

    @GetMapping("/list1")
    @PreAuthorize("isAuthenticated()")
    fun getPrograms(
        @RequestParam(value = "page") page: Int,
        @RequestParam(value = "size") size: Int
    ): Any? {
        val pagePR: PageRequest = PageRequest.of(page - 1, size)
        return programRepository.findAllByDeletedAtIsNull(pagePR)
    }

    @GetMapping("/{programId}")
    @PreAuthorize("isAuthenticated()")
    fun getProgram(@PathVariable(value = "programId") programId: UUID) =
        programRepository.findByIdAndDeletedAtIsNull(programId)

    @PostMapping("/save")
    @PreAuthorize("isAuthenticated()")
    fun saveProgram(@Valid @RequestBody newProgram: Program): Status {
        val status = Status()
        status.status = 0
        status.message = ""

        if (newProgram.id == null) {
            programRepository.save(newProgram)
            status.status = 1
            status.message = "Program saved!"
            status.value = newProgram.id
            return status
        } else {
            val program = programRepository.findByIdAndDeletedAtIsNull(newProgram.id!!).get()
            program.title = (newProgram.title)
            programRepository.save(program)
            status.status = 1
            status.message = "Program updated!"
            status.value = program.id
            return status
        }
    }

    @DeleteMapping("/{programId}")
    @PreAuthorize("isAuthenticated()")
    fun deleteProgram(@PathVariable(value = "programId") programId: UUID): Status {
        val status = Status()
        status.status = 0
        status.message = "Program does not exist!"

        programRepository.findByIdAndDeletedAtIsNull(programId).ifPresent {
            it.deletedAt = (Timestamp(System.currentTimeMillis()))
            programRepository.save(it)
            status.status = 1
            status.message = "Program deleted!"
        }
        return status
    }
}
