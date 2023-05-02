package com.mdsp.backend.app.reference.model.fields

class BooleanField(name: String, config: MutableMap<String, Any?>): Field(name, config) {
    override fun setValue(valueField: Any?) {
        if (valueField != null) {
            this.valueField = valueField.toString().toBoolean()
        } else {
            this.valueField = false
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
                "defaultValue" -> {
                    this.setValue(item.value.toString().toBoolean())
                    item.setValue(item.value.toString().toBoolean())
                }
            }
        }
    }

    override fun unserialized(serialized: Any?) {
        super.unserialized(serialized)
        this.valueField = this.valueField.toString() == "true"
    }

    override fun unserializedToString(): String {
        if (this.valueField.toString() == "true") {
            return "Yes"
        }
        return "No"
    }

}
