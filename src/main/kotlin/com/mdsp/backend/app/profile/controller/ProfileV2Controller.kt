package com.mdsp.backend.app.profile.controller

import com.mdsp.backend.app.files.model.Files
import com.mdsp.backend.app.files.repository.IFilesRepository
import com.mdsp.backend.app.files.service.FilesService
import com.mdsp.backend.app.profile.model.*
import com.mdsp.backend.app.profile.repository.*
import com.mdsp.backend.app.profile.service.ProfileService
import com.mdsp.backend.app.reference.model.RefRecord
import com.mdsp.backend.app.reference.model.fields.ComplexField
import com.mdsp.backend.app.reference.repository.IReferenceRepository
import com.mdsp.backend.app.reference.service.AccessService
import com.mdsp.backend.app.reference.service.RecordNoteService
import com.mdsp.backend.app.structure.service.RolesGroupService
import com.mdsp.backend.app.system.config.DataSourceConfiguration
import com.mdsp.backend.app.system.model.Status
import com.mdsp.backend.app.user.model.UserPrincipal
import com.mdsp.backend.app.user.model.payload.UserAuthRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileNotFoundException
import java.sql.Timestamp
import java.util.*
import javax.validation.Valid
import kotlin.collections.ArrayList


@RestController
@RequestMapping("/api/v2/profiles")
class ProfileV2Controller {

    @Autowired
    lateinit var profileRepository: IProfileRepository

    @Autowired
    lateinit var referenceRepository: IReferenceRepository

    @Autowired
    lateinit var recordNoteService: RecordNoteService

    @Autowired
    lateinit var rolesGroupService: RolesGroupService

    @Autowired
    lateinit var profileService: ProfileService

    @Autowired
    lateinit var filesRepository: IFilesRepository

    @Value("\${file.tmp.upload-dir}")
    private val pathFiles: String = ""

    @Autowired
    lateinit var encoder: PasswordEncoder

    @Autowired
    lateinit var dataSourceConfig: DataSourceConfiguration

    @Autowired
    lateinit var accessService: AccessService

    private val qualityCompress = arrayListOf("s", "m", "l")

    private val PROFILE_REF: UUID = UUID.fromString("00000000-0000-0000-0000-000000000017")

    //Data for edit page
    @GetMapping("/user-avatar")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    fun getUserImage(authentication: Authentication): ResponseEntity<*> {
        var status = Status()
        val user = profileRepository.findByEmailAndDeletedAtIsNull(authentication.name)
        if (user.isPresent && user.get().getAvatarRef().isNotEmpty()) {
            status.status = 1
            status.message = user.get().getAvatarRef("thumb_", "s")
        }
        return ResponseEntity(status, HttpStatus.OK)
    }

    //My Profile
    @GetMapping("/user/type/{type}")
    @PreAuthorize("isAuthenticated()")
    fun getUser(@PathVariable(value = "type") type: String, authentication: Authentication): MutableMap<String, Any?> {
        val profileCandidate: Optional<Profile> = profileRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(authentication.name!!)
        val reference = referenceRepository.findByIdAndDeletedAtIsNull(PROFILE_REF)
        var result: MutableMap<String, Any?> = mutableMapOf()
        if (reference.isPresent && profileCandidate.isPresent) {
            result = recordNoteService.getRecordById(profileCandidate.get().getId()!!, reference.get(), type)
            result["id"] = profileCandidate.get().getId()
            if (type == "view") {
                result["roles"] = rolesGroupService.getRolesByProfileString(profileCandidate.get().getId()!!)
            } else if (type == "edit") {
                result["roles"]  = rolesGroupService.getRolesByProfileMap(profileCandidate.get().getId()!!)
            }
            result["enable_notification"] = profileCandidate.get().enableNotification
        }
        return result
    }

    @GetMapping(value = ["/user/{id}/{type}", "/user/{id}"])
    @PreAuthorize("hasRole('ADMIN') or hasRole('CHIEF') or hasRole('HEAD_CITY') or hasRole('MENTOR')")
    fun getUserDetails(
        @PathVariable(value = "id") id: UUID,
        @PathVariable(required = false) type: String?,
        @RequestParam(value = "fields") fields: Array<String>,
    ): MutableMap<String, Any?> {
        val reference = referenceRepository.findByIdAndDeletedAtIsNull(PROFILE_REF)
        var result: MutableMap<String, Any?> = mutableMapOf()
        var view = "view"
        if (type != null)
            view = type
        if (reference.isPresent) {
            result = recordNoteService.getRecordById(id, reference.get(), view, fields)
            result["id"] = id
            if (view == "view") {
                result["roles"]  = rolesGroupService.getRolesByProfileString(id)
            }
            if (view == "edit") {
                result["roles"]  = rolesGroupService.getRolesByProfileMap(id)
            }
        }
        return result
    }

    // Edit page to db
    // My user password
    @PostMapping(value = ["/acc/user/edit", "/my/user/edit"])
    @PreAuthorize("isAuthenticated()")
    fun editMyUser(
        @Valid @RequestBody infoUser: UserAuthRequest,
        authentication: Authentication
    ): ResponseEntity<*> {
        val status = Status()
        status.status = 0
        status.message = ""
        val reference = referenceRepository.findByIdAndDeletedAtIsNull(PROFILE_REF)
        val recordId = (authentication.principal as UserPrincipal).id
        if (reference.isPresent) {
            val refRecord = RefRecord(recordId, reference.get(), dataSourceConfig)
            if (refRecord.isExisted()) {
                val profile = profileRepository.findByIdAndDeletedAtIsNull(recordId)
                refRecord.load()
                val newRecord: MutableMap<String, Any?> = mutableMapOf()
                newRecord["username"] = infoUser.username
                newRecord["email"] = infoUser.email
                newRecord["password"] = infoUser.newPassword
                refRecord.setDataField(newRecord)
                refRecord.setEditor(profileService.getProfileReferenceById(recordId))

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


                if (refRecord.isValid()) {
                    if (
                        !infoUser.newPassword.isNullOrBlank()
                        && !infoUser.confirmPassword.isNullOrBlank()
                        && !infoUser.password.isNullOrBlank()
                    ) {
                        if (infoUser.newPassword != infoUser.confirmPassword)  {
                            status.message = "Passwords are not same"
                            return ResponseEntity(status, HttpStatus.OK)
                        }

                        if (!encoder.matches(infoUser.password, profile.get().pwd())) {
                            status.message = "Current is not true"
                            return ResponseEntity(status, HttpStatus.OK)
                        }

                        if (!ProfileService.isPasswordValid(infoUser.newPassword!!)) {
                            status.status = 0
                            status.message = "Password is not valid!"
                            return ResponseEntity(status, HttpStatus.OK)
                        }
                        refRecord.getDataField()["password"] = encoder.encode(infoUser.newPassword)
                    } else {
                        refRecord.removeKeyData("password")
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

    // Edit another user password
    @PostMapping(value = ["/acc/user/edit/{id}", "/user/edit/{id}"])
    @PreAuthorize("isAuthenticated()")
    fun editUserInfo(
        @PathVariable(value = "id") id: UUID,
        @Valid @RequestBody infoUser: UserAuthRequest,
        authentication: Authentication
    ): ResponseEntity<*> {
        val status = Status()
        status.status = 0
        status.message = ""

        val reference = referenceRepository.findByIdAndDeletedAtIsNull(PROFILE_REF)
        if (reference.isPresent) {
            val refRecord = RefRecord(id, reference.get(), dataSourceConfig)
            if (refRecord.isExisted()) {
                refRecord.load()
                val newRecord: MutableMap<String, Any?> = mutableMapOf()
                newRecord["username"] = infoUser.username
                newRecord["email"] = infoUser.email
                newRecord["password"] = infoUser.newPassword
                refRecord.setDataField(newRecord)
                refRecord.setEditor(profileService.getProfileReferenceById(id))

                // Access to edit
                val profileId = (authentication.principal as UserPrincipal).id
                if (refRecord.getDataField().containsKey("owner") && refRecord.getDataField()["owner"] != null) {
                    val owner = UUID.fromString(refRecord.getDataField()["owner"].toString())
                    if (!accessService.mayEdit(profileId, reference.get(), owner, arrayListOf(id))) {
                        return ResponseEntity(status, HttpStatus.OK)
                    }
                }
                // Access to edit

                if (refRecord.getDataField().containsKey("email")) {
                    if (!ProfileService.isEmailValid(refRecord.getDataField()["email"].toString().lowercase())) {
                        status.status = 0
                        status.message = "Email is not valid!"
                        return ResponseEntity(status, HttpStatus.OK)
                    }
                    val profileCandidate = profileRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(refRecord.getDataField()["email"].toString())
                    if (profileCandidate.isPresent && profileCandidate.get().getId() != id) {
                        status.status = 0
                        status.message = "Email is present!"
                        return ResponseEntity(status, HttpStatus.OK)
                    }
                    refRecord.getDataField()["email"] = refRecord.getDataField()["email"].toString().lowercase()
                }


                if (refRecord.isValid()) {
                    if (
                        !infoUser.newPassword.isNullOrBlank()
                        && !infoUser.confirmPassword.isNullOrBlank()
                    ) {
                        if (infoUser.newPassword != infoUser.confirmPassword)  {
                            status.message = "Passwords are not same"
                            return ResponseEntity(status, HttpStatus.OK)
                        }
                        if (!ProfileService.isPasswordValid(infoUser.newPassword!!)) {
                            status.status = 0
                            status.message = "Password is not valid!"
                            return ResponseEntity(status, HttpStatus.OK)
                        }
                        refRecord.getDataField()["password"] = encoder.encode(infoUser.newPassword)
                    } else {
                        refRecord.removeKeyData("password")
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

    // Edit user info all
    @PostMapping(value = ["/acc/user/edit/all"])
    @PreAuthorize("isAuthenticated()")
    fun editPageRecordReference(
        @Valid @RequestBody newRecord: MutableMap<String, Any?>,
        @RequestParam(value = "additional") additional: Boolean = false,
        authentication: Authentication
    ): ResponseEntity<*> {
        val status = Status()
        status.status = 0
        status.message = ""

        val reference = referenceRepository.findByIdAndDeletedAtIsNull(PROFILE_REF)

        val profileId = (authentication.principal as UserPrincipal).id
        val recordId = if (newRecord.containsKey("id")) { UUID.fromString(newRecord["id"] as String) } else { profileId }
        if (reference.isPresent) {
            var access = false
            var refRecord = RefRecord(recordId, reference.get(), dataSourceConfig)
            if (refRecord.isExisted()) {
                refRecord.load()

                // Access to edit
                if (refRecord.getDataField().containsKey("owner") && refRecord.getDataField()["owner"] != null) {
                    val owner = UUID.fromString(refRecord.getDataField()["owner"].toString())
                    access = accessService.mayEdit(profileId, reference.get(), owner, arrayListOf(recordId))
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
                refRecord.setEditor(profileService.getProfileReferenceById(recordId))

                if (!access) {
                    addRecord.remove("fio")
                    addRecord.remove("first_name")
                    addRecord.remove("middle_name")
                    refRecord.removeKeyData("fio")
                    refRecord.removeKeyData("first_name")
                    refRecord.removeKeyData("middle_name")
                }
                else {
                    if (addRecord.containsKey("roles")) {
                        val roles = addRecord["roles"] as ArrayList<MutableMap<String, Any?>>
                        val childRoles = rolesGroupService.getChildRolesUUIDByProfile(profileId)
                        if (profileId == recordId) {
                            childRoles.addAll(rolesGroupService.getRolesUUIDByProfile(profileId))
                        }
                        for (role in roles) {
                            if (!childRoles.contains(role["id"] as String)) {
                                addRecord.remove("roles")
                                break
                            }
                        }
                    }
                }

                refRecord.removeKeyData("username")

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


                if (refRecord.isValid()) {
                    for (item in recordNoteService.getPasswordFields(reference.get())) {
                        refRecord.removeKeyData(item)
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

    @PostMapping(value = ["/profile/user/edit/all", "/user/edit"])
    @PreAuthorize("isAuthenticated()")
    fun editRecordReference(
        @Valid @RequestBody newRecord: MutableMap<String, Any?>,
        @RequestParam(value = "additional") additional: Boolean = false,
        authentication: Authentication
    ): ResponseEntity<*> {
        val status = Status()
        status.status = 0
        status.message = ""

        val reference = referenceRepository.findByIdAndDeletedAtIsNull(PROFILE_REF)
        val recordId = (authentication.principal as UserPrincipal).id
        if (reference.isPresent) {
            var refRecord = RefRecord(recordId, reference.get(), dataSourceConfig)
            if (refRecord.isExisted()) {
                refRecord.load()

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
                refRecord.setEditor(profileService.getProfileReferenceById(recordId))


                refRecord.removeKeyData("username")

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

    @PostMapping(value = ["/acc/user/edit/avatar", "/user/edit/avatar"])
    @PreAuthorize("isAuthenticated()")
    fun userAvatarUpload(
        @RequestParam("file") fileForm: MultipartFile,
        authentication: Authentication
    ): ResponseEntity<*> {
        val status = Status()
        if (!fileForm.contentType.toString().contains("image/", true)) {
            status.message = "File must image"
            return ResponseEntity(status, HttpStatus.BAD_REQUEST)
        }
        val profile = profileRepository.findByIdAndDeletedAtIsNull((authentication.principal as UserPrincipal).id)
        if (!profile.isPresent) {
            return ResponseEntity(status, HttpStatus.OK)
        }
        val file: File = FilesService.multipartFileToFile(fileForm, pathFiles)
        val hashFiles = FilesService.sha256Checksum(file)
        val fileType = fileForm.originalFilename!!.substringAfterLast(".")
        val fileName = fileForm.originalFilename!!.substringBeforeLast(".")
        run creatingFile@ {
            val filesDB = filesRepository.findAllByHashFileAndDeletedAtIsNull(hashFiles)
            for (files in filesDB) {
                if (fileForm.contentType == files.getMime()) {
                    files.editor = (profileService.getProfileReferenceById((authentication.principal as UserPrincipal).id))
                    files.updatedAt = (Timestamp(System.currentTimeMillis()))
                    files.setName(fileName)
                    if (!FilesService.filesExists(files.getFilesFullPath(pathFiles))) {
                        FilesService.createFile(file, pathFiles, files)
                    }
                    filesRepository.save(files)
                    for (quality in qualityCompress) {
                        FilesService.imageCompressingAndResizeAndSave(pathFiles, files, quality, "thumb_")
                    }
                    file.delete()
                    profile.get().setAvatarRef(arrayListOf(mutableMapOf("id" to files.getId(), "value" to fileName)))
                    profileRepository.save(profile.get())
                    status.value = files.getId() /*+ ":" + fileKey*/
                    status.message = "Successful uploaded"
                    status.status = 1
                    return@creatingFile
                }
            }

            val files = Files(null)
            files.creator = (profileService.getProfileReferenceById((authentication.principal as UserPrincipal).id))
            files.setFilesFormat(fileType)
            files.setMime(fileForm.contentType)
            files.setHashFile(hashFiles)
            files.setSize(fileForm.size)
            files.setName(fileName)
            filesRepository.save(files)

            FilesService.createFile(file, pathFiles, files)

            for (quality in qualityCompress) {
                FilesService.imageCompressingAndResizeAndSave(pathFiles, files, quality, "thumb_")
            }
            profile.get().setAvatarRef(arrayListOf(mutableMapOf("id" to files.getId(), "value" to fileName)))
            profileRepository.save(profile.get())

            try {
                status.value = files.getId() /*+ ":" + fileKey*/
                status.message = "Successful uploaded"
                status.status = 1

            } catch (ex: FileNotFoundException) {
                status.value = ex.message
                status.message = "File not saved"
                status.status = 0
            }
        }
        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/switch-notification")
    @PreAuthorize("isAuthenticated()")
    fun enableNotify(
        @Valid @RequestBody enableNotify: Boolean,
        authentication: Authentication
    ): ResponseEntity<*> {
        val status = Status()
        val profile = profileRepository.findByIdAndDeletedAtIsNull((authentication.principal as UserPrincipal).id)
        if (profile.isPresent) {
            profile.get().enableNotification = enableNotify
            profileRepository.save(profile.get())
            status.status = 1
        }
        return ResponseEntity(status, HttpStatus.OK)
    }
}
