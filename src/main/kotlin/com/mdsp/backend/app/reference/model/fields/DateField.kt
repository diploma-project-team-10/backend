package com.mdsp.backend.app.reference.model.fields

import org.springframework.format.annotation.DateTimeFormat
import java.text.DateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class DateField(name: String, config: MutableMap<String, Any?>): Field(name, config) {
    override fun setValue(valueField: Any?) {
        val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        this.valueField = null
        if (valueField != null) {
            var valDate = valueField.toString()
            if (valDate.length >= 10) {
                valDate = valDate.substring(0, 10).trim()
            }
            this.valueField = LocalDate.parse(valDate)
            if (this.config["minDate"] != null) {
                val minDate = LocalDate.now().minusDays(this.config["minDate"].toString().toLong())
                if (!minDate.isBefore(this.valueField as LocalDate) && !minDate.isEqual(this.valueField as LocalDate)) {
                    this.error += "${this.config["title"]} min Date: ${minDate.format(dateFormat)}\n"
                }
            }
            if (this.config["maxDate"] != null) {
                val maxDate = LocalDate.now().plusDays(this.config["maxDate"].toString().toLong())
                if (!maxDate.isAfter(this.valueField as LocalDate) && !maxDate.isEqual(this.valueField as LocalDate)) {
                    this.error += "${this.config["title"]} max Date: ${maxDate.format(dateFormat)}\n"
                }
            }
        }
        if (!this.isConfig && this.config["isRequired"] as Boolean && this.valueField == null) {
            this.error += "${this.config["title"]} is require\n"
        }
    }

    override fun newField() {
        super.newField()
        this.config["isUnique"] = false
        this.config["defaultValue"] = null
        this.config["currentTimestamp"] = false
        this.config["addDays"] = null
        this.config["viewFormat"] = null
        this.config["minDate"] = null
        this.config["maxDate"] = null
    }

    override fun setConfig() {
        super.setConfig()
        for (item in this.config) {
            when (item.key) {
                "isUnique" -> {
                    item.setValue(item.value as Boolean)
                }
                "currentTimestamp" -> {
                    item.setValue(item.value as Boolean)
                }
                "addDays" -> {
                    if (this.config["currentTimestamp"] as Boolean) {
                        item.setValue(item.value.toString().toIntOrNull())
                    } else {
                        item.setValue(null)
                    }

                }
                "viewFormat" -> {
                    item.setValue(item.value)
                }
                "minDate" -> {
                    item.setValue(item.value.toString().toIntOrNull())
                }
                "maxDate" -> {
                    item.setValue(item.value.toString().toIntOrNull())
                }
                "defaultValue" -> {
                    this.setValue(item.value)
                    if (item.value.toString().isEmpty() || this.config["currentTimestamp"] as Boolean) {
                        item.setValue(null)
                    } else {
                        item.setValue(item.value)
                    }
                }
            }
        }
    }

    override fun serialized(): LocalDate? {
        if (this.valueField == null) {
            return null
        }
        return LocalDate.parse(this.valueField.toString())
    }

}
