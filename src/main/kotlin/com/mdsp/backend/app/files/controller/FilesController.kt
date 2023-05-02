package com.mdsp.backend.app.files.controller

import com.mdsp.backend.app.files.model.Files
import com.mdsp.backend.app.files.payload.*
import com.mdsp.backend.app.files.repository.IFilesRepository
import com.mdsp.backend.app.files.service.FilesService
import com.mdsp.backend.app.profile.service.ProfileService
import com.mdsp.backend.app.reference.service.SectionService
import com.mdsp.backend.app.system.model.Status
import com.mdsp.backend.app.user.model.UserPrincipal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.InputStreamResource
import org.springframework.http.*
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.*
import java.sql.Timestamp
import java.util.*
import javax.validation.Valid


@RestController
@RequestMapping("/api/media/file")
class FilesController {
    @Value("\${file.tmp.upload-dir}")
    private val pathFiles: String = ""

    @Autowired
    lateinit var sectionService: SectionService

    @Autowired
    lateinit var filesRepository: IFilesRepository

    @Autowired
    lateinit var profileService: ProfileService

    private val qualityCompress = arrayListOf("s", "m", "l")


    @GetMapping("{id}")
    @PreAuthorize("isAuthenticated()")
    fun getImageSource(
        @PathVariable(value = "id") id: UUID,
        @RequestParam(value = "type") type: String? = "",
        @RequestParam(value = "size") size: String? = ""
    ): ResponseEntity<*> {
        val file = filesRepository.findByIdAndDeletedAtIsNull(id)
        if (!file.isPresent) {
            return ResponseEntity(null, HttpStatus.BAD_REQUEST)
        }
        var typeFile =""
        var sizeFile =""
        if (!type.isNullOrEmpty()) {
            typeFile = type
        }
        if (!size.isNullOrEmpty()) {
            sizeFile = size
        }
        val pathToFile = file.get().getFilesFullPath(pathFiles, typeFile, sizeFile)
        if (FilesService.filesExists(pathToFile)) {
            val resource = InputStreamResource(FileInputStream(pathToFile))
            return ResponseEntity.ok()
                .contentType(FilesService.getContentTypeFromMime(file.get().getMime()))
                .body(resource)
        }
        return ResponseEntity("File not exists", HttpStatus.BAD_REQUEST)
    }

    @GetMapping("/thumb/{id}")
    fun getImageSourceThumb(@PathVariable(value = "id") id: UUID): ResponseEntity<*> {
        val file = filesRepository.findByIdAndDeletedAtIsNull(id)
        if (!file.isPresent) {
            return ResponseEntity(null, HttpStatus.BAD_REQUEST)
        }
        val pathToFile = file.get().getFilesFullPath(pathFiles, "thumb_", "m")
        if (FilesService.filesExists(pathToFile)) {
            val resource = InputStreamResource(FileInputStream(pathToFile))
            return ResponseEntity.ok()
                .contentType(FilesService.getContentTypeFromMime(file.get().getMime()))
                .body(resource)
        }
        return ResponseEntity("File not exists", HttpStatus.BAD_REQUEST)
    }

    @GetMapping("/download/{id}")
    @PreAuthorize("isAuthenticated()")
    fun getDownloadFile(
        @PathVariable(value = "id") id: UUID,
        authentication: Authentication
    ): ResponseEntity<*> {
        val file = filesRepository.findByIdAndDeletedAtIsNull(id)
        if (!file.isPresent) {
            return ResponseEntity(null, HttpStatus.BAD_GATEWAY)
        }
        val pathToFile = file.get().getFilesFullPath(this.pathFiles)
        if (FilesService.filesExists(pathToFile)) {
            val resource = InputStreamResource(FileInputStream(pathToFile))
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=${file.get().getFilesName("0")}.${file.get().getFilesFormat()}")
                .contentType(FilesService.getContentTypeFromMime(file.get().getMime(), MediaType.APPLICATION_OCTET_STREAM))
                .body(resource)
        }
        return ResponseEntity("File not exists", HttpStatus.BAD_REQUEST)
    }

    @PostMapping("request-file")
    @PreAuthorize("isAuthenticated()")
    fun getFileBase64(
        @Valid @RequestBody filesPayload: FilesPayload,
        authentication: Authentication
    ): ResponseEntity<*> {
        val status = Status()
        val file: File = FilesService.decoder(filesPayload.getBase64())
        val hashFiles = FilesService.sha256Checksum(file)
        run creatingFile@{
            val filesDB = filesRepository.findAllByHashFileAndDeletedAtIsNull(hashFiles)
            for (files in filesDB) {
                if (filesPayload.getMime() == files.getMime()) {
                    files.editor = (profileService.getProfileReferenceById((authentication.principal as UserPrincipal).id))
                    files.updatedAt = (Timestamp(System.currentTimeMillis()))
                    files.setName(filesPayload.getFileName())
                    status.value = files.getFilesFullPath(pathFiles) /*+ ":" + fileKey*/
                    status.message = "Successful uploaded"
                    status.status = 1
                    file.deleteOnExit()
                    return@creatingFile
                }
            }

            val files = Files(null)
            files.creator = (profileService.getProfileReferenceById((authentication.principal as UserPrincipal).id))
            files.setFilesFormat(filesPayload.getFileFormat())
            files.setMime(filesPayload.getMime())
            files.setHashFile(hashFiles)
            files.setSize(file.length())
            files.setName(filesPayload.getFileName())
            filesRepository.save(files)
            try {
                status.value = FilesService.createFile(file, pathFiles, files) /*+ ":" + fileKey*/
                status.message = "Successful uploaded"
                status.status = 1
            } catch (ex: FileNotFoundException) {
                status.value = ex.message
                status.message = "File not saved"
                status.status = 0
            }
        }
        return ResponseEntity(status, HttpStatus.OK)
    }


    @PostMapping("request-avatar")
    @PreAuthorize("isAuthenticated()")
    fun getCroppedAvatarAndSave(
        @Valid @RequestBody filesPayload: FilesPayload
        ,authentication: Authentication
    ): ResponseEntity<*> {
        val status = Status()
        val file: File = FilesService.decoder(filesPayload.getBase64())
        val files = filesRepository.getOne(UUID.fromString(filesPayload.getId()))
        try {
            FilesService.createFile(file, pathFiles, files, "thumb_")
            for (quality in qualityCompress) {
                FilesService.imageCompressingAndResizeAndSave(pathFiles, files, quality, "thumb_")
            }
            status.value = files.getFilesFullPath(pathFiles, "thumb_")/*+ ":" + fileKey*/
            status.message = "Successful uploaded"
            status.status = 1
        } catch (ex: FileNotFoundException) {
            status.value = ex.message
            status.message = "File not saved"
            status.status = 0
        }
        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("request-file/{type}")
    @PreAuthorize("isAuthenticated()")
    fun postFile(
        @PathVariable(value = "type") type: String,
        @RequestParam("file") fileForm: MultipartFile,
        @RequestParam("id") id: String?,
        @RequestParam("fieldId") fieldId: String?,
        authentication: Authentication
    ): ResponseEntity<*> {
        val status = Status()
        if (type == "image" && !fileForm.contentType.toString().contains("image/", true)) {
            status.message = "File must image"
            return ResponseEntity(status, HttpStatus.BAD_REQUEST)
        }
        val file: File = FilesService.multipartFileToFile(fileForm, pathFiles)
        val hashFiles = FilesService.sha256Checksum(file)
        val fileType = fileForm.originalFilename!!.substringAfterLast(".")
        val fileName = fileForm.originalFilename!!.substringBeforeLast(".")
        run creatingFile@ {
            val filesDB = filesRepository.findAllByHashFileAndDeletedAtIsNull(hashFiles)
            for (files in filesDB) {
                if (fileForm.contentType == files.getMime()) {
                    files.editor = (profileService.getProfileReferenceById((authentication.principal as UserPrincipal).id))
                    files.updatedAt = (Timestamp(System.currentTimeMillis()))
                    files.setName(fileName)
                    if (!FilesService.filesExists(files.getFilesFullPath(pathFiles))) {
                        FilesService.createFile(file, pathFiles, files)
                    }
                    filesRepository.save(files)
                    if (type == "image") {
                        for (quality in qualityCompress) {
                            FilesService.imageCompressingAndResizeAndSave(pathFiles, files, quality, "thumb_")
                        }
                    }
                    file.delete()
                    status.value = files.getId() /*+ ":" + fileKey*/
                    status.message = "Successful uploaded"
                    status.status = 1
                    return@creatingFile
                }
            }
            val files = Files(null)
            files.creator = (profileService.getProfileReferenceById((authentication.principal as UserPrincipal).id))
            files.setFilesFormat(fileType)
            files.setMime(fileForm.contentType)
            files.setHashFile(hashFiles)
            files.setSize(fileForm.size)
            files.setName(fileName)
            filesRepository.save(files)

            FilesService.createFile(file, pathFiles, files)

            if (type == "image") {
                for (quality in qualityCompress) {
                    FilesService.imageCompressingAndResizeAndSave(pathFiles, files, quality, "thumb_")
                }
            }

            try {
                status.value = files.getId() /*+ ":" + fileKey*/
                status.message = "Successful uploaded"
                status.status = 1

            } catch (ex: FileNotFoundException) {
                status.value = ex.message
                status.message = "File not saved"
                status.status = 0
            }
        }
        return ResponseEntity(status, HttpStatus.OK)
    }
}
