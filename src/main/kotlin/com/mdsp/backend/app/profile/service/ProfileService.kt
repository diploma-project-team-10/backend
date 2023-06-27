package com.mdsp.backend.app.profile.service

import com.mdsp.backend.app.profile.model.Profile
import com.mdsp.backend.app.profile.repository.IProfileRepository
import com.mdsp.backend.app.reference.model.RefRecord
import com.mdsp.backend.app.reference.model.Reference
import com.mdsp.backend.app.reference.repository.IReferenceRepository
import com.mdsp.backend.app.structure.repository.IRoleGroupRepository
import com.mdsp.backend.app.structure.repository.IStructureRepository
import com.mdsp.backend.app.structure.service.RolesGroupService
import com.mdsp.backend.app.system.config.DataSourceConfiguration
import com.mdsp.backend.app.system.model.GridQuery
import com.mdsp.backend.app.system.model.Util
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageImpl
import org.springframework.stereotype.Service
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

@Service
class ProfileService {

    @Autowired
    lateinit var profileRepository: IProfileRepository

    @Autowired
    lateinit var structureRepository: IStructureRepository

    @Autowired
    lateinit var referenceRepository: IReferenceRepository

    @Autowired
    lateinit var dataSourceConfig: DataSourceConfiguration

    @Autowired
    lateinit var rolesGroupService: RolesGroupService

    @Autowired
    private lateinit var roleGroupRepository: IRoleGroupRepository

    fun getProfileById(id: UUID, all: Boolean = false): Optional<Profile> {
        if (all) {
            return profileRepository.findById(id)
        }
        return profileRepository.findByIdAndDeletedAtIsNull(id)
    }

    fun getProfileByUsernameOrEmail(username: String, all: Boolean = false): Optional<Profile> {
        val res = profileRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(username, username)
        // TODO FIX
        for (item in res) {
            if (all || (item.deletedAt == null)) {
                return Optional.of(item)
            }
        }
        return Optional.empty()
    }

    fun getProfileReferenceById(id: UUID): Array<Array<String>>? {
        val _profile = profileRepository.findByIdAndDeletedAtIsNull(id)
        if (_profile.isPresent) {
            var res: Array<Array<String>> = arrayOf()
            res = res.plus(arrayOf(_profile.get().getId().toString()))
            res = res.plus(arrayOf(_profile.get().getFio()))
            return res
        }
        return null
    }

    fun getAclIds(profileId: UUID): Array<UUID> {
        var result: Array<UUID> = arrayOf(UUID.fromString("00000000-0000-0000-0000-000000000000"))
        val profile = profileRepository.findByIdAndDeletedAtIsNull(profileId)
        if (profile.isPresent) {
            result = result.plus(profileId)
            val structure = structureRepository.findAllByProfileIdAndDeletedAtIsNull(profileId)
            for (item in structure) {
                result = result.plus(item.getPath())
            }

            val roles = rolesGroupService.getRolesByProfileMap(profileId)
            for (item in roles) {
                if (
                    item.containsKey("id")
                    && item["id"] is UUID
                    && !result.contains(UUID.fromString(item["id"].toString()))
                ) {
                    result = result.plus(UUID.fromString(item["id"].toString()))
                }
            }
        }
        return result
    }

    fun setProfileRef(reference: Reference, record: RefRecord, newRecordData: MutableMap<String, Any?>): RefRecord {
        val data = record.getDataField()
        val fioPrepare: ArrayList<String> = arrayListOf(data["last_name"].toString(), data["first_name"].toString())
        if (data["middle_name"] != null) {
            fioPrepare.add(data["middle_name"].toString())
        }

        if (newRecordData.containsKey("roles")) {
            rolesGroupService.setMembers(
                newRecordData["roles"] as ArrayList<MutableMap<String, Any?>>,
                record.getRecordId()!!,
                fioPrepare.joinToString(" ")
            )
        }

        record.setDataField(mutableMapOf("fio" to fioPrepare.joinToString(" ").trim()))
        return record
    }

    fun accessStudentList(profile: Profile, filter: MutableMap<String, String> = mutableMapOf()): Any? {
        // Список студентов
        val result: ArrayList<MutableMap<String, Any?>> = arrayListOf()
        val roleG = roleGroupRepository.findByIdAndDeletedAtIsNull(UUID.fromString("00000000-0000-0000-0000-000000001017"))
        if (roleG.isPresent) {
            val members = roleG.get().getMembers()
            var membersIds: Array<Any?> = arrayOf()
            if (filter.containsKey("mt") && !filter["mt"].isNullOrBlank()) {
                for (item in filter["mt"].toString().split(",")) {
                    if (item.isNotEmpty()) {
                        val strIds = getIdsByMentor(UUID.fromString(item)!!)
                        if (strIds.isNotEmpty()) {
                            val symbolS = Array(strIds.size) { "?" }
                            result.add(mutableMapOf(
                                "condition" to " AND profiles.id::character varying IN (${symbolS.joinToString(",")})",
                                "value" to strIds
                            ))
                        } else {
                            result.add(mutableMapOf(
                                "condition" to " AND 1 = 0",
                                "value" to arrayOf<String>()
                            ))
                        }
                    }
                }
            }
            for (item in members) {
                if (item.containsKey("id")) {
                    membersIds = membersIds.plus(item["id"].toString())
                }
            }

            if (membersIds.isNotEmpty()) {
                val symbolQ = Array(membersIds.size) { "?" }
                result.add(mutableMapOf(
                    "condition" to " AND profiles.id::character varying IN (${symbolQ.joinToString(",")})",
                    "value" to membersIds
                ))
            } else {
                result.add(mutableMapOf(
                    "condition" to " AND 1 = 0",
                    "value" to arrayOf<String>()
                ))
                return result
            }

            val rolesProfile = rolesGroupService.getRolesByProfileMap(profile.getId()!!, arrayOf("key"))
            val roles: ArrayList<String> = arrayListOf()
            for (item in rolesProfile) {
                roles.add(item["key"].toString().uppercase())
            }

            if (!roles.contains("ADMIN")) {
                if (roles.contains("MENTOR")) {
                    val strIds = getIdsByMentor(profile.getId()!!)
                    if (strIds.isNotEmpty()) {
                        val symbolS = Array(strIds.size) { "?" }
                        result.add(mutableMapOf(
                            "condition" to " AND profiles.id::character varying IN (${symbolS.joinToString(",")})",
                            "value" to strIds
                        ))
                    }
                } else if (roles.contains("STUDENT")) {
                    return false
                }
            }
        }
        return result
        // Список студентов
    }

    fun getIdsByMentor(id: UUID): Array<String> {
        val structures = structureRepository.getListByManager(id.toString())
        var strIds: Array<String> = arrayOf()
        for (structure in structures) {
            val strM = structureRepository.getListByPath(structure.getId()!!)
            for (str in strM) {
                if (str.getType() == "profile") {
                    strIds = strIds.plus(str.getProfileId().toString())
                }
            }
        }
        return strIds
    }


    fun isAdmin(profileId: UUID): Boolean {
        val roles: List<String> = arrayListOf("admin")
        val user = profileRepository.findByIdAndDeletedAtIsNull(profileId)
        if (
            user.isPresent
            && user.get().getEnabled()!!
            && user.get().getIsBlocked() == null
        ) {
            val rolesProfile = rolesGroupService.getRolesByProfileMap(user.get().getId()!!, arrayOf("key"))
            val result = rolesProfile.filter { p -> roles.any { it.lowercase() == p["key"].toString().lowercase() } }
            if (result.isNotEmpty()) {
                return true
            }
        }

        return false
    }

    fun setUserRating(newRating: Int) {}

    companion object {
        @JvmStatic
        private val USERNAME_REGEX = "^[a-zA-Z]([._-](?![._-])|[a-zA-Z0-9]){4,}[a-zA-Z0-9]$"

        @JvmStatic
        private val PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+\$).{7,}\$"


        fun isUsernameValid(username: String?): Boolean {
//            println(username)
//            println(username!!.length)
            val pattern: Pattern = Pattern.compile(USERNAME_REGEX)
            val matcher: Matcher = pattern.matcher(username)
            return matcher.matches()
        }

        fun isEmailValid(email: String): Boolean {
            return Pattern.compile(
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
                        + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
                        + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
            ).matcher(email).matches()
//            email: new RegExp('(?:[a-z0-9!#$%&\'*+=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&\'*+=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\\])
        }

        fun isPasswordValid(password: String): Boolean {
            return PASSWORD_REGEX.toRegex().matches(password)
        }
    }
}
