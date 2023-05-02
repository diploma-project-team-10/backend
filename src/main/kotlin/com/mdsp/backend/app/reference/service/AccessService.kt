package com.mdsp.backend.app.reference.service

import com.mdsp.backend.app.profile.model.Profile
import com.mdsp.backend.app.profile.repository.IProfileRepository
import com.mdsp.backend.app.profile.service.ProfileService
import com.mdsp.backend.app.reference.model.Reference
import com.mdsp.backend.app.reference.repository.IReferenceAccessRepository
import com.mdsp.backend.app.reference.repository.IReferenceRepository
import com.mdsp.backend.app.structure.repository.IRoleGroupRepository
import com.mdsp.backend.app.structure.repository.IStructureRepository
import com.mdsp.backend.app.structure.service.RolesGroupService
import com.mdsp.backend.app.structure.service.StructureService
import com.mdsp.backend.app.system.config.DataSourceConfiguration
import com.mdsp.backend.app.system.service.Acl.Acl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import kotlin.collections.ArrayList

@Service
class AccessService {

    @Autowired
    private lateinit var dataSourceConfig: DataSourceConfiguration

    @Autowired
    private lateinit var referenceRepository: IReferenceRepository

    @Autowired
    private lateinit var accessRepository: IReferenceAccessRepository

    @Autowired
    private lateinit var profileRepository: IProfileRepository

    @Autowired
    private lateinit var rolesGroupService: RolesGroupService

    @Autowired
    private lateinit var roleGroupRepository: IRoleGroupRepository

    @Autowired
    private lateinit var structureRepository: IStructureRepository

    @Autowired
    private lateinit var profileService: ProfileService

    @Autowired
    private lateinit var structureService: StructureService

    private val PROFILE_REF: UUID = UUID.fromString("00000000-0000-0000-0000-000000000017")

    fun mayView(profileId: UUID, reference: Reference): Boolean {
        if (profileService.isAdmin(profileId)) {
            return true
        }
        val accessList = accessRepository.findAllByReferenceIdAndDeletedAtIsNull(reference.getId()!!)
        for (access in accessList) {
            if (
                (access.mayView || access.mayAdd)
                && Acl.hasAccess(arrayOf(access.subjectId!!), profileService.getAclIds(profileId)).isNotEmpty()
            ) {
                return true
            }
        }

        return false
    }

    fun mayView(profileId: UUID, reference: Reference, owner: UUID): Boolean {
        if (profileService.isAdmin(profileId)) {
            return true
        }
        val ids = mayViewIds(profileId, reference)

        return Acl.hasAccess(ids, arrayOf(owner, UUID.fromString("00000000-0000-0000-0000-000000000000"))).isNotEmpty()
    }

    fun mayViewIds(profileId: UUID, reference: Reference): Array<UUID> {
        val empty: Array<Any?> = arrayOf()
        if (profileService.isAdmin(profileId)) {
            return arrayOf(UUID.fromString("00000000-0000-0000-0000-000000000000"))
        }
        var result: Array<UUID> = arrayOf()
        val accessList = accessRepository.findAllByReferenceIdAndDeletedAtIsNull(reference.getId()!!)
        for (access in accessList) {
            if (
                (access.mayView || access.mayAdd)
                && Acl.hasAccess(arrayOf(access.subjectId!!), profileService.getAclIds(profileId)).isNotEmpty()
            ) {
                result = result.plus(profileId)
                for (obj in access.objects) {
                    if (
                        (obj.mayView || obj.mayEdit || obj.mayDelete)
                        && obj.id != null
                    ) {
                        if (obj.id == UUID.fromString("00000000-0000-0000-0000-000000000000")) {
                            return arrayOf(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                        }
                        val profileCandidate = profileRepository.findByIdAndDeletedAtIsNull(UUID.fromString(obj.id.toString()))
                        if (profileCandidate.isPresent) {
                            result = result.plus(UUID.fromString(obj.id.toString()))
                            continue
                        }

                        val roles = roleGroupRepository.findByIdAndDeletedAtIsNull(obj.id!!)
                        if (roles.isPresent) {
                            for (member in roles.get().getMembers()) {
                                if (member.containsKey("id")) {
                                    val structure = structureRepository.findByIdAndDeletedAtIsNull(UUID.fromString(member["id"].toString()))
                                    if (structure.isPresent) {
                                        for (pId in structureService.getProfilesStructure(structure.get().getId())) {
                                            result = result.plus(pId)
                                        }
                                    } else {
                                        result = result.plus(UUID.fromString(member["id"].toString()))
                                    }
                                }
                            }
                            continue
                        }

                        val structure = structureRepository.findByIdAndDeletedAtIsNull(obj.id!!)
                        if (structure.isPresent) {
                            for (pId in structureService.getProfilesStructure(structure.get().getId())) {
                                result = result.plus(pId)
                            }
                            continue
                        }
                    }
                }
            }
        }

        return result
    }

    fun getAccess(profileId: UUID, reference: Reference): MutableMap<String, Any?> {
        val empty: Array<Any?> = arrayOf()
        if (profileService.isAdmin(profileId)) {
            return mutableMapOf("condition" to "1 = 1", "value" to empty)
        }
        val result: ArrayList<MutableMap<String, Any?>> = arrayListOf()
        val accessList = accessRepository.findAllByReferenceIdAndDeletedAtIsNull(reference.getId()!!)
        var values: Array<Any?> = arrayOf()
        for (access in accessList) {
            if (
                (access.mayView || access.mayAdd)
                && Acl.hasAccess(arrayOf(access.subjectId!!), profileService.getAclIds(profileId)).isNotEmpty()
            ) {
                values = values.plus(profileId)
                for (obj in access.objects) {
                    if (
                        (obj.mayView || obj.mayEdit || obj.mayDelete)
                        && obj.id != null
                    ) {
                        if (obj.id == UUID.fromString("00000000-0000-0000-0000-000000000000")) {
                            return mutableMapOf("condition" to "1 = 1", "value" to empty)
                        }

                        val profileCandidate = profileRepository.findByIdAndDeletedAtIsNull(UUID.fromString(obj.id.toString()))
                        if (profileCandidate.isPresent) {
                            values = values.plus(obj.id)
                            continue
                        }

                        val roles = roleGroupRepository.findByIdAndDeletedAtIsNull(obj.id!!)
                        if (roles.isPresent) {
                            for (member in roles.get().getMembers()) {
                                if (member.containsKey("id")) {
                                    val structure = structureRepository.findByIdAndDeletedAtIsNull(UUID.fromString(member["id"].toString()))
                                    if (structure.isPresent) {
                                        for (pId in structureService.getProfilesStructure(structure.get().getId())) {
                                            values = values.plus(pId)
                                        }
                                    } else {
                                        values = values.plus(UUID.fromString(member["id"].toString()))
                                    }
                                }
                            }
                            continue
                        }

                        val structure = structureRepository.findByIdAndDeletedAtIsNull(obj.id!!)
                        if (structure.isPresent) {
                            for (pId in structureService.getProfilesStructure(structure.get().getId())) {
                                values = values.plus(pId)
                            }
                            continue
                        }

                    }
                }
            }
        }
        if (values.isNotEmpty()) {
            val symbolQ = Array(values.size) { "?" }
            return mutableMapOf(
                "condition" to "${reference.getTableName()}.owner IN (${symbolQ.joinToString(",")})",
                "value" to values
            )
        }

        return mutableMapOf("condition" to "1 = 0", "value" to empty)
    }

    fun getEditIds(profileId: UUID, reference: Reference): Array<UUID> {
        if (profileService.isAdmin(profileId)) {
            return arrayOf(UUID.fromString("00000000-0000-0000-0000-000000000000"))
        }
        var result: Array<UUID> = arrayOf()

        val accessList = accessRepository.findAllByReferenceIdAndDeletedAtIsNull(reference.getId()!!)
        for (access in accessList) {
            if (
                (access.mayView || access.mayAdd)
                && Acl.hasAccess(arrayOf(access.subjectId!!), profileService.getAclIds(profileId)).isNotEmpty()
            ) {
                result = result.plus(profileId)

                for (obj in access.objects) {
                    if (obj.mayEdit && obj.id != null) {
                        if (obj.id == UUID.fromString("00000000-0000-0000-0000-000000000000")) {
                            return arrayOf(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                        }

                        val profileCandidate = profileRepository.findByIdAndDeletedAtIsNull(UUID.fromString(obj.id.toString()))
                        if (profileCandidate.isPresent) {
                            result = result.plus(UUID.fromString(obj.id.toString()))
                            continue
                        }

                        val roles = roleGroupRepository.findByIdAndDeletedAtIsNull(obj.id!!)
                        if (roles.isPresent) {
                            for (member in roles.get().getMembers()) {
                                if (member.containsKey("id")) {
                                    val structure = structureRepository.findByIdAndDeletedAtIsNull(UUID.fromString(member["id"].toString()))
                                    if (structure.isPresent) {
                                        for (pId in structureService.getProfilesStructure(structure.get().getId())) {
                                            result = result.plus(pId)
                                        }
                                    } else {
                                        result = result.plus(UUID.fromString(member["id"].toString()))
                                    }
                                }
                            }
                            continue
                        }

                        val structure = structureRepository.findByIdAndDeletedAtIsNull(obj.id!!)
                        if (structure.isPresent) {
                            for (pId in structureService.getProfilesStructure(structure.get().getId())) {
                                result = result.plus(pId)
                            }
                            continue
                        }
                    }
                }
            }
        }

        return result
    }

    fun mayEdit(profileId: UUID, reference: Reference, owner: UUID, forEditing: ArrayList<UUID> = arrayListOf()): Boolean {
        if (profileService.isAdmin(profileId)) {
            return true
        }
         if (reference.getId()!! == PROFILE_REF) {
            val ids = editAndDeleteIdsByRoleInProfile(profileId, forEditing)
             forEditing.addAll(arrayListOf(owner, UUID.fromString("00000000-0000-0000-0000-000000000000")))
             return Acl.hasAccess(ids, forEditing.toTypedArray()).isNotEmpty()
        }

        val ids = getEditIds(profileId, reference)

        return Acl.hasAccess(ids, arrayOf(owner, UUID.fromString("00000000-0000-0000-0000-000000000000"))).isNotEmpty()
    }

    fun getDeleteIds(profileId: UUID, reference: Reference): Array<UUID> {
        if (profileService.isAdmin(profileId)) {
            return arrayOf(UUID.fromString("00000000-0000-0000-0000-000000000000"))
        }
        var result: Array<UUID> = arrayOf()
        val accessList = accessRepository.findAllByReferenceIdAndDeletedAtIsNull(reference.getId()!!)
        for (access in accessList) {
            if (
                (access.mayView || access.mayAdd)
                && Acl.hasAccess(arrayOf(access.subjectId!!), profileService.getAclIds(profileId)).isNotEmpty()
            ) {
                result = result.plus(profileId)
                for (obj in access.objects) {
                    if (obj.mayDelete && obj.id != null) {
                        if (obj.id == UUID.fromString("00000000-0000-0000-0000-000000000000")) {
                            return arrayOf(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                        }
                        val profileCandidate = profileRepository.findByIdAndDeletedAtIsNull(UUID.fromString(obj.id.toString()))
                        if (profileCandidate.isPresent) {
                            result = result.plus(UUID.fromString(obj.id.toString()))
                            continue
                        }

                        val roles = roleGroupRepository.findByIdAndDeletedAtIsNull(obj.id!!)
                        if (roles.isPresent) {
                            for (member in roles.get().getMembers()) {
                                if (member.containsKey("id")) {
                                    val structure = structureRepository.findByIdAndDeletedAtIsNull(UUID.fromString(member["id"].toString()))
                                    if (structure.isPresent) {
                                        for (pId in structureService.getProfilesStructure(structure.get().getId())) {
                                            result = result.plus(pId)
                                        }
                                    } else {
                                        result = result.plus(UUID.fromString(member["id"].toString()))
                                    }
                                }
                            }
                            continue
                        }

                        val structure = structureRepository.findByIdAndDeletedAtIsNull(obj.id!!)
                        if (structure.isPresent) {
                            for (pId in structureService.getProfilesStructure(structure.get().getId())) {
                                result = result.plus(pId)
                            }
                            continue
                        }
                    }
                }
            }
        }

        return result
    }

    fun editAndDeleteIdsByRoleInProfile(profileId: UUID, forEditing: ArrayList<UUID>, type: String = "edit"): Array<UUID> {
        var result: Array<UUID> = arrayOf()
        val roles = rolesGroupService.getRolesByProfile(profileId)
        for (role in roles) {
            if (!result.contains(profileId) && role != "STUDENT")
                result = result.plus(profileId)
            when (role) {
                "CHIEF" -> {
                    var profiles :ArrayList<Profile> = arrayListOf()
                    for (edit in forEditing) {
                        val profile = profileRepository.findByIdAndDeletedAtIsNull(edit);
                        if (profile.isPresent) {
                            profiles.add(profile.get())
                        }
                    }
                    if (profiles.isEmpty()){
                        profiles = profileRepository.findAllByDeletedAtIsNull()
                    }
                    for (profile in profiles) {
                        if (profile.getId() != null && canAccessByRole(profileId, profile.getId()!!)) {
                            result = result.plus(profile.getId()!!)
                        }
                    }
                    break
                }
                "HEAD_CITY" -> {
                    val current = profileRepository.findByIdAndDeletedAtIsNull(profileId)
                    if (current.isPresent) {
                        val profiles :ArrayList<Profile> = arrayListOf()
                        for (city in current.get().getCity()) {
                            for (edit in forEditing) {
                                val profile = profileRepository.findByIdAndCityDeletedAtIsNull((city["id"] as String),  edit);
                                if (profile.isPresent) {
                                    profiles.add(profile.get())
                                }
                            }
                        }
                        if (profiles.isEmpty()){
                            for (city in current.get().getCity()) {
                                profiles.addAll(profileRepository.findAllByCityDeletedAtIsNull(city["id"] as String))
                            }
                        }

                        for (profile in profiles) {
                            if (profile.getId() != null && canAccessByRole(profileId, profile.getId()!!)) {
                                result = result.plus(profile.getId()!!)
                            }
                        }
                    }
                }
                "MENTOR" -> {
                    if (type == "edit"){
                        for (id in forEditing) {
                            for (structure in structureService.getSubdivisionsByProfile(id)) {
                                if (
                                    structure.getManager().isNotEmpty()
                                    && structure.getManager().first().containsKey("value")
                                    && structure.getManager().first().containsKey("id")
                                ) {
                                    if (profileId == UUID.fromString(structure.getManager().first()["id"] as String)
                                        && canAccessByRole(profileId, id))
                                        result = result.plus(id)
                                }
                            }
                        }
                    }
                }
            }
        }
        return result
    }

    fun mayDelete(profileId: UUID, reference: Reference, owner: UUID, forEditing: ArrayList<UUID> = arrayListOf()): Boolean {
        if (profileService.isAdmin(profileId)) {
            return true
        }
        if (reference.getId()!! == PROFILE_REF) {
            val ids = editAndDeleteIdsByRoleInProfile(profileId, forEditing,"delete")
            forEditing.addAll(arrayListOf(owner, UUID.fromString("00000000-0000-0000-0000-000000000000")))
            return Acl.hasAccess(ids, forEditing.toTypedArray()).isNotEmpty()
        }

        val ids = getDeleteIds(profileId, reference)

        return Acl.hasAccess(ids, arrayOf(owner, UUID.fromString("00000000-0000-0000-0000-000000000000"))).isNotEmpty()
    }

    fun mayAdd(profileId: UUID, reference: Reference): Boolean {
        if (profileService.isAdmin(profileId)) {
            return true
        }
        val accessList = accessRepository.findAllByReferenceIdAndDeletedAtIsNull(reference.getId()!!)
        for (access in accessList) {
            if (
                access.mayAdd
                && Acl.hasAccess(arrayOf(access.subjectId!!), profileService.getAclIds(profileId)).isNotEmpty()
            ) {
                return true
            }
        }

        return false
    }

    fun canAccessByRole(firstProfile: UUID, secondProfile: UUID): Boolean {
        val firstChildRoles = rolesGroupService.getChildRolesUUIDByProfile(firstProfile)
        val secondRoles = rolesGroupService.getRolesUUIDByProfile(secondProfile)
        return firstChildRoles.containsAll(secondRoles)
    }

    companion object {

    }
}
