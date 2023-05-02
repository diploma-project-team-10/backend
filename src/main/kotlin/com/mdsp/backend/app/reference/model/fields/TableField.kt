package com.mdsp.backend.app.reference.model.fields

class TableField(name: String, config: MutableMap<String, Any?>): Field(name, config) {
    override fun setValue(valueField: Any?) {
        this.valueField = null
        if (valueField != null) {

        }
        if (!this.isConfig && this.config["isRequired"] as Boolean && this.valueField == null) {
            this.error += "${this.config["title"]} is require\n"
        }
    }

    override fun newField() {
        super.newField()
        this.config["width"] = "100%"
        this.config["defaultValue"] = null
        this.config["cellPadding"] = 0
        this.config["border"] = 1
        this.config["isNumbering"] = false
        this.config["showTotal"] = false
        this.config["canAddDelete"] = false
        this.config["separator"] = ", "
        this.config["fields"] = null
    }

    override fun setConfig() {
        super.setConfig()
        for (item in this.config) {
            when (item.key) {
                "width", "separator" -> {
                    item.setValue(item.value.toString())
                }
                "cellPadding" -> {
                    item.setValue(item.value.toString().toIntOrNull())
                }
                "border" -> {
                    item.setValue(item.value.toString().toIntOrNull())
                }
                "isNumbering", "showTotal", "canAddDelete" -> {
                    item.setValue(item.value as Boolean)
                }
                "defaultValue", "fields" -> {
                    item.setValue(item.value)
                }
            }
        }
    }

}
