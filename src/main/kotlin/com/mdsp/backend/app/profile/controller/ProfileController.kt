package com.mdsp.backend.app.profile.controller

import com.mdsp.backend.app.profile.model.*
import com.mdsp.backend.app.profile.repository.*
import com.mdsp.backend.app.reference.repository.IReferenceRepository
import com.mdsp.backend.app.reference.service.RecordNoteService
import com.mdsp.backend.app.system.config.DataSourceConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@RequestMapping("/api")
class ProfileController {

    @Autowired
    lateinit var profileRepository: IProfileRepository

    @Autowired
    lateinit var encoder: PasswordEncoder

    @Autowired
    lateinit var referenceRepository: IReferenceRepository

    @Autowired
    lateinit var recordNoteService: RecordNoteService

    @Autowired
    lateinit var dataSourceConfig: DataSourceConfiguration

    @GetMapping("/profiles/{id}")
    @PreAuthorize("isAuthenticated()")
    fun getPersons(@PathVariable id: UUID): Optional<Profile> {
        val profile = profileRepository.findByIdAndDeletedAtIsNull(id)
        profile.ifPresent {
            it.setPassword("")
        }
        return profile
    }

    @GetMapping("/myprofile")
    @PreAuthorize("isAuthenticated()")
    fun getMyProfile(authentication: Authentication): Optional<Profile> {
        val profile = profileRepository.findByUsernameAndDeletedAtIsNull(authentication.name)
        profile.ifPresent {
            it.setPassword("")
        }
        return profile
    }
}
