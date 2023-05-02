package com.mdsp.backend.app.reference.model.fields

import com.mdsp.backend.app.system.model.Util
import org.apache.commons.lang.StringEscapeUtils

abstract class ComplexField(name: String, config: MutableMap<String, Any?>): Field(name, config) {
    override fun setValue(valueField: Any?) {
        if (valueField !is Array<*>) {
            this.valueField = null
        }
        if (
            valueField != null
            && valueField is ArrayList<*>
            && (valueField).isNotEmpty()
        ) {
            val valField = valueField as ArrayList<MutableMap<String, Any?>>
            if (this.config["isSingle"] as Boolean) {
                valField[0]["value"] = valField[0]["value"].toString().replace("\"", "&quot;").trim()
                this.valueField = mutableMapOf(valField[0]["id"] to valField[0]["value"])
            } else {
                var fieldTar: MutableMap<String, Any?> = mutableMapOf()
                for (item in valField) {
                    item["value"] = item["value"].toString().replace("\"", "&quot;").trim()
                    fieldTar[item["id"].toString()] = item["value"]
                }
                this.valueField = fieldTar
            }
        }
        if (
            !this.isConfig
            && this.config["isRequired"] as Boolean
            && (this.valueField == null || (this.valueField is MutableMap<*, *> && (this.valueField as MutableMap<String, Any?>).isEmpty()))
        ) {
            this.error += "${this.config["title"]} is require\n"
        }
    }


    override fun serialized(): Any? {
        if (this.valueField != null && this.valueField is MutableMap<*, *>) {
            val valueF: MutableMap<String, String?> = this.valueField!! as MutableMap<String, String?>
            var valF: Array<Array<String>> = arrayOf()
            var valKey: Array<String> = arrayOf()
            var valValue: Array<String> = arrayOf()
            for (item in valueF) {
                valKey = valKey.plus(item.key.toString())
                if (item.value != null) {
                    valValue = valValue.plus(item.value!!)
                } else {
                    valValue = valValue.plus("")
                }
            }
            valF = valF.plus(valKey)
            valF = valF.plus(valValue)

            this.valueField = valF
        }

        return this.valueField
    }

    override fun unserialized(serialized: Any?) {
        super.unserialized(serialized)
        if (serialized != null) {
//            var serString = Util.setQuotes(serialized.toString())
            this.valueField = Util.unserialized(serialized.toString())
            val arrResult: ArrayList<MutableMap<String, Any?>> = arrayListOf()
            for ((key, item) in this.valueField as MutableMap<String, Any?>) {
                arrResult.add(mutableMapOf("id" to key, "value" to StringEscapeUtils.unescapeHtml(item.toString())))
            }
            this.valueField = arrResult
        }
    }

    override fun unserializedToString(): String {
        val result: ArrayList<String> = arrayListOf()
        if (this.valueField != null && this.valueField != "null") {
            for (item in (this.valueField as ArrayList<MutableMap<String, Any?>>)) {
                result.add(item["value"].toString())
            }
        }
        return result.joinToString(this.config["separator"].toString())
    }

}
