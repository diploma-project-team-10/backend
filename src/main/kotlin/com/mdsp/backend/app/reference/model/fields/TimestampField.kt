package com.mdsp.backend.app.reference.model.fields

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TimestampField(name: String, config: MutableMap<String, Any?>): Field(name, config) {
    //TODO all fields set Value on create record
    override fun setValue(valueField: Any?) {
        val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        this.valueField = null
        if (valueField != null) {
            this.valueField = LocalDateTime.parse(valueField.toString().replace(" ", "T").replace("Z", ""))
            if (this.config["minDay"] != null) {
                var minDate = LocalDateTime.now().minusDays(this.config["minDay"].toString().toLong())
                if (this.config["minTime"] != null) {
                    minDate = minDate.minusHours(this.config["minTime"].toString().substring(0, 2).toLong())
                    minDate = minDate.minusMinutes(this.config["minTime"].toString().substring(3, 5).toLong())
                }
                if (!minDate.isBefore(this.valueField as LocalDateTime)) {
                    this.error += "${this.config["title"]} min Date: ${minDate.format(dateFormat)}\n"
                }

            }
            if (this.config["maxDay"] != null) {
                var maxDate = LocalDateTime.now().plusDays(this.config["maxDay"].toString().toLong())
                if (this.config["maxTime"] != null) {
                    maxDate = maxDate.plusHours(this.config["maxTime"].toString().substring(0, 2).toLong())
                    maxDate = maxDate.plusMinutes(this.config["maxTime"].toString().substring(3, 5).toLong())
                }
                if (!maxDate.isAfter(this.valueField as LocalDateTime)) {
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
        this.config["defaultValue"] = false
        this.config["isUnique"] = false
        this.config["defaultValue"] = null
        this.config["currentTimestamp"] = false
        this.config["addDays"] = null
        this.config["addTime"] = null
        this.config["viewFormat"] = null
        this.config["minDay"] = null
        this.config["minTime"] = null
        this.config["maxDay"] = null
        this.config["maxTime"] = null
    }

    override fun setConfig() {
        super.setConfig()
        for (item in this.config) {
            when (item.key) {
                "defaultValue" -> {
                    if (this.config["currentTimestamp"] == false) {
                        this.setValue(item.value)
                    }
                    item.setValue(item.value)
                }
            }
        }
    }

    override fun serialized(): LocalDateTime? {
        if (this.valueField == null) {
            return null
        }
        return LocalDateTime.parse(this.valueField.toString().replace(" ", "T"))
    }

}
