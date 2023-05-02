package com.mdsp.backend.app.structure.service

import com.mdsp.backend.app.profile.repository.IProfileRepository
import com.mdsp.backend.app.reference.model.RefRecord
import com.mdsp.backend.app.reference.repository.IReferenceRepository
import com.mdsp.backend.app.structure.model.RolesGroup
import com.mdsp.backend.app.structure.model.Structure
import com.mdsp.backend.app.structure.repository.IRoleGroupRepository
import com.mdsp.backend.app.structure.repository.IStructureRepository
import com.mdsp.backend.app.system.config.DataSourceConfiguration
import com.mdsp.backend.app.system.model.GridQuery
import com.mdsp.backend.app.system.model.Util
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class RolesGroupService {

    @Autowired
    private lateinit var roleGroupRepository: IRoleGroupRepository

    @Autowired
    lateinit var profileRepository: IProfileRepository

    @Autowired
    lateinit var referenceRepository: IReferenceRepository

    @Autowired
    lateinit var dataSourceConfig: DataSourceConfiguration

    fun setMembers(dataRoles: ArrayList<MutableMap<String, Any?>>, profileId: UUID, fio: String) {
        val roles = dataRoles
        val referenceRoles = referenceRepository.findByIdAndDeletedAtIsNull(UUID.fromString("00000000-0000-0000-0000-000000000019"))
        if (referenceRoles.isPresent) {

            var rolesId = arrayOf<String>()
            for (role in roles) {
                var recRoles = RefRecord(UUID.fromString(role["id"].toString()), referenceRoles.get(), dataSourceConfig)
                if (recRoles.isExisted()) {
                    recRoles.load()
                    if (recRoles.isDeleted()) {
                        continue
                    }
                    var members: ArrayList<MutableMap<String, Any?>> = arrayListOf()
                    if (recRoles.getDataField()["members"] != null) {
                        members = recRoles.getDataField()["members"] as ArrayList<MutableMap<String, Any?>>
                    }
                    if (members.none { p -> profileId.toString() == p["id"].toString() }) {
                        members.add(mutableMapOf(
                            "id" to profileId,
                            "value" to fio.trim()
                        ))
                        recRoles.setDataField(mutableMapOf("members" to members))
                        if (recRoles.isValid()) {
                            recRoles.save()
                        }
                    }
                    rolesId = rolesId.plus(recRoles.getRecordId()!!.toString())
                }
            }

            //Remove Roles
            if (rolesId.isNotEmpty() || roles.isEmpty()) {
                var grid = GridQuery(referenceRoles.get().getTableName(), dataSourceConfig)
                var allField = Util.mergeMutableMap(Util.toJson(referenceRoles.get().getUserFields()), Util.toJson(referenceRoles.get().getSysFields()))
                grid.setRefFields(allField)

                val symbolQ = Array(rolesId.size) { "?" }
                grid.addCondition(
                    "deleted_at IS NULL AND db_array_key_exists(?, \"members\") = 1",
                    arrayOf(profileId.toString())
                )
                if (rolesId.isNotEmpty()) {
                    grid.addCondition(
                        " AND lower(id::character varying) NOT IN (${
                            symbolQ.joinToString(",")
                        })",
                        arrayOf(*rolesId)
                    )
                }
                grid.addColumn("display_name AS value")
                val removingRoles = grid.getDataPage()
                for (removeId in removingRoles) {
                    var recRoles =
                        RefRecord(UUID.fromString(removeId["id"].toString()), referenceRoles.get(), dataSourceConfig)
                    if (recRoles.isExisted()) {
                        recRoles.load()
                        if (recRoles.isDeleted()) {
                            continue
                        }
                        var members = recRoles.getDataField()["members"] as ArrayList<MutableMap<String, Any?>>
                        members =
                            members.filter { p -> profileId.toString() != p["id"].toString() } as ArrayList<MutableMap<String, Any?>>
                        recRoles.setDataField(mutableMapOf("members" to members))
                        if (recRoles.isValid()) {
                            recRoles.save()
                        }
                    }
                }
            }
        }
    }

    fun getRolesByProfileMap(profileId: UUID, fields: Array<String> = arrayOf()): MutableList<MutableMap<String, Any?>> {
        val referenceRoles = referenceRepository.findByIdAndDeletedAtIsNull(UUID.fromString("00000000-0000-0000-0000-000000000019"))
        if (referenceRoles.isPresent) {
            val grid = GridQuery(referenceRoles.get().getTableName(), dataSourceConfig)
            val allField = Util.mergeMutableMap(Util.toJson(referenceRoles.get().getUserFields()), Util.toJson(referenceRoles.get().getSysFields()))
            grid.setRefFields(allField)
            grid.addCondition(
                "deleted_at IS NULL AND db_array_key_exists(?, \"members\") = 1",
                arrayOf(profileId.toString())
            )
            grid.addColumn("display_name AS value")
            for (column in fields) {
                grid.addColumn(column)
            }
            return grid.getDataPage()
        }
        return mutableListOf()
    }

    fun getRolesByProfileString(profileId: UUID): String {
        val mRes = getRolesByProfileMap(profileId)
        val result = arrayListOf<String>()
        for (item in mRes) {
            if (item.containsKey("value")) {
                result.add(item["value"].toString())
            }
        }
        return result.joinToString(", ")
    }

    fun getRolesByProfile(profileId: UUID): ArrayList<String> {
        val mRes = getRolesByProfileMap(profileId, arrayOf("key"))
        val result = arrayListOf<String>()
        for (item in mRes) {
            if (item.containsKey("key")) {
                result.add(item["key"].toString().uppercase())
            }
        }
        return result
    }

    fun getRolesUUIDByProfile(profileId: UUID): ArrayList<String> {
        val mRes = getRolesByProfileMap(profileId, arrayOf("id"))
        val result = arrayListOf<String>()
        for (item in mRes) {
            if (item.containsKey("id")) {
                result.add(item["id"].toString())
            }
        }
        return result
    }

    fun getChildRolesByProfileMap(profileId: UUID): MutableList<MutableMap<String, Any?>> {
        val mRes = getRolesByProfileMap(profileId, arrayOf("id"))
        val result = mutableListOf<MutableMap<String, Any?>>()
        for (item in mRes) {
            if (item.containsKey("id") && item["id"] != null) {
                val role = roleGroupRepository.findByIdAndDeletedAtIsNull(item["id"]!! as UUID)
                if (role.isPresent) {
                    for ( childRole in role.get().getChildRoles()) {
                        if (!result.contains(childRole)) {
                            result.add(childRole)
                        }
                    }
                }
            }
        }
        return result
    }

    fun getChildRolesUUIDByProfile(profileId: UUID): ArrayList<String> {
        val mRes = getChildRolesByProfileMap(profileId)
        val result = arrayListOf<String>()
        for (item in mRes) {
            if (item.containsKey("id") && item["id"] != null) {
                result.add(item["id"].toString())
            }
        }
        return result
    }

    fun getGroupByRole(key: String): Optional<RolesGroup> {
        val groups =  roleGroupRepository.findAllByKeyAndDeletedAtIsNull(key)
        if (groups.isNotEmpty()) {
            return Optional.of(groups.first())
        }
        return Optional.empty()
    }

}
