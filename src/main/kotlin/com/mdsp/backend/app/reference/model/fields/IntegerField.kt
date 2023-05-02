package com.mdsp.backend.app.reference.model.fields

class IntegerField(name: String, config: MutableMap<String, Any?>): Field(name, config) {
    override fun setValue(valueField: Any?) {
        this.valueField = null
        if (valueField != null && valueField.toString().isNotEmpty()) {
            this.valueField = valueField.toString().toLong()

            if (this.config["hasRange"] as Boolean) {

                if (
                    this.config["rangeFrom"] != null
                    && this.config["rangeFrom"].toString().toLong() > this.valueField.toString().toLong()
                ) {
                    this.error += "${this.config["title"]} from: ${this.config["rangeFrom"]}\n"
                }

                if (
                    this.config["rangeTo"] != null
                    && this.config["rangeTo"].toString().toLong() < this.valueField.toString().toLong()
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
                    if (this.config["hasRange"] as Boolean) {
                        item.setValue(item.value.toString().toIntOrNull())
                    } else {
                        item.setValue(null)
                    }

                }
                "rangeTo" -> {
                    if (this.config["hasRange"] as Boolean) {
                        item.setValue(item.value.toString().toIntOrNull())
                    } else {
                        item.setValue(null)
                    }
                }
                "defaultValue" -> {
                    this.setValue(item.value.toString().toIntOrNull())
                    item.setValue(item.value.toString().toIntOrNull())
                }
            }
        }
    }

    override fun serialized() = this.valueField.toString().toLongOrNull()

}
