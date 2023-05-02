package com.mdsp.backend.app.structure.service

import com.mdsp.backend.app.profile.repository.IProfileRepository
import com.mdsp.backend.app.structure.model.Structure
import com.mdsp.backend.app.structure.repository.IStructureRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.util.*
import kotlin.collections.ArrayList

@Service
class StructureService {

    @Autowired
    private lateinit var structureRepository: IStructureRepository

    @Autowired
    private lateinit var profileRepository: IProfileRepository


    fun getPath(structure: Structure): Array<UUID> {
        if (structure.getParentId() == null) {
            return arrayOf(structure.getId()!!)
        }
        val parentStructure = structureRepository.findByIdAndDeletedAtIsNull(structure.getParentId()!!)
        if (parentStructure.isPresent) {
            return arrayOf(structure.getId()!!, *getPath(parentStructure.get()))
        }
        return arrayOf(structure.getId()!!)
    }

    fun getProfilesStructure(structureId: UUID? = null): Array<UUID> {
        var profileIds: Array<UUID> = arrayOf()
        val children = structureRepository.findAllByParentIdAndDeletedAtIsNullOrderBySortOrderAsc(structureId)
        for (child in children) {
            if (child.getType() == "profile") {
                profileIds = profileIds.plus(child.getProfileId()!!)
            }
            profileIds = profileIds.plus(getProfilesStructure(child.getId()))
        }
        return profileIds
    }

    fun getSubdivisionByProfileAsMap(
            profileName: String,
            map: MutableMap<String, String>? = mutableMapOf()
    ): MutableMap<String, String>? {
        val profileOptional = profileRepository.findByUsernameAndDeletedAtIsNull(profileName)
        if(profileOptional.isPresent){
            val profileId = profileOptional.get().getId()
            val profileStructure = structureRepository.findAllByProfileIdAndDeletedAtIsNull(profileId!!)
            if(profileStructure.isNotEmpty()){
                val result = arrayListOf<UUID>()
                for (profile in profileStructure) {
                    result.addAll(getProfilesStructure(profile.getParentId()))
                }
//                if(!map!!.containsKey("mt")) map["mt"] = ""
                map!!["std"] = result.toSet().joinToString(",")
            }
        }
        return map
    }

    fun getSubdivisionByProfile(profileId: UUID): Optional<Structure> {
        val structures = structureRepository.findAllByProfileIdAndDeletedAtIsNull(profileId)
        for (structure in structures) {
            if (structure.getParentId() != null) {
                val parentStructure = structureRepository.findByIdAndDeletedAtIsNull(structure.getParentId()!!)
                if (parentStructure.isPresent && parentStructure.get().getType() == "subdivision") {
                    return parentStructure
                }
            }
        }
        return Optional.empty()
    }

    fun getSubdivisionsByProfile(profileId: UUID): ArrayList<Structure> {
        val structures = structureRepository.findAllByProfileIdAndDeletedAtIsNull(profileId)
        val result: ArrayList<Structure> = arrayListOf()
        for (structure in structures) {
            if (structure.getParentId() != null && structure.getType() == "profile") {
                val parentStructure = structureRepository.findByIdAndDeletedAtIsNull(structure.getParentId()!!)
                if (parentStructure.isPresent && parentStructure.get().getType() == "subdivision") {
                    result.add(parentStructure.get())
                }
            }
        }
        return result
    }

    fun getManagerSubdivision(profileId: UUID): ArrayList<Structure> {
        return structureRepository.getListByManager(profileId.toString())
    }

    fun isRecursive(current: UUID, parent: UUID): Boolean {
        var forParent = structureRepository.findByIdAndDeletedAtIsNull(parent).get()
        while (forParent.getParentId() != current) {
            if (forParent.getParentId() == null) return false
            forParent = structureRepository.findByIdAndDeletedAtIsNull(forParent.getParentId()!!).get()
        }
        return true
    }

    fun deleteRecursive(structures: ArrayList<Structure>) {
        for (item in structures) {
            item.deletedAt = (Timestamp(System.currentTimeMillis()))
            structureRepository.save(item)
            val structuresParent = structureRepository.findAllByParentIdAndDeletedAtIsNullOrderBySortOrderAsc(item.getId()!!)
            if (structuresParent.isNotEmpty()) {
                deleteRecursive(structuresParent)
            }
        }
    }

}
