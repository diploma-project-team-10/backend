package com.mdsp.backend.app.files.payload

import com.mdsp.backend.app.system.model.DateAudit
import java.util.*

class FilesPayload {
    private var id: String?
    private var fileName: String
    private var fileFormat: String
    private var mime: String
    private var base64: String

    constructor(id: String?, fileName: String, fileFormat: String, mime: String, base64: String) {
        this.id = id
        this.fileName = fileName
        this.fileFormat = fileFormat
        this.mime = mime
        this.base64 = base64
    }

    fun getId(): String? = this.id
    fun getFileName(): String = this.fileName
    fun getFileFormat(): String = this.fileFormat
    fun getMime(): String = this.mime
    fun getBase64(): String = this.base64

    fun setId(id: String?) {this.id = id}
    fun setFileName(fileName: String) { this.fileName = fileName }
    fun setFileFormat(fileFormat: String) { this.fileFormat = fileFormat }
    fun setMime(mime: String) { this.mime = mime }
    fun setBase64(base64: String) { this.base64 = base64 }
}