package com.mdsp.backend.app.reference.controller

import com.mdsp.backend.app.profile.service.ProfileService
import com.mdsp.backend.app.reference.model.Reference
import com.mdsp.backend.app.reference.model.Section
import com.mdsp.backend.app.reference.model.fields.FieldFactory
import com.mdsp.backend.app.reference.payload.SectionRequest
import com.mdsp.backend.app.reference.repository.IReferenceRepository
import com.mdsp.backend.app.reference.repository.ISectionRepository
import com.mdsp.backend.app.reference.service.AccessService
import com.mdsp.backend.app.reference.service.RecordNoteService
import com.mdsp.backend.app.reference.service.ReferenceNoteService
import com.mdsp.backend.app.reference.service.SectionService
import com.mdsp.backend.app.structure.repository.IRoleGroupRepository
import com.mdsp.backend.app.structure.repository.IStructureRepository
import com.mdsp.backend.app.structure.service.RolesGroupService
import com.mdsp.backend.app.system.config.DataSourceConfiguration
import com.mdsp.backend.app.system.model.GridQuery
import com.mdsp.backend.app.system.model.Status
import com.mdsp.backend.app.system.model.Util
import com.mdsp.backend.app.system.service.Acl.Acl
import com.mdsp.backend.app.user.model.UserPrincipal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.sql.Timestamp
import java.util.*
import javax.validation.Valid
import kotlin.collections.ArrayList

@RestController
@RequestMapping("/api/reference/section")
class SectionController {
    @Autowired
    lateinit var sectionRepository: ISectionRepository

    @Autowired
    lateinit var referenceRepository: IReferenceRepository

    @Autowired
    lateinit var profileService: ProfileService

    @Autowired
    lateinit var recordNoteService: RecordNoteService

    @Autowired
    lateinit var referenceNoteService: ReferenceNoteService

    @Autowired
    lateinit var dataSourceConfig: DataSourceConfiguration

    @Autowired
    lateinit var rolesGroupService: RolesGroupService

    @Autowired
    lateinit var accessService: AccessService

    @Autowired
    lateinit var sectionService: SectionService

    @GetMapping("/list/{referenceId}")
    @PreAuthorize("isAuthenticated()")
    fun getRefSections(@PathVariable(value = "referenceId") referenceId: UUID)
        = sectionRepository.findAllByReferenceIdAndDeletedAtIsNullOrderByOrderNum(referenceId)

    @GetMapping("/list/{referenceId}/{sectionId}")
    @PreAuthorize("isAuthenticated()")
    fun getRefRecordByPage(
        @PathVariable(value = "referenceId") referenceId: UUID,
        @PathVariable(value = "sectionId") sectionId: UUID,
        @RequestParam(value = "page") page: Int = 1,
        @RequestParam(value = "size") size: Int = 20,
        @RequestParam(value = "fields") fields: String? = "",
        @RequestParam filter: MutableMap<String, String>? = mutableMapOf(),
        @RequestParam(value = "headerenable") headerEnable: Boolean = false,
        @RequestParam(value = "s") searchValue: String? = "",
        authentication: Authentication
    ): Page<MutableMap<String, Any?>> {
        return sectionService.getRefRecordByPage(
            referenceId, sectionId, page, size, fields, filter, headerEnable, searchValue, authentication
        )
    }

    @GetMapping("/header/{referenceId}/{sectionId}")
    @PreAuthorize("isAuthenticated()")
    fun getRefRecordHeader(
        @PathVariable(value = "referenceId") referenceId: UUID,
        @PathVariable(value = "sectionId") sectionId: UUID,
        authentication: Authentication
    ): MutableList<MutableMap<String, Any?>>  {
        return referenceNoteService.getReferenceFields(referenceId, sectionId)
    }

    @GetMapping("/get/autocomplete/{referenceId}/{sectionId}")
    @PreAuthorize("isAuthenticated()")
    fun getRecordAutocomplete(
        @PathVariable(value = "referenceId") referenceId: UUID,
        @PathVariable(value = "sectionId") sectionId: UUID,
        @RequestParam(value = "value") value: String,
        @RequestParam(value = "fields") fields: String,
        @RequestParam(value = "template") template: String?
    ): MutableList<MutableMap<String, Any?>> {
        val reference = referenceRepository.findByIdAndDeletedAtIsNull(referenceId)
        if (reference.isPresent) {
            val section = sectionRepository.findByIdAndDeletedAtIsNull(sectionId)
            if (!section.isPresent) {
                return arrayListOf()
            }
            val allField = Util.mergeMutableMap(Util.toJson(reference.get().getUserFields()), Util.toJson(reference.get().getSysFields()))
            val filters = section.get().getFilterField() as ArrayList<ArrayList<MutableMap<String, Any?>>>
            val conditionFilter = SectionService.prepareCondition(filters, allField)

            return recordNoteService.getRecordByValue(referenceId, value, fields, template, conditionFilter)
        }
        return arrayListOf()
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    fun createReference(@Valid @RequestBody section: Section, authentication: Authentication): ResponseEntity<*> {
        var status = Status()
        status.status = 0
        status.message = ""

        if (section.getReferenceId() == null) {
            status.message = "Reference fill!"
            return ResponseEntity(status, HttpStatus.OK)
        }

        section.creator = (profileService.getProfileReferenceById((authentication.principal as UserPrincipal).id))
        sectionRepository.save(section)

        if (section.getId() == null) {
            status.status = 0
            status.message = "Reference not created!"
            status.value = section
            return ResponseEntity(status, HttpStatus.BAD_REQUEST)
        }

        status.status = 1
        status.message = "Section's created!"
        status.value = section.getId()

        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/sortable")
    @PreAuthorize("hasRole('ADMIN')")
    fun editFieldSortable(
        @Valid @RequestBody sections: Array<MutableMap<String, Any?>>,
        authentication: Authentication
    ): ResponseEntity<*> {
        val status = Status()
        status.status = 0
        status.message = ""

        for (item in sections) {
            if (item["id"].toString().isNullOrEmpty()) {
                continue
            }
            val section = sectionRepository.findByIdAndDeletedAtIsNull(UUID.fromString(item["id"].toString()))
            if (section.isPresent) {
                section.get().setOrderNum(item["orderNum"].toString().toLongOrNull())
                sectionRepository.save(section.get())
            }
        }
        status.status = 1
        status.message = "Success"

        return ResponseEntity(status, HttpStatus.OK)
    }
}
