package com.mdsp.backend.app.system.service.Acl

import org.springframework.context.annotation.Configuration
import java.util.*


@Configuration
class Acl {
    companion object {
        fun hasAccess(arrayFirst: Array<UUID>, arraySecond: Array<UUID>): Array<UUID> {
            val set = HashSet<UUID>()

            set.addAll(arrayFirst)
            set.retainAll(arraySecond)

            var intersection: Array<UUID>? = arrayOf()
            intersection = set.toArray(intersection)

            return intersection
        }

        fun hasAccess(arrayFirst: Array<Array<String>>, arraySecond: Array<UUID>): Array<UUID> {
            var arrayPrepare: Array<UUID> = arrayOf()
            for (item in arrayFirst) {
                for (innerItem in item) {
                    arrayPrepare = arrayPrepare.plus(UUID.fromString(innerItem))
                }
                break
            }
            return hasAccess(arrayPrepare, arraySecond)
        }

        fun hasAccess(arrayFirst: ArrayList<MutableMap<String, Any?>>, arraySecond: Array<UUID>): Array<UUID> {
            var arrayPrepare: Array<UUID> = arrayOf()
            for (item in arrayFirst) {
                arrayPrepare = arrayPrepare.plus(UUID.fromString(item["id"].toString()))
            }
            return hasAccess(arrayPrepare, arraySecond)
        }
    }
}

