package com.mdsp.backend.app.reference.controller

import com.mdsp.backend.app.profile.repository.IProfileRepository
import com.mdsp.backend.app.profile.service.ProfileService
import com.mdsp.backend.app.profile.service.RoleService
import com.mdsp.backend.app.reference.model.RefRecord
import com.mdsp.backend.app.reference.model.fields.ComplexField
import com.mdsp.backend.app.reference.repository.IReferenceRepository
import com.mdsp.backend.app.reference.service.AccessService
import com.mdsp.backend.app.reference.service.RecordNoteService
import com.mdsp.backend.app.reference.service.SectionService
import com.mdsp.backend.app.structure.model.Structure
import com.mdsp.backend.app.structure.repository.IStructureRepository
import com.mdsp.backend.app.structure.service.RolesGroupService
import com.mdsp.backend.app.structure.service.StructureService
import com.mdsp.backend.app.system.config.DataSourceConfiguration
import com.mdsp.backend.app.system.model.GridQuery
import com.mdsp.backend.app.system.model.Status
import com.mdsp.backend.app.system.model.Util
import com.mdsp.backend.app.user.model.UserPrincipal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.sql.Timestamp
import java.util.*
import javax.validation.Valid


@RestController
@RequestMapping("/api/reference/record")
class RefRecordController() {

    @Autowired
    lateinit var referenceRepository: IReferenceRepository

    @Autowired
    lateinit var sectionService: SectionService

    @Autowired
    lateinit var dataSourceConfig: DataSourceConfiguration

    private lateinit var jdbc: JdbcTemplate

    @Autowired
    lateinit var profileService: ProfileService

    @Autowired
    lateinit var roleService: RoleService

    @Autowired
    lateinit var recordNoteService: RecordNoteService

    @Autowired
    lateinit var encoder: PasswordEncoder

    @Autowired
    lateinit var profileRepository: IProfileRepository

    @Autowired
    lateinit var rolesGroupService: RolesGroupService

    @Autowired
    lateinit var accessService: AccessService

    @Autowired
    lateinit var structureRepository: IStructureRepository

    @Autowired
    lateinit var structureService: StructureService

    @Autowired
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    private val PROFILE_REF: UUID = UUID.fromString("00000000-0000-0000-0000-000000000017")
    private val STRUCTURE_REF: UUID = UUID.fromString("00000000-0000-0000-0000-000000000018")
    private val ROLE_REF: UUID = UUID.fromString("00000000-0000-0000-0000-000000000019")
    private val POST_NEWS: UUID = UUID.fromString("e7e8fb19-d843-425b-b3dd-f1474cd8340f")

    @GetMapping("/list/{referenceId}")
    @PreAuthorize("isAuthenticated()")
    fun getRefRecordByPage(
        @PathVariable(value = "referenceId") referenceId: UUID,
        @RequestParam(value = "page") page: Int = 1,
        @RequestParam(value = "size") size: Int = 20,
        @RequestParam(value = "fields") fields: String? = "",
        @RequestParam(value = "template") template: String? = "",
        @RequestParam(value = "customised") customFields: Boolean = true,
        @RequestParam(value = "headerenable") headerEnable: Boolean = false,
        @RequestParam(value = "sids") sids: ArrayList<String>? = arrayListOf(),
        @RequestParam(value = "s") searchValue: String? = ""
    ): Page<MutableMap<String, *>>? {
        val pagePR: PageRequest = PageRequest.of(page - 1, size)
        val pagePRDefault: PageRequest = PageRequest.of(0, 1000)
        val reference = referenceRepository.findByIdAndDeletedAtIsNull(referenceId)
        var totalRows: Long = 0
        var result: MutableList<MutableMap<String, *>> = arrayListOf()
        var headerTable: MutableList<MutableMap<String, Any?>> = mutableListOf()

        if (reference.isPresent) {
            val grid = GridQuery(reference.get().getTableName(), dataSourceConfig)
            grid.setPageRequest(pagePRDefault)
            grid.addCondition("deleted_at IS NULL", arrayOf())
            val allField = Util.mergeMutableMap(Util.toJson(reference.get().getUserFields()), Util.toJson(reference.get().getSysFields()))
            grid.setRefFields(allField)
            headerTable = if (fields != null && fields.isNotEmpty()) {
                reference.get().getFolderSerialize(fields.split(",").toTypedArray())
            } else {
                reference.get().getFolderSerialize(reference.get().getFolderFields())
            }

            var searchCondition: Array<String> = arrayOf()
            for (item in headerTable) {
                grid.addColumn(item["id"].toString())
                if (searchValue != null && searchValue.isNotEmpty()) {
                    searchCondition = searchCondition.plus("lower(${item["id"].toString()}::character varying) LIKE lower(?)")
                }
            }
            if (searchCondition.isNotEmpty()) {
                val symbolQ = Array(searchCondition.size) { "%$searchValue%" }
                grid.addCondition(" AND (${searchCondition.joinToString(" OR ")})", arrayOf(*symbolQ))
            }
            totalRows = grid.countTotal() + result.size


            if (sids != null) {
                for (item in sids) {
                    grid.addOrder("id = '$item'", "DESC")
                }
            }

            result.addAll(grid.getDataPage(customFields))
        }



        val last = if (result.size >= page * size) page * size else if (result.size > 0) result.size else 0
        val first = if (result.size >= (page - 1) * size) (page - 1) * size else 0
        result = result.subList(first, last)

        if (headerEnable)
            result.add(0, mutableMapOf("header" to headerTable))

        return PageImpl(
            result,
            pagePR,
            totalRows
        )
    }

    @GetMapping("/get/selected/{referenceId}")
    @PreAuthorize("isAuthenticated()")
    fun getRefRecordByIds(
        @PathVariable(value = "referenceId") referenceId: UUID,
        @RequestParam(value = "fields") fields: String? = "",
        @RequestParam(value = "rec") recordIds: String? = "",
        @RequestParam(value = "customised") customFields: Boolean = true
    ): ResponseEntity<*> {
        if (recordIds.isNullOrEmpty() || fields.isNullOrEmpty()) {
            return ResponseEntity(arrayOf<String>(), HttpStatus.OK)
        }

        val reference = referenceRepository.findByIdAndDeletedAtIsNull(referenceId)
        val result: MutableList<MutableMap<String, *>> = arrayListOf()
        if (reference.isPresent) {
            val grid = GridQuery(reference.get().getTableName(), dataSourceConfig)
            if (!recordIds.isNullOrEmpty()) {
                val symbolQ = Array(recordIds.split(',').size) { "?" }
                grid.addCondition("id::character varying IN (${symbolQ.joinToString(",")}) AND",
                    recordIds.split(',').toTypedArray())
            }
            grid.addCondition("deleted_at IS NULL", arrayOf())
            val allField = Util.mergeMutableMap(Util.toJson(reference.get().getUserFields()), Util.toJson(reference.get().getSysFields()))
            grid.setRefFields(allField)

            if (!fields.isNullOrEmpty()) {
                for (item in fields.split(",")) {
                    grid.addColumn(item)
                }
            }
            result.addAll(grid.getDataPage(customFields))
        }
        return ResponseEntity(result, HttpStatus.OK)
    }

    @GetMapping("/exists/{referenceId}/{recordId}")
    @PreAuthorize("isAuthenticated()")
    fun existsRefRecord(
        @PathVariable(value = "referenceId") referenceId: UUID,
        @PathVariable(value = "recordId") recordId: UUID,
        authentication: Authentication
    ): Optional<Boolean> {
        val reference = referenceRepository.findByIdAndDeletedAtIsNull(referenceId)
        if (reference.isPresent) {
            val result = recordNoteService.getRecordById(recordId, reference.get())

            if (result.isEmpty()) {
                return Optional.of(false)
            }

            if (result["deleted_at"] != null) {
                return Optional.of(false)
            }
            // Access to view
            val profileId = (authentication.principal as UserPrincipal).id
            if (result.containsKey("owner") && result["owner"] != null) {
                val owner = UUID.fromString(result["owner"].toString())
                if (!accessService.mayView(profileId, reference.get(), owner)) {
                    return Optional.of(false)
                }
                result.remove("owner")
            }
            // Access to view
            return Optional.of(true)
        }
        return Optional.of(false)
    }

    @GetMapping("/get/{referenceId}/{recordId}")
    @PreAuthorize("isAuthenticated()")
    fun getRecord(
        @PathVariable(value = "referenceId") referenceId: UUID,
        @PathVariable(value = "recordId") recordId: UUID,
        authentication: Authentication
    ): MutableMap<String, Any?> {
        var result: MutableMap<String, Any?> = mutableMapOf()

        val reference = referenceRepository.findByIdAndDeletedAtIsNull(referenceId)

        if (reference.isPresent) {
            result = recordNoteService.getRecordById(recordId, reference.get())
            if (result["deleted_at"] != null) {
                return mutableMapOf()
            }
            // Access to view
            val profileId = (authentication.principal as UserPrincipal).id
            if (result.containsKey("owner") && result["owner"] != null) {
                val owner = UUID.fromString(result["owner"].toString())
                if (!accessService.mayView(profileId, reference.get(), owner)) {
                    return mutableMapOf()
                }
                result.remove("owner")
            }
            // Access to view

            if (referenceId == PROFILE_REF) {
                result["roles"] = rolesGroupService.getRolesByProfileString(recordId)
            }
        }
        return result
    }

    @GetMapping("/get-edit/{referenceId}/{recordId}")
    @PreAuthorize("isAuthenticated()")
    fun getRecordToEdit(
        @PathVariable(value = "referenceId") referenceId: UUID,
        @PathVariable(value = "recordId") recordId: UUID
    ): MutableMap<String, Any?> {
        var result: MutableMap<String, Any?> = mutableMapOf()

        val reference = referenceRepository.findByIdAndDeletedAtIsNull(referenceId)
        if (reference.isPresent) {
            result = recordNoteService.getRecordById(recordId, reference.get(), "edit")

            when (referenceId) {
                PROFILE_REF -> {
                    result["roles"] = rolesGroupService.getRolesByProfileMap(recordId)
                }
                ROLE_REF -> {

                }
            }
        }
        return result
    }

    @GetMapping("/get/autocomplete/{referenceId}")
    @PreAuthorize("isAuthenticated()")
    fun getRecordAutocomplete(
        @PathVariable(value = "referenceId") referenceId: UUID,
        @RequestParam(value = "value") value: String,
        @RequestParam(value = "fields") fields: String,
        @RequestParam(value = "template") template: String?
    ): MutableList<MutableMap<String, Any?>> {
        return recordNoteService.getRecordByValue(referenceId, value, fields, template)
    }

    @PostMapping("/create/{referenceId}")
    @PreAuthorize("isAuthenticated()")
    fun createReference(
        @PathVariable(value = "referenceId") referenceId: UUID,
        @Valid @RequestBody newRecord: MutableMap<String, Any?>,
        authentication: Authentication
    ): ResponseEntity<*> {
        val status = Status()
        status.status = 0

        val reference = referenceRepository.findByIdAndDeletedAtIsNull(referenceId)
        if (reference.isPresent) {
            // Access to add
            val profileId = (authentication.principal as UserPrincipal).id
            if (!accessService.mayAdd(profileId, reference.get())) {
                return ResponseEntity(status, HttpStatus.OK)
            }
            // Access to add
            var refRecord = RefRecord(null, reference.get(), dataSourceConfig)
            refRecord.newRecord()
            refRecord.setCreator(profileService.getProfileReferenceById((authentication.principal as UserPrincipal).id))
            refRecord.setDataField(newRecord)
            when (referenceId) {
                PROFILE_REF -> {
                    if (refRecord.getDataField().containsKey("username")) {
                        if (!ProfileService.isUsernameValid(refRecord.getDataField()["username"].toString().lowercase())) {
                            status.status = 0
                            status.message = "Username is not valid!"
                            return ResponseEntity(status, HttpStatus.OK)
                        }
                        val profileCandidate = profileRepository.findByUsernameIgnoreCaseAndDeletedAtIsNull(refRecord.getDataField()["username"].toString())
                        if (profileCandidate.isPresent) {
                            status.status = 0
                            status.message = "Username is present!"
                            return ResponseEntity(status, HttpStatus.OK)
                        }
                        refRecord.getDataField()["username"] = refRecord.getDataField()["username"].toString().lowercase()
                    }

                    if (
                        refRecord.getDataField().containsKey("email") && refRecord.getDataField()["email"].toString().isNotEmpty()
                    ) {
                        if (!ProfileService.isEmailValid(refRecord.getDataField()["email"].toString().lowercase())) {
                            status.status = 0
                            status.message = "Email is not valid!"
                            return ResponseEntity(status, HttpStatus.OK)
                        }
                        val profileCandidate = profileRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(refRecord.getDataField()["email"].toString())
                        if (profileCandidate.isPresent) {
                            status.status = 0
                            status.message = "Email is present!"
                            return ResponseEntity(status, HttpStatus.OK)
                        }
                        refRecord.getDataField()["email"] = refRecord.getDataField()["email"].toString().lowercase()
                    }
                    refRecord = profileService.setProfileRef(reference.get(), refRecord, newRecord)
                }
                ROLE_REF -> {
                    refRecord = roleService.setRoleRef(reference.get(), refRecord)
                }

            }
            if (!refRecord.isExisted() && refRecord.isValid()) {
                for (item in recordNoteService.getPasswordFields(reference.get())) {
                    if (refRecord.getDataField().containsKey(item)) {
                        if (!ProfileService.isPasswordValid(refRecord.getDataField()[item].toString())) {
                            status.status = 0
                            status.message = "Password is not valid!"
                            return ResponseEntity(status, HttpStatus.OK)
                        }
                        refRecord.getDataField()[item] = encoder.encode(refRecord.getDataField()[item].toString())
                    }
                }
                refRecord.save()
                status.status = 1
                status.message = "Record saved"
                status.value = refRecord.getRecordId()
            } else {
                status.message = refRecord.getErrorText()
            }
        }

        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/{referenceId}/{eml}")
    @PreAuthorize("isAuthenticated()")
    fun chekcByEmail(
        @PathVariable("referenceId") referenceId: UUID,
        @PathVariable("eml") eml: String
    ): ResponseEntity<*> {
        val email = eml.toLowerCase()
        val status = Status()
        status.status = 0
        status.message = "Reference does not exist"

        val reference = referenceRepository.findByIdAndDeletedAtIsNull(referenceId)
        if (reference.isPresent) {
            this.jdbc = JdbcTemplate(dataSourceConfig.dataBaseOneTemplate)
            val result = jdbc.queryForList(
                "SELECT * FROM student WHERE LOWER(email) = ? and deleted_at is null",
                email
            )
            if (result.isNotEmpty()) {
                for (r in result) {
                    var id = r["id"].toString()
                    var userStatus = r["status"].toString()
                    if (userStatus.contains("2")) {
                        val userResult: MutableMap<String, Any?> = mutableMapOf(
                            "id" to id,
                            "status" to userStatus
                        )
                        status.message = ""
                        status.value = userResult
                        status.status = 2
                        return ResponseEntity(status, HttpStatus.OK)
                    }
                }
                status.message = "Did not pass"
                status.status = 1
                return ResponseEntity(status, HttpStatus.OK)
            } else {
                this.jdbc = JdbcTemplate(dataSourceConfig.dataBaseOneTemplate)
                val result = jdbc.queryForList(
                    "SELECT * FROM student WHERE email = ?",
                    email
                )
                if (result.isNotEmpty()) {
                    status.status = 1
                    status.message = "Did not pass"
                } else {
                    status.status = 0
                    status.message = "Record does not exists"
                }
            }
        }
        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/edit/{referenceId}/{recordId}")
    @PreAuthorize("isAuthenticated()")
    fun editRecordReference(
        @PathVariable(value = "referenceId") referenceId: UUID,
        @PathVariable(value = "recordId") recordId: UUID,
        @Valid @RequestBody newRecord: MutableMap<String, Any?>,
        @RequestParam(value = "additional") additional: Boolean = false,
        authentication: Authentication
    ): ResponseEntity<*> {
        val status = Status()
        status.status = 0
        status.message = "Reference does not exist"

        val reference = referenceRepository.findByIdAndDeletedAtIsNull(referenceId)
        if (reference.isPresent) {
            var refRecord = RefRecord(recordId, reference.get(), dataSourceConfig)
            status.message = "RefRecord does not exist"
            if (refRecord.isExisted()) {
                refRecord.load()

                // Access to edit
                val profileId = (authentication.principal as UserPrincipal).id
                if (refRecord.getDataField().containsKey("owner") && refRecord.getDataField()["owner"] != null) {
                    val owner = UUID.fromString(refRecord.getDataField()["owner"].toString())
                    if (!accessService.mayEdit(profileId, reference.get(), owner, arrayListOf(recordId))) {
                        status.message = "Access denied"
                        return ResponseEntity(status, HttpStatus.OK)
                    }
                }
                // Access to edit

                var addRecord: MutableMap<String, Any?> = newRecord
                if (additional) {
                    addRecord = mutableMapOf()
                    for ((key, value) in newRecord) {
                        if (
                            !refRecord.getDataField().containsKey(key)
                            || refRecord.getAllFields()[key]!! !is ComplexField
                        ) {
                            continue
                        }
                        addRecord[key] = value
                        if (
                            refRecord.getAllFields().containsKey(key)
                            && refRecord.getAllFields()[key]!! is ComplexField
                            && refRecord.getDataField()[key] != null
                        ) {
                            addRecord[key] = refRecord.getDataField()[key]
                            for (item in (value as ArrayList<MutableMap<String, Any?>>)) {
                                if (
                                    addRecord[key] is ArrayList<*>
                                    && (addRecord[key] as ArrayList<MutableMap<String, Any?>>).none { p -> p["id"] == item["id"] }
                                ) {
                                    addRecord[key] = (addRecord[key] as ArrayList<MutableMap<String, Any?>>).plus(item)
                                }
                            }
                        }
                    }
                }
                refRecord.setDataField(addRecord)
                refRecord.setEditor(profileService.getProfileReferenceById((authentication.principal as UserPrincipal).id))
                when (referenceId) {
                    PROFILE_REF -> {
                        if (refRecord.getDataField().containsKey("username")) {
                            if (!ProfileService.isUsernameValid(refRecord.getDataField()["username"].toString().lowercase())) {
                                status.status = 0
                                status.message = "Username is not valid!"
                                return ResponseEntity(status, HttpStatus.OK)
                            }
                            val profileCandidate = profileRepository.findByUsernameIgnoreCaseAndDeletedAtIsNull(refRecord.getDataField()["username"].toString())
                            if (profileCandidate.isPresent && profileCandidate.get().getId() != recordId) {
                                status.status = 0
                                status.message = "Username is present!"
                                return ResponseEntity(status, HttpStatus.OK)
                            }
                            refRecord.getDataField()["username"] = refRecord.getDataField()["username"].toString().lowercase()
                        }

                        if (refRecord.getDataField().containsKey("email")) {
                            if (!ProfileService.isEmailValid(refRecord.getDataField()["email"].toString().lowercase())) {
                                status.status = 0
                                status.message = "Email is not valid!"
                                return ResponseEntity(status, HttpStatus.OK)
                            }
                            val profileCandidate = profileRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(refRecord.getDataField()["email"].toString())
                            if (profileCandidate.isPresent && profileCandidate.get().getId() != recordId) {
                                status.status = 0
                                status.message = "Email is present!"
                                return ResponseEntity(status, HttpStatus.OK)
                            }
                            refRecord.getDataField()["email"] = refRecord.getDataField()["email"].toString().lowercase()
                        }
                        refRecord = profileService.setProfileRef(reference.get(), refRecord, addRecord)
                    }
                    ROLE_REF -> {
                        refRecord = roleService.setRoleRef(reference.get(), refRecord)
                    }
                }
                if (refRecord.isValid()) {
                    for (item in recordNoteService.getPasswordFields(reference.get())) {
                        if (
                            addRecord.containsKey(item)
                            && addRecord[item] != null
                            && addRecord[item] != ""
                        ) {
                            if (!ProfileService.isPasswordValid(addRecord[item].toString())) {
                                status.status = 0
                                status.message = "Password is not valid!"
                                return ResponseEntity(status, HttpStatus.OK)
                            }
                            refRecord.getDataField()[item] = encoder.encode(addRecord[item].toString())
                        } else {
                            refRecord.removeKeyData(item)
                        }
                    }
                    refRecord.save()
                    status.status = 1
                    status.message = "Record saved"
                    status.value = refRecord.getRecordId()
                } else {
                    status.status = 0
                    status.message = refRecord.getErrorText()
                }

            }
        }

        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/edit/list/{referenceId}")
    @PreAuthorize("isAuthenticated()")
    fun editRecordsReference(
        @PathVariable(value = "referenceId") referenceId: UUID,
        @Valid @RequestBody newRecords: MutableMap<String, Any?>,
        @RequestParam(value = "additional") additional: Boolean = false,
        authentication: Authentication
    ): ResponseEntity<*> {
        val status = Status()
        status.status = 0
        status.message = ""
        val recordIds = newRecords["ids"] as ArrayList<String>
        val newRecord = newRecords["record"] as MutableMap<String, Any?>
        val reference = referenceRepository.findByIdAndDeletedAtIsNull(referenceId)
        if (reference.isPresent) {
            for(record in recordIds){
                val recordId = UUID.fromString(record)
                var refRecord = RefRecord(recordId, reference.get(), dataSourceConfig)
                if (refRecord.isExisted()) {
                refRecord.load()

                // Access to edit
                val profileId = (authentication.principal as UserPrincipal).id
                if (refRecord.getDataField().containsKey("owner") && refRecord.getDataField()["owner"] != null) {
                    val owner = UUID.fromString(refRecord.getDataField()["owner"].toString())
                    if (!accessService.mayEdit(profileId, reference.get(), owner, arrayListOf(recordId))) {
                        return ResponseEntity(status, HttpStatus.OK)
                    }
                }
                // Access to edit

                var addRecord: MutableMap<String, Any?> = newRecord
                if (additional) {
                    addRecord = mutableMapOf()
                    for ((key, value) in newRecord) {
                        if (
                            !refRecord.getDataField().containsKey(key)
                            || refRecord.getAllFields()[key]!! !is ComplexField
                        ) {
                            continue
                        }
                        addRecord[key] = value
                        if (
                            refRecord.getAllFields().containsKey(key)
                            && refRecord.getAllFields()[key]!! is ComplexField
                            && refRecord.getDataField()[key] != null
                        ) {
                            addRecord[key] = refRecord.getDataField()[key]
                            for (item in (value as ArrayList<MutableMap<String, Any?>>)) {
                                if (
                                    addRecord[key] is ArrayList<*>
                                    && (addRecord[key] as ArrayList<MutableMap<String, Any?>>).none { p -> p["id"] == item["id"] }
                                ) {
                                    addRecord[key] = (addRecord[key] as ArrayList<MutableMap<String, Any?>>).plus(item)
                                }
                            }
                        }
                    }
                }
                refRecord.setDataField(addRecord)
                refRecord.setEditor(profileService.getProfileReferenceById((authentication.principal as UserPrincipal).id))
                when (referenceId) {
                    PROFILE_REF -> {
                        if (refRecord.getDataField().containsKey("username")) {
                            if (!ProfileService.isUsernameValid(refRecord.getDataField()["username"].toString().lowercase())) {
                                status.status = 0
                                status.message = "Username is not valid!"
                                return ResponseEntity(status, HttpStatus.OK)
                            }
                            val profileCandidate = profileRepository.findByUsernameIgnoreCaseAndDeletedAtIsNull(refRecord.getDataField()["username"].toString())
                            if (profileCandidate.isPresent && profileCandidate.get().getId() != recordId) {
                                status.status = 0
                                status.message = "Username is present!"
                                return ResponseEntity(status, HttpStatus.OK)
                            }
                            refRecord.getDataField()["username"] = refRecord.getDataField()["username"].toString().lowercase()
                        }

                        if (refRecord.getDataField().containsKey("email")) {
                            if (!ProfileService.isEmailValid(refRecord.getDataField()["email"].toString().lowercase())) {
                                status.status = 0
                                status.message = "Email is not valid!"
                                return ResponseEntity(status, HttpStatus.OK)
                            }
                            val profileCandidate = profileRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(refRecord.getDataField()["email"].toString())
                            if (profileCandidate.isPresent && profileCandidate.get().getId() != recordId) {
                                status.status = 0
                                status.message = "Email is present!"
                                return ResponseEntity(status, HttpStatus.OK)
                            }
                            refRecord.getDataField()["email"] = refRecord.getDataField()["email"].toString().lowercase()
                        }
                        refRecord = profileService.setProfileRef(reference.get(), refRecord, addRecord)
                    }
                    ROLE_REF -> {
                        refRecord = roleService.setRoleRef(reference.get(), refRecord)
                    }
                }
                if (refRecord.isValid()) {
                    for (item in recordNoteService.getPasswordFields(reference.get())) {
                        if (
                            addRecord.containsKey(item)
                            && addRecord[item] != null
                            && addRecord[item] != ""
                        ) {
                            if (!ProfileService.isPasswordValid(addRecord[item].toString())) {
                                status.status = 0
                                status.message = "Password is not valid!"
                                return ResponseEntity(status, HttpStatus.OK)
                            }
                            refRecord.getDataField()[item] = encoder.encode(addRecord[item].toString())
                        } else {
                            refRecord.removeKeyData(item)
                        }
                    }
                    refRecord.save()
                    status.status = 1
                    status.message = "Record saved"
                    status.value = refRecord.getRecordId()
                } else {
                    status.status = 0
                    status.message = refRecord.getErrorText()
                }

            }
            }
        }

        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/remove/{referenceId}/{recordId}")
    @PreAuthorize("isAuthenticated()")
    fun removeRecordReference(
        @PathVariable(value = "referenceId") referenceId: UUID,
        @PathVariable(value = "recordId") recordId: UUID,
        @Valid @RequestBody newRecord: MutableMap<String, Any?>,
        authentication: Authentication
    ): ResponseEntity<*> {
        val status = Status()
        status.status = 0
        status.message = ""

        val reference = referenceRepository.findByIdAndDeletedAtIsNull(referenceId)
        if (reference.isPresent) {
            val refRecord = RefRecord(recordId, reference.get(), dataSourceConfig)
            if (refRecord.isExisted()) {
                // Access to edit
                val refRecordLoaded = RefRecord(recordId, reference.get(), dataSourceConfig)
                refRecordLoaded.load()
                val profileId = (authentication.principal as UserPrincipal).id
                if (refRecordLoaded.getDataField().containsKey("owner") && refRecordLoaded.getDataField()["owner"] != null) {
                    val owner = UUID.fromString(refRecordLoaded.getDataField()["owner"].toString())
                    if (!accessService.mayDelete(profileId, reference.get(), owner, arrayListOf(recordId))) {
                        return ResponseEntity(status, HttpStatus.OK)
                    }
                }

                when (referenceId) {
                    PROFILE_REF -> {
//                        val data = refRecord.getDataField()
                        refRecord.getDataField()["username"] = "${refRecordLoaded.getDataField()["username"].toString()}_deleted_${Timestamp(System.currentTimeMillis()).toString()}"
                        if (refRecordLoaded.getDataField()["email"] != null) {
                            refRecord.getDataField()["email"] = "${refRecordLoaded.getDataField()["email"].toString()}_deleted_${Timestamp(System.currentTimeMillis()).toString()}"
                        }
                        val profile = UUID.fromString(refRecord.getDataField()["id"].toString())
                        val profileStr = structureRepository.findAllByProfileIdAndDeletedAtIsNull(profile)
                        val structures: ArrayList<Structure> = arrayListOf()
                        for (str in profileStr){
                            structures.addAll(structureRepository.findAllByIdOrParentIdAndDeletedAtIsNull(str.getId()!!, str.getId()!!))
                        }
                        structureService.deleteRecursive(structures)
                    }
                    ROLE_REF -> {

                    }
                }

                // Access to edit
                refRecord.removeRecord()
                refRecord.setEditor(profileService.getProfileReferenceById((authentication.principal as UserPrincipal).id))
                refRecord.save()
                status.status = 1
                status.message = "Record removed"
            }
        }

        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/import/record/{referenceId}")
    @PreAuthorize("isAuthenticated()")
    fun importRecords(
        @PathVariable(value = "referenceId") referenceId: UUID,
        @Valid @RequestBody newRecord: ArrayList<MutableMap<String, Any?>>,
        authentication: Authentication
    ): ResponseEntity<*> {
        var status = Status()

        var reference = referenceRepository.findByIdAndDeletedAtIsNull(referenceId)
        if (reference.isPresent) {
            var iCount = 0
            var refRecord = RefRecord(null, reference.get(), dataSourceConfig)
            val fullFields = refRecord.getAllFields()
            for (item in newRecord) {
                refRecord.newRecord()
                refRecord.setCreator(profileService.getProfileReferenceById((authentication.principal as UserPrincipal).id))
                for ((key, fieldValue) in item) {
                    if (arrayOf("reference", "structure").contains(fullFields[key]!!.getConfig()["type"])) {
                        val config = fullFields[key]!!.getConfig()
                        val searchValue = recordNoteService.getRecordByValue(
                            UUID.fromString(config["referenceId"].toString()),
                            fieldValue.toString().replace("  ", " ").trim(),
                            (config["fields"] as ArrayList<*>).joinToString(","),
                            config["templateView"] as String
                        )
                        if (searchValue.size > 0) {
                            item[key] = arrayListOf(mutableMapOf("id" to searchValue[0]["id"].toString(), "value" to searchValue[0]["value"]))
                        } else {
                            item[key] = null
                        }
                    } else if (arrayOf("enumeration").contains(fullFields[key]!!.getConfig()["type"])) {
                        val config = fullFields[key]!!.getConfig()
                        val values = config["values"] as ArrayList<MutableMap<String, Any?>>
                        var found = false
                        for (search in values) {
                            if (search["value"].toString().toLowerCase().trim() == fieldValue.toString().toLowerCase().trim()) {
                                found = true
                                item[key] = arrayListOf(search)
                                break
                            }
                        }
                        if (!found) {
                            item[key] = null
                        }
                    }
                }
                if (referenceId == UUID.fromString("00000000-0000-0000-0000-000000000017")) {
                    if (!item.containsKey("username") || item["username"].toString().isEmpty()) {
                        if (item.containsKey("email") && item["email"].toString().isNotEmpty()) {
                            item["username"] = item["email"].toString().substringBefore("@")
                        }
                    }
                    item["password"] = encoder.encode("Qwerty2021")
                    item["enabled"] = true
                }
                refRecord.setDataField(item)
                if (!refRecord.isExisted()) {
                    refRecord.save()
                    iCount++

                    //TODO baska variant karastiru
//                    val userRole = UsersRoles(
//                        0,
//                        UUID.fromString(refRecord.getDataField()["id"].toString()),
//                        1,
//                        null
//                    )
//                    usersRoleRepository.save(userRole)
                }
                status.value = refRecord.getDataField()
            }
            if (iCount == newRecord.size) {
                status.status = 1
            }
            status.message = "$iCount/${newRecord.size}"
        }

        return ResponseEntity(status, HttpStatus.OK)
    }

}
