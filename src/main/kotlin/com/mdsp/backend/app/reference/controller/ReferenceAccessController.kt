package com.mdsp.backend.app.reference.controller

import com.mdsp.backend.app.profile.service.ProfileService
import com.mdsp.backend.app.reference.model.Access
import com.mdsp.backend.app.reference.repository.IReferenceAccessRepository
import com.mdsp.backend.app.reference.repository.IReferenceRepository
import com.mdsp.backend.app.reference.service.AccessService
import com.mdsp.backend.app.reference.service.RecordNoteService
import com.mdsp.backend.app.reference.service.ReferenceNoteService
import com.mdsp.backend.app.system.model.Status
import com.mdsp.backend.app.system.service.Acl.Acl
import com.mdsp.backend.app.user.model.UserPrincipal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid
import kotlin.collections.ArrayList


@RestController
@RequestMapping("/api/reference/access")
class ReferenceAccessController() {

    @Autowired
    lateinit var referenceRepository: IReferenceRepository

    @Autowired
    lateinit var recordNoteService: RecordNoteService

    @Autowired
    lateinit var accessRepository: IReferenceAccessRepository

    @Autowired
    lateinit var profileService: ProfileService

    @Autowired
    lateinit var accessService: AccessService

    @GetMapping("/get/{referenceId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getReference(@PathVariable(value = "referenceId") referenceId: UUID): ArrayList<Access> {
        val reference = referenceRepository.findByIdAndDeletedAtIsNull(referenceId)
        if (reference.isPresent) {
            return accessRepository.findAllByReferenceIdAndDeletedAtIsNull(referenceId)
        }
        return arrayListOf()
    }

    @PostMapping("/{referenceId}/create")
    @PreAuthorize("hasRole('ADMIN')")
    fun createReference(
        @PathVariable(value = "referenceId") referenceId: UUID,
        @Valid @RequestBody newAccess: ArrayList<Access>,
        authentication: Authentication
    ): ResponseEntity<*> {
        val status = Status()

        val reference = referenceRepository.findByIdAndDeletedAtIsNull(referenceId)
        if (reference.isPresent) {
            accessRepository.deleteByReferenceId(referenceId)
            for (access in newAccess) {
                access.referenceId = referenceId
                accessRepository.save(access)
                status.value = access.objects
            }
            status.status = 1
            status.message = ""
        }

        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/record/{type}/{referenceId}/{recordId}")
    @PreAuthorize("isAuthenticated()")
    fun mayEditRecord(
        @PathVariable(value = "type") type: String,
        @PathVariable(value = "referenceId") referenceId: UUID,
        @PathVariable(value = "recordId") recordId: UUID,
        authentication: Authentication
    ): Boolean
    {
        val reference = referenceRepository.findByIdAndDeletedAtIsNull(referenceId)
        if (reference.isPresent) {
            val profileId = (authentication.principal as UserPrincipal).id

            val result = recordNoteService.getRecordById(recordId, reference.get())
            // Access to view
            if (result.containsKey("owner") && result["owner"] != null) {
                val owner = UUID.fromString(result["owner"].toString())
                when (type) {
                    "view" -> {
                        return accessService.mayView(profileId, reference.get(), owner)
                    }
                    "edit" -> {
                        return accessService.mayEdit(profileId, reference.get(), owner, arrayListOf(recordId))
                    }
                    "delete" -> {
                        return accessService.mayDelete(profileId, reference.get(), owner, arrayListOf(recordId))
                    }
                }

            }
        }
        return false
    }

    @PostMapping("/record/add/{referenceId}")
    @PreAuthorize("isAuthenticated()")
    fun mayAddRecord(
        @PathVariable(value = "referenceId") referenceId: UUID,
        authentication: Authentication
    ): Boolean
    {
        val reference = referenceRepository.findByIdAndDeletedAtIsNull(referenceId)
        if (reference.isPresent) {
            val profileId = (authentication.principal as UserPrincipal).id
            return accessService.mayAdd(profileId, reference.get())
        }
        return false
    }

}
