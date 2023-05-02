package com.mdsp.backend.app.reference.model.fields

import org.springframework.beans.factory.annotation.Autowired

class PasswordField(name: String, config: MutableMap<String, Any?>): Field(name, config) {

    override fun setValue(valueField: Any?) {
        this.valueField = null
        if (valueField != null && valueField.toString().isNotEmpty()) {
            this.valueField = valueField.toString().trim()
            if (this.valueField.toString().length < 7) {
                this.error += "${this.config["title"]} is min length: 7\n"
            }
        }
        if (!this.isConfig && this.config["isRequired"] as Boolean && this.valueField == null) {
            this.error += "${this.config["title"]} is require\n"
        }
    }

    override fun newField() {
        super.newField()
        this.config["defaultValue"] = null
    }

    override fun setConfig() {
        super.setConfig()
        for (item in this.config) {
            when (item.key) {
                "confirm" -> {
                    item.setValue(item.value.toString())
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

    override fun unserializedToString(): String? {
        return null
    }

}
