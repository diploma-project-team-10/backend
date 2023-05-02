package com.mdsp.backend.app.reference.model.fields

class FieldFactory {
    companion object {
        fun create(name: String, config: MutableMap<String, Any?>) =
            when (config["type"]) {
                "integer" -> {
                    IntegerField(name, config)
                }
                "float" -> {
                    FloatField(name, config)
                }
                "string", "uuid" -> {
                    StringField(name, config)
                }
                "text" -> {
                    TextField(name, config)
                }
                "password" -> {
                    PasswordField(name, config)
                }
                "date" -> {
                    DateField(name, config)
                }
                "timestamp" -> {
                    TimestampField(name, config)
                }
                "boolean" -> {
                    BooleanField(name, config)
                }
                "enumeration" -> {
                    EnumerationField(name, config)
                }
                "reference" -> {
                    ReferenceField(name, config)
                }
                "structure" -> {
                    StructureField(name, config)
                }
                "table" -> {
                    TableField(name, config)
                }
                "image" -> {
                    ImageField(name, config)
                }
                "file" -> {
                    FileField(name, config)
                }
                else -> throw Exception("This class not exist!")
            }
    }
}
