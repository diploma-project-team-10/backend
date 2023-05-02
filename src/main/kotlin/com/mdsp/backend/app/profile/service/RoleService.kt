package com.mdsp.backend.app.profile.service

import com.mdsp.backend.app.profile.model.Profile
import com.mdsp.backend.app.profile.repository.IProfileRepository
import com.mdsp.backend.app.reference.model.RefRecord
import com.mdsp.backend.app.reference.model.Reference
import com.mdsp.backend.app.reference.repository.IReferenceRepository
import com.mdsp.backend.app.structure.repository.IStructureRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class RoleService {

    @Autowired
    lateinit var profileRepository: IProfileRepository

    @Autowired
    lateinit var structureRepository: IStructureRepository

    fun getProfileById(id: UUID, all: Boolean = false): Optional<Profile> {
        if (all) {
            return profileRepository.findById(id)
        }
        return profileRepository.findByIdAndDeletedAtIsNull(id)
    }

    fun setRoleRef(reference: Reference, record: RefRecord): RefRecord {
        val data = record.getDataField()
        return record
    }
}
