package com.mdsp.backend.app.reference.controller

import com.mdsp.backend.app.profile.service.ProfileService
import com.mdsp.backend.app.reference.model.Reference
import com.mdsp.backend.app.reference.model.fields.FieldFactory
import com.mdsp.backend.app.reference.repository.IReferenceAccessRepository
import com.mdsp.backend.app.reference.repository.IReferenceRepository
import com.mdsp.backend.app.reference.service.ReferenceNoteService
import com.mdsp.backend.app.system.config.DataSourceConfiguration
import com.mdsp.backend.app.system.model.EngineQuery
import com.mdsp.backend.app.system.model.Status
import com.mdsp.backend.app.system.model.Util
import com.mdsp.backend.app.user.model.UserPrincipal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.sql.Timestamp
import java.util.*
import javax.validation.Valid


@RestController
@RequestMapping("/api/reference")
class ReferenceController() {

    @Autowired
    lateinit var referenceRepository: IReferenceRepository

    @Autowired
    lateinit var referenceNoteService: ReferenceNoteService

    @Autowired
    lateinit var profileService: ProfileService

    @Autowired
    private lateinit var dataSourceConfig: DataSourceConfiguration

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    fun getReferencesByPage(
        @RequestParam(value = "page") page: Int = 1,
        @RequestParam(value = "size") size: Int = 20
    ): Page<Reference> {
        val pagePR: PageRequest = PageRequest.of(page - 1, size)
        return referenceRepository.findAllByDeletedAtIsNull(pagePR)
    }

    @GetMapping("/list/all")
    @PreAuthorize("hasRole('ADMIN')")
    fun getReferences() = referenceRepository.findAllByDeletedAtIsNull()

    @GetMapping("/get/{referenceId}")
    @PreAuthorize("isAuthenticated()")
    fun getReference(@PathVariable(value = "referenceId") referenceId: UUID) = referenceRepository.findByIdAndDeletedAtIsNull(referenceId)

    @GetMapping("/get-fields/{referenceId}")
    @PreAuthorize("isAuthenticated()")
    fun getReferenceFields(@PathVariable(value = "referenceId") referenceId: UUID): MutableMap<String, Any?> {
        val reference = referenceRepository.findByIdAndDeletedAtIsNull(referenceId)
        var editField: MutableMap<String, Any?> = mutableMapOf()
        if (reference.isPresent) {
            val userFields: MutableMap<String, Any?> = Util.toJson(reference.get().getUserFields())
            val sysFields: MutableMap<String, Any?> = Util.toJson(reference.get().getSysFields())

            editField = Util.mergeMutableMap(userFields, sysFields)
        }


        return editField
    }


    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    fun createReference(@Valid @RequestBody newReference: Reference, authentication: Authentication): ResponseEntity<*> {
        var status = Status()
        status.status = 0
        status.message = ""

        newReference.setTableName("ref_" + Util.getRandomString(7))
        referenceRepository.save(newReference)

        if (newReference.getId() == null) {
            status.status = 0
            status.message = "Reference not created!"
            status.value = newReference
            return ResponseEntity(status, HttpStatus.BAD_REQUEST)
        }
        var refTableCreate = referenceNoteService.createTableNote(newReference)
        if (refTableCreate == "OK") {
            status.status = 1
            status.message = "Reference created!"
            status.value = newReference.getId()
        } else {
            status.status = 0
            status.message = refTableCreate
            status.value = null

            referenceRepository.delete(newReference)
        }

        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/edit")
    @PreAuthorize("hasRole('ADMIN')")
    fun editReference(@Valid @RequestBody newReference: Reference, authentication: Authentication): ResponseEntity<*> {
        var status = Status()
        var ref = referenceRepository.findByIdAndDeletedAtIsNull(newReference.getId()!!)
        if (ref.isPresent) {
            newReference.editor = (profileService.getProfileReferenceById((authentication.principal as UserPrincipal).id))
            newReference.updatedAt = (Timestamp(System.currentTimeMillis()))
            newReference.setTableName(ref.get().getTableName())
            referenceRepository.save(newReference)

            status.status = 1
            status.message = "Reference saved!"
            status.value = newReference.getId()
        }

        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/{referenceId}/edit/field/{fieldId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun editField(
        @Valid @RequestBody fieldReference: MutableMap<String, Any?>,
        @PathVariable referenceId: UUID,
        @PathVariable fieldId: String,
        authentication: Authentication
    ): ResponseEntity<*> {
        var status = Status()
        status.status = 0
        status.message = ""

        var reference = referenceRepository.findById(referenceId)
        if (reference.isPresent) {
            val newField = FieldFactory.create(fieldReference["title"] as String, fieldReference)
            newField.setIsConfig(true)
            newField.setConfig()
            if (newField.getError().isNotEmpty()) {
                status.status = 0
                status.message = newField.getError()
                status.value = newField
                return ResponseEntity(status, HttpStatus.OK)
            }

            var userFields: MutableMap<String, Any?> = Util.toJson(reference.get().getUserFields())
            var sysFields: MutableMap<String, Any?> = Util.toJson(reference.get().getSysFields())
            var editField = Util.mergeMutableMap(userFields, sysFields)
            var nameField: String = fieldId
            var resultCreateField: String? = null
            var canSave = true
            if (fieldId == "new") {
                nameField = "b_" + Util.getRandomString(7)
                newField.setOrderNum(editField.size)
                resultCreateField = referenceNoteService.createFieldTable(reference.get(), nameField, newField.getConfig())
                canSave = false
            }

            if (resultCreateField == "OK" || canSave) {
                userFields[nameField] = newField.getConfig()

                reference.get().setUserFields(Util.mutableMapAsString(userFields))
                reference.get().editor = (profileService.getProfileReferenceById((authentication.principal as UserPrincipal).id))
                reference.get().updatedAt = (Timestamp(System.currentTimeMillis()))

                if (canSave) {
                    resultCreateField = referenceNoteService.changeFieldDefaultTable(
                        reference.get(),
                        nameField,
                        newField.getConfig()["defaultValue"],
                        newField.getConfig()["type"] as String
                    )
                    if (resultCreateField != "OK") {
                        status.status = 0
                        status.message = resultCreateField
                        status.value = newField
                        return ResponseEntity(status, HttpStatus.OK)
                    }
                }

                referenceRepository.save(reference.get())

                status.status = 1
                status.message = "Reference's field saved!"
                status.value = nameField
            } else {
                status.status = 0
                status.message = resultCreateField
                status.value = newField
            }
        }

        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/{referenceId}/edit/field/sortable")
    @PreAuthorize("hasRole('ADMIN')")
    fun editFieldSortable(
        @Valid @RequestBody fieldReference: Array<MutableMap<String, Any?>>,
        @PathVariable referenceId: UUID,
        authentication: Authentication
    ): ResponseEntity<*> {
        var status = Status()
        status.status = 0
        status.message = ""

        var reference = referenceRepository.findById(referenceId)
        if (reference.isPresent) {
            var userFields = Util.toMutableJson(reference.get().getUserFields())
            var sysFields = Util.toMutableJson(reference.get().getSysFields())

            for (item in fieldReference) {
                if (item["orderNum"] != null) {
                    if (userFields[item["id"]] != null) {
                        val refField = FieldFactory.create(userFields[item["id"]]?.get("title") as String, userFields[item["id"]]!!)
                        refField.setOrderNum(item["orderNum"].toString().toInt())
                    } else if (sysFields[item["id"]] != null) {
                        val refField = FieldFactory.create(sysFields[item["id"]]?.get("title") as String, sysFields[item["id"]]!!)
                        refField.setOrderNum(item["orderNum"].toString().toInt())
                    }
                }
            }
            reference.get().setUserFields(Util.mutableMapAsString(userFields))
            reference.get().setSysFields(Util.mutableMapAsString(sysFields))
            reference.get().updatedAt = (Timestamp(System.currentTimeMillis()))
            referenceRepository.save(reference.get())

            status.status = 1
            status.message = "Reference's field saved!"
        }

        return ResponseEntity(status, HttpStatus.OK)
    }

    @DeleteMapping("/delete/field/{referenceId}/{fieldId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun removeField(
        @PathVariable(value = "referenceId") referenceId: UUID,
        @PathVariable(value = "fieldId") fieldId: String,
        authentication: Authentication
    ): ResponseEntity<*> {
        val status = Status()
        status.status = 0
        status.message = ""
        if (ReferenceNoteService.isSystemReference(referenceId)) {
            status.message = "You can't delete System reference!"
            return ResponseEntity(status, HttpStatus.OK)
        }
        val reference = referenceRepository.findById(referenceId)
        if (reference.isPresent) {
            val engineQuery = EngineQuery(reference.get().getTableName(), dataSourceConfig)
            val isExistsColumn = (engineQuery.isColumnNotExisted(fieldId) > 0)

            val userFields = Util.toMutableJson(reference.get().getUserFields())
            val sysFields = Util.toMutableJson(reference.get().getSysFields())
            var removeIndex = 0
            var resultCreateField: String? = null
            if (userFields[fieldId] != null) {
                removeIndex = userFields[fieldId]?.get("orderNum") as Int
                userFields.remove(fieldId)
                resultCreateField = referenceNoteService.removeFieldTable(reference.get(), fieldId)
            } else if (sysFields[fieldId] != null) {
                removeIndex = sysFields[fieldId]?.get("orderNum") as Int
                sysFields.remove(fieldId)
                resultCreateField = referenceNoteService.removeFieldTable(reference.get(), fieldId)
            }
            if (resultCreateField == "OK" || !isExistsColumn) {
                referenceNoteService.fixSortOrders(userFields, removeIndex)
                referenceNoteService.fixSortOrders(sysFields, removeIndex)
                reference.get().setUserFields(Util.mutableMapAsString(userFields))
                reference.get().setSysFields(Util.mutableMapAsString(sysFields))
                reference.get().updatedAt = (Timestamp(System.currentTimeMillis()))
                referenceRepository.save(reference.get())

                status.status = 1
                status.message = "Reference's field saved!"
            } else {
                status.message = resultCreateField
            }

        }

        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/{referenceId}/edit/field/replace/{fieldOldId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun editFieldId(
        @Valid @RequestBody fieldNewId: String,
        @PathVariable fieldOldId: String,
        @PathVariable referenceId: UUID,
        authentication: Authentication
    ): ResponseEntity<*> {
        var status = Status()
        status.status = 0
        status.message = ""

        var reference = referenceRepository.findById(referenceId)
        if (reference.isPresent) {
            var userFields = Util.toMutableJson(reference.get().getUserFields())
            var sysFields = Util.toMutableJson(reference.get().getSysFields())

            var resultCreateField: String? = null
            if (userFields[fieldOldId] != null) {
                userFields[fieldOldId]?.let { userFields.put(fieldNewId.toLowerCase(), it) }
                userFields.remove(fieldOldId)
                resultCreateField = referenceNoteService.changeFieldIdTable(reference.get(), fieldOldId, fieldNewId.toLowerCase())
            } else if (sysFields[fieldOldId] != null) {
                sysFields[fieldOldId]?.let { sysFields.put(fieldNewId.toLowerCase(), it) }
                sysFields.remove(fieldOldId)
                resultCreateField = referenceNoteService.changeFieldIdTable(reference.get(), fieldOldId, fieldNewId.toLowerCase())
            }
            if (resultCreateField == "OK") {
                reference.get().setUserFields(Util.mutableMapAsString(userFields))
                reference.get().setSysFields(Util.mutableMapAsString(sysFields))
                reference.get().updatedAt = (Timestamp(System.currentTimeMillis()))
                referenceRepository.save(reference.get())

                status.status = 1
                status.message = "Reference's field saved!"
                status.value = userFields[fieldNewId.toLowerCase()]?.get("type")
            } else {
                status.message = resultCreateField
            }
        }
        return ResponseEntity(status, HttpStatus.OK)
    }
}
