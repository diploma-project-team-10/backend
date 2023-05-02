package com.mdsp.backend.app.reference.model.fields

class TextField(name: String, config: MutableMap<String, Any?>): Field(name, config) {
    override fun setValue(valueField: Any?) {
        this.valueField = null
        if (valueField != null) {
            this.valueField = valueField.toString()
        }
        if (!this.isConfig && this.config["isRequired"] as Boolean && (this.valueField == null || this.valueField.toString().isEmpty())) {
            this.error += "${this.config["title"]} is require\n"
        }
    }

    override fun newField() {
        super.newField()
        this.config["isUnique"] = false
        this.config["defaultValue"] = null
        this.config["maxShowLength"] = null
    }

    override fun setConfig() {
        super.setConfig()
        for (item in this.config) {
            when (item.key) {
                "isUnique" -> {
                    item.setValue(item.value as Boolean)
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
