package com.mdsp.backend.app.mail.controller

import com.mdsp.backend.app.files.repository.IFilesRepository
import com.mdsp.backend.app.mail.service.GMailService
import com.mdsp.backend.app.mail.service.MailService
import com.mdsp.backend.app.reference.repository.IReferenceRepository
import com.mdsp.backend.app.reference.service.RecordNoteService
import com.mdsp.backend.app.system.config.DataSourceConfiguration
import com.mdsp.backend.app.system.model.Status
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/mail")
class MailController {
    @Autowired
    lateinit var mailService: MailService

    @Autowired
    lateinit var gMailService: GMailService

    @Autowired
    lateinit var referenceRepository: IReferenceRepository

    @Autowired
    lateinit var dataSourceConfig: DataSourceConfiguration

    @Autowired
    lateinit var recordNoteService: RecordNoteService

    @Autowired
    lateinit var filesRepository: IFilesRepository

    @Value("\${file.tmp.upload-dir}")
    private val pathFiles: String = ""

    private var logger = LogManager.getLogger(MailController::class.java)


    @PostMapping("/send/kbf-message")
    @PreAuthorize("isAuthenticated()")
    fun sendMessage(@Valid @RequestBody data: MutableMap<String, String>): ResponseEntity<*>{
        val username = data["username"]!!
        val id = data["id"]!!
        val status = Status()
        status.status = 1
        gMailService.sendMessage(username, "GOOD JOB")
        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/sendMessage")
    @PreAuthorize("isAuthenticated")
    fun sendRefuseMessage(@RequestBody data: MutableMap<String, Any>): ResponseEntity<*>{
        val emailMessage = data["message"] as String
        val studentsData = data["studentsData"] as ArrayList<MutableMap<String, Any>>
        for(studentData in studentsData){
            val studentEmail = studentData["email"] as String
            gMailService.sendMessage(studentEmail, emailMessage)
        }
        val status = Status()
        status.status = 1
        status.message = "sended"
        return ResponseEntity(status, HttpStatus.OK)
    }
}
