package com.mdsp.backend.app.client.controller

import com.mdsp.backend.app.client.model.Client
import com.mdsp.backend.app.client.repository.IClientRepository
import com.mdsp.backend.app.profile.repository.IProfileRepository
import com.mdsp.backend.app.system.model.Status
import com.mdsp.backend.app.user.model.payload.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import javax.validation.Valid


@RestController
@RequestMapping("/api/clients")
class ClientController() {

    @Autowired
    lateinit var clientRepository: IClientRepository

    @Autowired
    lateinit var profileRepository: IProfileRepository

    @GetMapping("/list")
    fun getClients() = clientRepository.findAllByDeletedAtIsNull()

    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    fun createClient(@Valid @RequestBody newGroup: Client): ResponseEntity<*> {
        val status = Status()
        status.status = 0
        status.message = ""
        clientRepository.save(newGroup)

        status.status = 1
        status.message = "Created!"
        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/auth/global")
    fun authGlobal(@Valid @RequestBody loginRequest: User): ResponseEntity<*> {
        val status = Status()

        val clients = clientRepository.findAllByDeletedAtIsNull()
        val restTemplate = RestTemplate()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val request = HttpEntity(loginRequest, headers)

        val result: ArrayList<MutableMap<String, Any?>> = arrayListOf()
        for (client in clients) {
            try {
                val response: ResponseEntity<Status> = restTemplate.postForEntity("${client.getUrl()}/api/auth/signin", request, Status::class.java)
                if (response.body != null && response.body!!.status == 1) {
                    result.add(mutableMapOf(
                        "client" to client.getTitle(),
                        "url" to client.getUrl(),
                        "body" to response.body!!.value)
                    )
                }
            } catch(e: Exception) {}

        }

        status.status = 1
        status.message = "Created!"
        status.value = result
        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/auth/{typeSign}/{providerId}/oauth2")
    fun authGlobalGoogle(
        @Valid @RequestBody loginRequest: User,
        @PathVariable(value = "typeSign") typeSign: String,
        @PathVariable(value = "providerId") providerId: String
    ): ResponseEntity<*> {
        val status = Status()

        val clients = clientRepository.findAllByDeletedAtIsNull()
        val restTemplate = RestTemplate()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val request = HttpEntity(loginRequest, headers)

        val result: ArrayList<MutableMap<String, Any?>> = arrayListOf()
        for (client in clients) {
            try {
                val response: ResponseEntity<Status> =
                    restTemplate.postForEntity("${client.getUrl()}/api/auth/$typeSign/$providerId/oauth2",
                        request,
                        Status::class.java)
                if (response.statusCodeValue == 200 && response.body != null && response.body!!.status == 1) {
                    result.add(mutableMapOf(
                        "client" to client.getTitle(),
                        "url" to client.getUrl(),
                        "body" to response.body!!.value
                    ))
                }
            } catch(e: Exception) {}
        }

        status.status = 1
        status.message = "Created!"
        status.value = result
        return ResponseEntity(status, HttpStatus.OK)
    }
}
