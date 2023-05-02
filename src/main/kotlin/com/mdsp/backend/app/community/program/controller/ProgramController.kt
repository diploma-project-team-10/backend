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
    fun getPrograms() = programRepository.findAllByDeletedAtIsNull()

    @GetMapping("/list1")
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

    @PostMapping("generate")
    @PreAuthorize("isAuthenticated()")
    fun generateQuiz(@Valid @RequestBody program: Program): Status {
        val status = Status()
        status.status = 0
        status.message = ""
////        println("Generate Quiz")
//        var programCandidate: Optional<Program> = programRepository.findByIdAndDeletedAtIsNull(program.getId()!!)
//        if(programCandidate.isPresent){
////            println("Program is Present")
//            var relativeTopics: Array<String> = programCandidate.get().getRelativeTopics()!!
//            var questions: ArrayList<Questions> = arrayListOf()
//            for(topic in relativeTopics) {
//                var topic_id: UUID = UUID.fromString(topic)
//                var list:ArrayList<Questions> = questionsRepository.findQuestionTopicByIdAndType(topic_id, 1)
//                if(list !== null) questions.addAll(list)
//            }
//            var result = ProgramService.getGeneratedTest(questions)
//            status.value = result
//            for(question in result){
//                println(question.getVariants())
//            }
//        }else{
//            status.message = "Program does not exist!"
//            return status
//        }

//        status.message = "Succesfull"
        status.message = "Comming Soon"
        status.status = 1

        return status
    }
}
