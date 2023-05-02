package com.mdsp.backend.app.files.model

import com.mdsp.backend.app.system.model.DateAudit
import com.vladmihalcea.hibernate.type.basic.PostgreSQLHStoreType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import java.text.SimpleDateFormat
import java.util.*
import javax.persistence.*
import java.util.HashMap

@Entity
@Table(name = "files")
@TypeDef(name = "hstore", typeClass = PostgreSQLHStoreType::class)
open class Files : DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private var id: UUID? = null

    @Type(type = "hstore")
    @Column(name = "files_names", columnDefinition = "hstore")
    private var filesNames: MutableMap<String, String?> = HashMap()

    @Column(name = "fileFormat")
    private var filesFormat: String? = null

    @Column(name = "mime")
    private var mime: String? = null

    @Column(name = "hash_file")
    private var hashFile: String? = null

    @Column(name = "size")
    private var size: Long? = null

    @Column(name = "subject_id")
    private var subjectId: UUID? = null

    @Column(name = "object_id")
    private var objectId: UUID? = null

    constructor(id: UUID?) {
        this.id = id
    }

    fun getId() = this.id
    fun getHashFile() = this.hashFile
    fun getMime() = this.mime
    fun getFilesName(key: String) = this.filesNames[key]
    fun getFilesFormat(): String? = this.filesFormat
    fun getFilesPath(pathFiles: String): String = pathFiles + SimpleDateFormat("/yyyy/MM/dd/").format(createdAt)
    fun getFilesNameDB(type: String = "", size: String = ""): String = type + this.id + size
    fun getFilesFullPath(pathFiles: String, type: String = "", size: String = ""): String = getFilesPath(pathFiles) + getFilesNameDB(type, size)
    fun getSizeInByte() = this.size
    fun getSubjectId() = this.subjectId
    fun getObjectId() = this.objectId

    fun setId(id: UUID?) {
        this.id = id
    }

    fun setHashFile(hashFile: String?) {
        this.hashFile = hashFile
    }

    fun setMime(mime: String?) {
        this.mime = mime
    }

    fun setSize(size: Long) {
        this.size = size
    }

    fun setName(fileName: String): Int {
        val countKey = filesNames.size
        var key = 0
        while (key < countKey) {
            if (this.filesNames[key.toString()] == fileName) {
                return key
            }
            key++
        }
        this.filesNames[countKey.toString()] = fileName
        return countKey
    }

    fun setFilesFormat(filesFormat: String) {
        this.filesFormat = filesFormat
    }

    fun setSubjectId(subjectId: UUID?) {
        this.subjectId = subjectId
    }

    fun setObjectId(objectId: UUID?) {
        this.objectId = objectId
    }
}
