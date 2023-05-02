package com.mdsp.backend.app.reference.service

import com.mdsp.backend.app.profile.service.ProfileService
import com.mdsp.backend.app.reference.repository.IReferenceRepository
import com.mdsp.backend.app.reference.repository.ISectionRepository
import com.mdsp.backend.app.structure.service.RolesGroupService
import com.mdsp.backend.app.system.config.DataSourceConfiguration
import com.mdsp.backend.app.system.config.FeignUserConfig
import com.mdsp.backend.app.system.model.EngineQuery
import com.mdsp.backend.app.system.model.GridJoinQuery
import com.mdsp.backend.app.system.model.Util
import com.mdsp.backend.app.system.service.Acl.Acl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

@Service
class SectionService {

    @Autowired
    lateinit var sectionRepository: ISectionRepository

    @Autowired
    lateinit var referenceRepository: IReferenceRepository

    @Autowired
    lateinit var profileService: ProfileService

    @Autowired
    lateinit var recordNoteService: RecordNoteService

    @Autowired
    private lateinit var feignConfig: FeignUserConfig

    @Autowired
    lateinit var referenceNoteService: ReferenceNoteService

    @Autowired
    lateinit var dataSourceConfig: DataSourceConfiguration

    @Autowired
    lateinit var rolesGroupService: RolesGroupService

    @Autowired
    lateinit var accessService: AccessService

    private val POST_NEWS: UUID = UUID.fromString("e7e8fb19-d843-425b-b3dd-f1474cd8340f")
    private val POST_STORIES: UUID = UUID.fromString("3c10fa1e-c6aa-44e5-a3ce-465fab2d9b00")
    private val POST_CATEGORY: UUID = UUID.fromString("20a4ccd9-6300-4462-acb6-6942bf4fe5d7")


    fun getRefRecordByPage(
        referenceId: UUID,
        sectionId: UUID,
        page: Int = 1,
        size: Int = 20,
        fields: String? = "",
        filter: MutableMap<String, String>? = mutableMapOf(),
        headerEnable: Boolean = false,
        searchValue: String? = "",
        authentication: Authentication
    ): Page<MutableMap<String, Any?>> {
        val pagePR: PageRequest = PageRequest.of(page - 1, size)
        val pagePRDefault: PageRequest = PageRequest.of(0, Int.MAX_VALUE)
        val reference = referenceRepository.findByIdAndDeletedAtIsNull(referenceId)
        val headerTable: MutableList<MutableMap<String, Any?>> = mutableListOf()

        var totalRows: Long = 0
        var result: MutableList<MutableMap<String, Any?>> = mutableListOf()

        if (reference.isPresent) {
            val tableName = reference.get().getTableName()
            val section = sectionRepository.findByIdAndDeletedAtIsNull(sectionId)
            if (!section.isPresent) {
                return PageImpl<MutableMap<String, Any?>>(result, pagePR, 0)
            }
            val profile = profileService.getProfileByUsernameOrEmail(authentication.name)
            if (!profile.isPresent) {
                return PageImpl<MutableMap<String, Any?>>(result, pagePR, 0)
            }
            val aclIds = profileService.getAclIds(profile.get().getId()!!)
            if (Acl.hasAccess(section.get().getAccess(), aclIds).isEmpty() && section.get().getAccess().isNotEmpty()) {
                return PageImpl<MutableMap<String, Any?>>(result, pagePR, 0)
            }

            val grid = GridJoinQuery(tableName, dataSourceConfig)

            val allField = Util.mergeMutableMap(
                Util.toJson(reference.get().getUserFields()),
                Util.toJson(reference.get().getSysFields())
            )

            grid.setRefFields(allField)

            val filters = section.get().getFilterField() as ArrayList<ArrayList<MutableMap<String, Any?>>>

            val conditionFilter = SectionService.prepareCondition(filters, allField, "$tableName.")

            grid.setPageRequest(pagePRDefault)

            grid.addCondition("$tableName.deleted_at IS NULL", arrayOf())

            val customFilter = conditionFilter["condition"].toString()
            if (customFilter.isNotEmpty()) {
                grid.addCondition(
                    " AND (${conditionFilter["condition"].toString()})",
                    conditionFilter["value"] as Array<Any?>
                )
            }

            if (!accessService.mayView(profile.get().getId()!!, reference.get())) {
                return PageImpl<MutableMap<String, Any?>>(result, pagePR, 0)
            } else {
                val accessSql = accessService.getAccess(profile.get().getId()!!, reference.get())
                if (accessSql.containsKey("condition") && accessSql.containsKey("value")) {
                    grid.addCondition("AND ${accessSql["condition"].toString()}", accessSql["value"] as Array<Any?>)
                }
            }

            // Список студентов
            if (
                referenceId == UUID.fromString("00000000-0000-0000-0000-000000000017")
                && sectionId == UUID.fromString("00000000-0000-0000-0000-000000000001")
            ) {
                var filterProfile: MutableMap<String, String> = mutableMapOf()
                if (filter != null) {
                    filterProfile = filter
                }
                val studentAccess: Any? = profileService.accessStudentList(profile.get(), filterProfile)
                if (studentAccess is Boolean) {
                    return PageImpl<MutableMap<String, Any?>>(result, pagePR, 0)
                } else if (studentAccess is ArrayList<*> && studentAccess.isNotEmpty()) {
                    for (item in studentAccess as ArrayList<MutableMap<String, Any?>>) {
                        grid.addCondition(item["condition"].toString(), item["value"] as Array<Any?>)
                    }
                }

                //Students mentor and subdivision
                grid.addJoin(
                    "structure",
                    "structure.deleted_at IS NULL AND db_array_key_exists(profiles.id::varchar, structure.employee) = 1 AND structure.type = 'subdivision'",
                    "LEFT"
                )
                grid.setColumnEmpty()
                grid.addColumn("DISTINCT(profiles.id)")
//                grid.addOrder("id", "asc")
                grid.addColumn("structure.manager[2][1] as mentor")
//                grid.addColumn("structure.id as subdivision")
                grid.addColumn("structure.display_name as subdivision")
                //Students mentor and subdivision
            }
            // Список студентов

            if (filter != null) {
                val conditionExtra: ArrayList<MutableMap<String, Any?>> = arrayListOf()
                for ((key, item) in filter) {
                    if (!arrayListOf(
                            "page",
                            "size",
                            "sort",
                            "headerenable"
                        ).contains(key) && allField.containsKey(key)
                    ) {
                        when ((allField[key] as MutableMap<*, *>)["type"]) {
                            "integer",
                            "float",
                            "date",
                            "timestamp" -> {
                                val valueRange = item.split(",")
                                if (valueRange.size == 2 && valueRange[0] == valueRange[1]) {
                                    conditionExtra.add(
                                        mutableMapOf(
                                            "title" to (allField[key] as MutableMap<*, *>)["title"],
                                            "value" to valueRange[0],
                                            "fieldId" to key,
                                            "condition" to "equal"
                                        )
                                    )
                                } else if (valueRange.size == 2) {
                                    if (valueRange[0].isNotEmpty()) {
                                        conditionExtra.add(
                                            mutableMapOf(
                                                "title" to (allField[key] as MutableMap<*, *>)["title"],
                                                "value" to valueRange[0],
                                                "fieldId" to key,
                                                "condition" to "more_equal"
                                            )
                                        )
                                    }
                                    if (valueRange[1].isNotEmpty()) {
                                        conditionExtra.add(
                                            mutableMapOf(
                                                "title" to (allField[key] as MutableMap<*, *>)["title"],
                                                "value" to valueRange[1],
                                                "fieldId" to key,
                                                "condition" to "less_equal"
                                            )
                                        )
                                    }
                                }
                            }
                            "enumeration" -> {
                                val valueRange = item.split(",")
                                val conditionsOr: ArrayList<ArrayList<MutableMap<String, Any?>>> = arrayListOf()
                                for (range in (allField[key] as MutableMap<*, *>)["values"] as ArrayList<MutableMap<*, *>>) {
                                    if (valueRange.contains(range["id"])) {
                                        conditionsOr.add(
                                            arrayListOf(
                                                mutableMapOf(
                                                    "title" to (allField[key] as MutableMap<*, *>)["title"],
                                                    "value" to range["value"].toString(),
                                                    "fieldId" to key,
                                                    "condition" to "like"
                                                )
                                            )
                                        )
                                    }
                                }
                                val extraConditionOr = SectionService.prepareCondition(conditionsOr, allField)
                                val extraConditionOr1 =
                                    SectionService.prepareCondition(conditionsOr, allField, "$tableName.")
                                val extraFilterOr = extraConditionOr["condition"].toString()
                                if (extraFilterOr.isNotEmpty()) {
                                    grid.addCondition(
                                        " AND (${extraConditionOr1["condition"].toString()})",
                                        extraConditionOr1["value"] as Array<Any?>
                                    )
                                }
                            }
                            "boolean" -> {
                                val valueRange = item.split(",")
                                val conditionsOr: ArrayList<ArrayList<MutableMap<String, Any?>>> = arrayListOf()
                                if (valueRange.size > 1 && valueRange[0] != valueRange[1]) {
                                    var iBool = 0
                                    for (range in valueRange) {
                                        var rangeItem = range.toBoolean()
                                        iBool++
                                        if (!rangeItem) {
                                            continue
                                        }
                                        if (iBool > 1) {
                                            rangeItem = !rangeItem
                                        }
                                        if (range.isNotEmpty()) {
                                            conditionsOr.add(
                                                arrayListOf(
                                                    mutableMapOf(
                                                        "title" to (allField[key] as MutableMap<*, *>)["title"],
                                                        "value" to rangeItem,
                                                        "fieldId" to key,
                                                        "condition" to "equal"
                                                    )
                                                )
                                            )
                                        }
                                    }
                                    val extraConditionOr = SectionService.prepareCondition(conditionsOr, allField)
                                    val extraConditionOr1 =
                                        SectionService.prepareCondition(conditionsOr, allField, "$tableName.")
                                    val extraFilterOr = extraConditionOr["condition"].toString()
                                    if (extraFilterOr.isNotEmpty()) {
                                        grid.addCondition(
                                            " AND (${extraConditionOr1["condition"].toString()})",
                                            extraConditionOr1["value"] as Array<Any?>
                                        )
                                    }
                                }
                            }
                            else -> {
                                conditionExtra.add(
                                    mutableMapOf(
                                        "title" to (allField[key] as MutableMap<*, *>)["title"],
                                        "value" to item,
                                        "fieldId" to key,
                                        "condition" to "like"
                                    )
                                )
                            }
                        }
                    }
                }
                val extraCondition =
                    SectionService.prepareCondition(arrayListOf(conditionExtra), allField, "$tableName.")
                val extraFilter = extraCondition["condition"].toString()
                if (extraFilter.isNotEmpty()) {
                    grid.addCondition(
                        " AND ${extraCondition["condition"].toString()}",
                        extraCondition["value"] as Array<Any?>
                    )
                }

                if (filter.containsKey("sort")) {
                    for (sort in filter["sort"]!!.split(",")) {
                        val sortArray = sort.split(":")
                        if (sortArray.size > 1) {
                            val fieldId = sortArray[0]
                            if (sortArray[1] == "ascend") {
                                grid.addOrder(
                                    fieldId, "asc",
                                    if (fieldId == "mentor" || fieldId == "subdivision") "" else tableName
                                )
                            } else {
                                grid.addOrder(
                                    fieldId, "desc",
                                    if (fieldId == "mentor" || fieldId == "subdivision") "" else tableName
                                )
                            }
                        }
                    }
                }
            }

            for (sort in section.get().getSortField()) {
                grid.addOrder(sort["id"].toString(), sort["sortBy"].toString())
            }

            var searchCondition: Array<String> = arrayOf()
            for (headerField in section.get().getFields()) {
                (allField[headerField["id"]] as MutableMap<String, Any?>)["id"] = headerField["id"]
                headerTable.add(allField[headerField["id"]] as MutableMap<String, Any?>)
                if (searchValue != null && searchValue.isNotEmpty()) {
                    searchCondition =
                        searchCondition.plus("lower($tableName.${headerField["id"].toString()}::character varying) LIKE lower(?)")
                }
            }
            if (searchCondition.isNotEmpty()) {
                val symbolQ = Array(searchCondition.size) { "%$searchValue%" }
                grid.addCondition(" AND (${searchCondition.joinToString(" OR ")})", arrayOf(*symbolQ))
            }
            totalRows = grid.countTotal() + result.size

            for (item in headerTable) {
                grid.addColumn("$tableName.${item["id"].toString()}")
            }

            result.addAll(grid.getDataPage(section.get().getEnableCustomFields()))
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

    fun getArchiveRefRecordByPage(
        referenceId: UUID,
        sectionId: UUID,
        page: Int = 1,
        size: Int = 20,
        fields: String? = "",
        filter: MutableMap<String, String>? = mutableMapOf(),
        headerEnable: Boolean = false,
        searchValue: String? = "",
        authentication: Authentication,
    ): Page<MutableMap<String, Any?>> {
        val pagePR: PageRequest = PageRequest.of(page - 1, size)
        val reference = referenceRepository.findByIdAndDeletedAtIsNull(referenceId)
        var totalRows1: Long = 0
        var result: MutableList<MutableMap<String, Any?>> = arrayListOf()
        if (reference.isPresent) {
            val tableName = reference.get().getTableName()
            val section = sectionRepository.findByIdAndDeletedAtIsNull(sectionId)
            if (!section.isPresent) {
                return PageImpl<MutableMap<String, Any?>>(result, pagePR, 0)
            }
            val profile = profileService.getProfileByUsernameOrEmail(authentication.name)
            if (!profile.isPresent) {
                return PageImpl<MutableMap<String, Any?>>(result, pagePR, 0)
            }
            val roles = rolesGroupService.getRolesByProfile(profile.get().getId()!!)
            if (!(roles.contains("ADMIN") || roles.contains("CHIEF"))) {
                return PageImpl<MutableMap<String, Any?>>(result, pagePR, 0)
            }

            val aclIds = profileService.getAclIds(profile.get().getId()!!)
            if (Acl.hasAccess(section.get().getAccess(), aclIds).isEmpty() && section.get().getAccess().isNotEmpty()) {
                return PageImpl<MutableMap<String, Any?>>(result, pagePR, 0)
            }

            val grid1 = GridJoinQuery(tableName, dataSourceConfig)

            val allField = Util.mergeMutableMap(
                Util.toJson(reference.get().getUserFields()),
                Util.toJson(reference.get().getSysFields())
            )
            grid1.setRefFields(allField)

            val filters = section.get().getFilterField() as ArrayList<ArrayList<MutableMap<String, Any?>>>

            val conditionFilter1 = prepareCondition(filters, allField, "$tableName.")

            grid1.setPageRequest(pagePR)
            grid1.addCondition("$tableName.deleted_at IS NOT NULL", arrayOf())

            val customFilter = conditionFilter1["condition"].toString()
            if (customFilter.isNotEmpty()) {
                grid1.addCondition(
                    " AND (${conditionFilter1["condition"].toString()})",
                    conditionFilter1["value"] as Array<Any?>
                )
            }

            if (!accessService.mayView(profile.get().getId()!!, reference.get())) {
                return PageImpl<MutableMap<String, Any?>>(result, pagePR, 0)
            } else {
                val accessSql = accessService.getAccess(profile.get().getId()!!, reference.get())
                if (accessSql.containsKey("condition") && accessSql.containsKey("value")) {
                    grid1.addCondition("AND ${accessSql["condition"].toString()}", accessSql["value"] as Array<Any?>)
                }
            }

            // Список студентов
            if (
                referenceId == UUID.fromString("00000000-0000-0000-0000-000000000017")
                && sectionId == UUID.fromString("00000000-0000-0000-0000-000000000001")
            ) {
                var filterProfile: MutableMap<String, String> = mutableMapOf()
                if (filter != null) {
                    filterProfile = filter
                }
                val studentAccess: Any? = profileService.accessStudentList(profile.get(), filterProfile)
                if (studentAccess is Boolean) {
                    return PageImpl<MutableMap<String, Any?>>(result, pagePR, 0)
                } else if (studentAccess is ArrayList<*> && studentAccess.isNotEmpty()) {
                    for (item in studentAccess as ArrayList<MutableMap<String, Any?>>) {
                        grid1.addCondition(item["condition"].toString(), item["value"] as Array<Any?>)
                    }
                }

                //Students mentor and subdivision
                grid1.addJoin(
                    "structure",
                    "structure.deleted_at IS NULL AND db_array_key_exists(profiles.id::varchar, structure.employee) = 1 AND structure.type = 'subdivision'",
                    "LEFT"
                )
                grid1.setColumnEmpty()
                grid1.addColumn("DISTINCT(profiles.id)")
//                grid1.addOrder("id", "asc")
                grid1.addColumn("structure.manager[2][1] as mentor")
//                grid1.addColumn("structure.id as subdivision")
                grid1.addColumn("structure.display_name as subdivision")
                //Students mentor and subdivision
            }
            // Список студентов

            if (filter != null) {
                val conditionExtra: ArrayList<MutableMap<String, Any?>> = arrayListOf()
                for ((key, item) in filter) {
                    if (!arrayListOf(
                            "page",
                            "size",
                            "sort",
                            "headerenable"
                        ).contains(key) && allField.containsKey(key)
                    ) {
                        when ((allField[key] as MutableMap<*, *>)["type"]) {
                            "integer",
                            "float",
                            "date",
                            "timestamp" -> {
                                val valueRange = item.split(",")
                                if (valueRange.size == 2 && valueRange[0] == valueRange[1]) {
                                    conditionExtra.add(
                                        mutableMapOf(
                                            "title" to (allField[key] as MutableMap<*, *>)["title"],
                                            "value" to valueRange[0],
                                            "fieldId" to key,
                                            "condition" to "equal"
                                        )
                                    )
                                } else if (valueRange.size == 2) {
                                    if (valueRange[0].isNotEmpty()) {
                                        conditionExtra.add(
                                            mutableMapOf(
                                                "title" to (allField[key] as MutableMap<*, *>)["title"],
                                                "value" to valueRange[0],
                                                "fieldId" to key,
                                                "condition" to "more_equal"
                                            )
                                        )
                                    }
                                    if (valueRange[1].isNotEmpty()) {
                                        conditionExtra.add(
                                            mutableMapOf(
                                                "title" to (allField[key] as MutableMap<*, *>)["title"],
                                                "value" to valueRange[1],
                                                "fieldId" to key,
                                                "condition" to "less_equal"
                                            )
                                        )
                                    }
                                }
                            }
                            "enumeration" -> {
                                val valueRange = item.split(",")
                                val conditionsOr: ArrayList<ArrayList<MutableMap<String, Any?>>> = arrayListOf()
                                for (range in (allField[key] as MutableMap<*, *>)["values"] as ArrayList<MutableMap<*, *>>) {
                                    if (valueRange.contains(range["id"])) {
                                        conditionsOr.add(
                                            arrayListOf(
                                                mutableMapOf(
                                                    "title" to (allField[key] as MutableMap<*, *>)["title"],
                                                    "value" to range["value"].toString(),
                                                    "fieldId" to key,
                                                    "condition" to "like"
                                                )
                                            )
                                        )
                                    }
                                }
                                val extraConditionOr1 = prepareCondition(conditionsOr, allField, "$tableName.")
                                val extraFilterOr = extraConditionOr1["condition"].toString()
                                if (extraFilterOr.isNotEmpty()) {
                                    grid1.addCondition(
                                        " AND (${extraConditionOr1["condition"].toString()})",
                                        extraConditionOr1["value"] as Array<Any?>
                                    )
                                }
                            }
                            "boolean" -> {
                                val valueRange = item.split(",")
                                val conditionsOr: ArrayList<ArrayList<MutableMap<String, Any?>>> = arrayListOf()
                                if (valueRange.size > 1 && valueRange[0] != valueRange[1]) {
                                    var iBool = 0
                                    for (range in valueRange) {
                                        var rangeItem = range.toBoolean()
                                        iBool++
                                        if (!rangeItem) {
                                            continue
                                        }
                                        if (iBool > 1) {
                                            rangeItem = !rangeItem
                                        }
                                        if (range.isNotEmpty()) {
                                            conditionsOr.add(
                                                arrayListOf(
                                                    mutableMapOf(
                                                        "title" to (allField[key] as MutableMap<*, *>)["title"],
                                                        "value" to rangeItem,
                                                        "fieldId" to key,
                                                        "condition" to "equal"
                                                    )
                                                )
                                            )
                                        }
                                    }
                                    val extraConditionOr1 =
                                        SectionService.prepareCondition(conditionsOr, allField, "$tableName.")
                                    val extraFilterOr = extraConditionOr1["condition"].toString()
                                    if (extraFilterOr.isNotEmpty()) {
                                        grid1.addCondition(
                                            " AND (${extraConditionOr1["condition"].toString()})",
                                            extraConditionOr1["value"] as Array<Any?>
                                        )
                                    }
                                }
                            }
                            else -> {
                                conditionExtra.add(
                                    mutableMapOf(
                                        "title" to (allField[key] as MutableMap<*, *>)["title"],
                                        "value" to item,
                                        "fieldId" to key,
                                        "condition" to "like"
                                    )
                                )
                            }
                        }
                    }
                }

                val extraCondition1 = prepareCondition(arrayListOf(conditionExtra), allField, "$tableName.")
                val extraFilter = extraCondition1["condition"].toString()
                if (extraFilter.isNotEmpty()) {
                    grid1.addCondition(
                        " AND ${extraCondition1["condition"].toString()}",
                        extraCondition1["value"] as Array<Any?>
                    )
                }

                if (filter.containsKey("sort")) {
                    for (sort in filter["sort"]!!.split(",")) {
                        val sortArray = sort.split(":")
                        if (sortArray.size > 1) {
                            val fieldId = sortArray[0]
                            if (sortArray[1] == "ascend") {
                                grid1.addOrder(
                                    fieldId, "asc",
                                    if (fieldId == "mentor" || fieldId == "subdivision") "" else tableName
                                )
                            } else {
                                grid1.addOrder(
                                    fieldId, "desc",
                                    if (fieldId == "mentor" || fieldId == "subdivision") "" else tableName
                                )
                            }
                        }
                    }
                }
            }

            for (sort in section.get().getSortField()) {
                grid1.addOrder(sort["id"].toString(), sort["sortBy"].toString())
            }

            var searchCondition1: Array<String> = arrayOf()
            val headerTable: MutableList<MutableMap<String, Any?>> = mutableListOf()
            for (headerField in section.get().getFields()) {
                (allField[headerField["id"]] as MutableMap<String, Any?>)["id"] = headerField["id"]
                headerTable.add(allField[headerField["id"]] as MutableMap<String, Any?>)
                if (searchValue != null && searchValue.isNotEmpty()) {
                    searchCondition1 =
                        searchCondition1.plus("lower($tableName.${headerField["id"].toString()}::character varying) LIKE lower(?)")
                }
            }
            if (searchCondition1.isNotEmpty()) {
                val symbolQ = Array(searchCondition1.size) { "%$searchValue%" }
                grid1.addCondition(" AND (${searchCondition1.joinToString(" OR ")})", arrayOf(*symbolQ))
            }

            totalRows1 = grid1.countTotal()

            for (item in headerTable) {
                grid1.addColumn("$tableName.${item["id"].toString()}")
            }


            result = if (headerEnable) {
                arrayListOf(mutableMapOf("header" to headerTable))
            } else {
                arrayListOf()
            }
            result.addAll(grid1.getDataPage(section.get().getEnableCustomFields()))
        }
        return PageImpl(
            result,
            pagePR,
            totalRows1
        )
    }

    fun fromHead(referenceId: UUID): Boolean {
        return referenceId == POST_CATEGORY || referenceId == POST_NEWS || referenceId == POST_STORIES
    }

    companion object {
        // TODO if will use TABLE NAME,  must end with "."
        fun prepareCondition(
            filter: ArrayList<ArrayList<MutableMap<String, Any?>>>,
            fields: MutableMap<String, Any?>,
            tableName: String = ""
        ): MutableMap<String, Any?> {
            var result: Array<String> = arrayOf()
            val numberConditions = Util.getNumberConditions()
            val stringConditions = Util.getStringConditions()

            var values: Array<Any?> = arrayOf()
            for (item in filter) {
                var condition: Array<Any?> = arrayOf()
                for (item2 in item) {
                    val type: String = (fields[item2["fieldId"]] as MutableMap<String, Any?>)["type"].toString()
                    when (type) {
                        "integer",
                        "float",
                        "date",
                        "timestamp" -> {
                            if (numberConditions.containsKey(item2["condition"])) {
                                if (item2["value"].toString().isNotEmpty()) {
                                    // TABLE NAME must end with "."
                                    condition =
                                        condition.plus("$tableName${item2["fieldId"]} ${numberConditions[item2["condition"]]} ?")

                                    if (type == "integer") {
                                        values = values.plus(item2["value"].toString().toLongOrNull())
                                    } else if (type == "float") {
                                        values = values.plus(item2["value"].toString().toDoubleOrNull())
                                    } else if (type == "date") {
                                        values = values.plus(LocalDate.parse(item2["value"].toString()))
                                    } else {
                                        values = values.plus(LocalDateTime.parse(item2["value"].toString()))
                                    }
                                }
                            }
                        }

                        "string",
                        "text",
                        "reference",
                        "enumeration",
                        "table" -> {
                            if (stringConditions.containsKey(item2["condition"])) {
                                if (item2["value"].toString().isNotEmpty()) {
                                    condition = condition.plus(
                                        "lower($tableName${
                                            EngineQuery.castColumn(
                                                item2["fieldId"].toString(),
                                                type
                                            )
                                        }) ${stringConditions[item2["condition"]]} lower(?)"
                                    )
                                    values = values.plus("%${item2["value"]}%")
                                }
//                                else {
//                                    var cond = ""
//                                    if (item2["condition"] == "like") {
//                                        cond = "IS"
//                                    } else {
//                                        cond = "IS NOT"
//                                    }
//                                    condition = condition.plus("${EngineQuery.castColumn(item2["fieldId"].toString(), type)} $cond NULL")
//                                }

                            }

                        }
                        "boolean" -> {
                            if (item2["condition"] == "equal") {
                                if (item2["value"].toString().isNotEmpty()) {
                                    condition = condition.plus("$tableName${item2["fieldId"].toString()} = ?")
                                    values = values.plus(item2["value"].toString().toLowerCase() == "true")
                                }
                            }
                        }
                        "structure" -> {

                        }
                        else -> {

                        }
                    }

                }
                if (condition.isNotEmpty()) {
                    result = result.plus("(${condition.joinToString(" AND ")})")
                }
            }

            return mutableMapOf("condition" to result.joinToString(" OR "), "value" to values)
        }
    }

}
