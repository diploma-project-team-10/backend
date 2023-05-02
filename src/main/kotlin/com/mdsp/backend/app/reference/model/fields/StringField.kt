package com.mdsp.backend.app.reference.model.fields

import java.util.*

class StringField(name: String, config: MutableMap<String, Any?>): Field(name, config) {
    override fun setValue(valueField: Any?) {
        this.valueField = null
        if (valueField != null) {
            this.valueField = valueField.toString().trim()

            if (
                this.config["minLength"] != null
                && this.config["minLength"].toString().toLong() > this.valueField.toString().length
            ) {
                this.error += "${this.config["title"]} is min length: ${this.config["minLength"]}\n"
            }

            if (
                this.config["maxLength"] != null
                && this.config["maxLength"].toString().toLong() < this.valueField.toString().length
            ) {
                this.error += "${this.config["title"]} is max length: ${this.config["maxLength"]}\n"
            }


            when (this.config["mask"]) {
                "phone" -> {
                    val phones = this.valueField.toString().trim().split(",").toMutableList()
                    for ((key, phone) in phones.withIndex()) {
                        if (phone.trim().length == 11) {
                            if (phone.trim()[0] == '8') {
                                phones[key] = "+7" + phone.trim().substring(1)
                            } else if (phone.trim()[0] == '7') {
                                phones[key] = "+" + phone.trim()
                            }
                        } else if (phone.trim().length == 10) {
                            phones[key] = "+7" + phone.trim()
                        } else if (phone.trim().length == 12 && phone != phone.trim()) {
                            phones[key] = phone.trim()
                        }
                    }
                    this.valueField = phones.joinToString(",")
                }
            }

            if (this.config["type"] == "uuid") {
                println(name)
                println(this.valueField)
                this.valueField = UUID.fromString(this.valueField.toString())
            }
        }
        if (!this.isConfig && this.config["isRequired"] as Boolean && (this.valueField == null || this.valueField.toString().isEmpty())) {
            this.error += "${this.config["title"]} is require\n"
        }
    }

    override fun newField() {
        super.newField()
        this.config["isUnique"] = false
        this.config["defaultValue"] = null
        this.config["minLength"] = null
        this.config["maxLength"] = null
        this.config["maxShowLength"] = null
        this.config["mask"] = null
    }

    override fun setConfig() {
        super.setConfig()
        for (item in this.config) {
            when (item.key) {
                "isUnique" -> {
                    item.setValue(item.value as Boolean)
                }
                "minLength" -> {
                    item.setValue(item.value.toString().toIntOrNull())
                }
                "maxLength" -> {
                    item.setValue(item.value.toString().toIntOrNull())
                }
                "maxShowLength" -> {
                    item.setValue(item.value.toString().toIntOrNull())
                }
                "defaultValue" -> {
                    this.setValue(item.value)
                    if (item.value != null) {
                        item.setValue(item.value.toString())
                    } else {
                        item.setValue(null)
                    }

                }
            }
        }
    }

}
