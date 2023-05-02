package com.mdsp.backend.app.reference.model.fields

class FloatField(name: String, config: MutableMap<String, Any?>): Field(name, config) {
    override fun setValue(valueField: Any?) {
        this.valueField = null
        if (valueField != null && valueField.toString().isNotEmpty()) {
            this.valueField = valueField.toString().toFloat()

            if (this.config["hasRange"] as Boolean) {

                if (
                    this.config["rangeFrom"] != null
                    && this.config["rangeFrom"].toString().toFloat() > this.valueField.toString().toFloat()
                ) {
                    this.error += "${this.config["title"]} from: ${this.config["rangeFrom"]}\n"
                }

                if (
                    this.config["rangeTo"] != null
                    && this.config["rangeTo"].toString().toFloat() < this.valueField.toString().toFloat()
                ) {
                    this.error += "${this.config["title"]} range to: ${this.config["rangeTo"]}\n"
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
        this.config["hasRange"] = false
        this.config["rangeFrom"] = null
        this.config["rangeTo"] = null
    }

    override fun setConfig() {
        super.setConfig()
        for (item in this.config) {
            when (item.key) {
                "isUnique" -> {
                    item.setValue(item.value as Boolean)
                }
                "hasRange" -> {
                    item.setValue(item.value as Boolean)
                }
                "rangeFrom" -> {
                    item.setValue(item.value.toString().toFloatOrNull())
                }
                "rangeTo" -> {
                    item.setValue(item.value.toString().toFloatOrNull())
                }
                "defaultValue" -> {
                    this.setValue(item.value)
                    item.setValue(item.value.toString().toFloatOrNull())
                }
            }
        }
    }

    override fun serialized() = this.valueField.toString().toDoubleOrNull()

}
