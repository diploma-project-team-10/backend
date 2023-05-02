package com.mdsp.backend.app.files.service

import com.mdsp.backend.app.files.repository.IFilesRepository
import com.mdsp.backend.app.profile.service.ProfileService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.*
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import kotlin.experimental.and
import com.mdsp.backend.app.files.model.Files as FileModel


@Service
class FilesService {

    @Value("\${file.tmp.upload-dir}")
    private val pathFiles: String = ""

    @Autowired
    lateinit var filesRepository: IFilesRepository

    @Autowired
    lateinit var profileService: ProfileService

    private val qualityCompress = arrayListOf("s", "m", "l")

    fun getFilePath(id: UUID, type: String? = "", size: String? = ""): String? {
        val file = filesRepository.findByIdAndDeletedAtIsNull(id)
        if (!file.isPresent) {
            return null
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
            return pathToFile
        }
        return null
    }

    companion object {
        private val LARGE = floatArrayOf(0f, 200f)
        private val MEDIUM = floatArrayOf(0f, 120f)
        private val SMALL = floatArrayOf(0f, 50f)

        fun getSizeInKByte(files: FileModel) = files.getSizeInByte()?.div(1024)
        fun getSizeInMByte(files: FileModel) = getSizeInKByte(files)?.div(1024)

        private fun getFilesPath(pathFiles: String): String {
            val directory: String = pathFiles + SimpleDateFormat("/yyyy/MM/dd").format(Date())
            Files.createDirectories(Paths.get(directory))
            return "$directory/"
        }

        fun filesExists(path: String): Boolean {
            return File(path).exists()
        }

        fun createFile(file: File, pathFiles: String, files: FileModel, type: String = ""): String {
            file.let { sourceFile ->
                if (
                    !filesExists(getFilesPath(pathFiles) + files.getFilesNameDB(type))
                    && getFilesPath(pathFiles) + files.getFilesNameDB(type) == files.getFilesFullPath(pathFiles, type)
                ) {
                    sourceFile.copyTo(File(getFilesPath(pathFiles) + files.getFilesNameDB(type)))
                } else if (!filesExists(files.getFilesFullPath(pathFiles, type))) {
                    sourceFile.copyTo(File(files.getFilesFullPath(pathFiles, type)))
                }
                sourceFile.delete()
            }
            if (!filesExists(files.getFilesFullPath(pathFiles, type)))
                throw FileNotFoundException("File doesn't created")
            return files.getFilesFullPath(pathFiles)
        }

        fun decoder(base64Str: String): File {
            val fileByte = Base64.getDecoder().decode(base64Str)
            val tempFile = File.createTempFile("tempFile", "")
            val fos = FileOutputStream(tempFile)
            fos.write(fileByte)
            return tempFile
        }

        //Checksum Hashing file in hex
        //Format
        //fun nameOfAlgorithmChecksum(file: File): String = getHahOfFile("nameOfAlgorithm", file)
        fun sha256Checksum(file: File): String = getHashOfFiles("SHA-256", file)

        private fun getHashOfFiles(type: String, input: File): String {
            val digest = MessageDigest.getInstance(type)
            val fis = FileInputStream(input)

            val byteArray = ByteArray(1024)
            var bytesCount = 0

            while (fis.read(byteArray).also { bytesCount = it } != -1) {
                digest.update(byteArray, 0, bytesCount)
            }
            fis.close()
            val bytes = digest.digest()

            val sb = StringBuilder()
            for (i in bytes.indices) {
                sb.append(((bytes[i].and(0xff.toByte())) + 0x100).toString(16).substring(1))
            }
            return sb.toString()
        }

        fun imageCompressingAndResizeAndSave(pathFiles: String, image: FileModel, quality: String, type: String = "thumb_") {
            val outputImagePath = image.getFilesFullPath(pathFiles, type, quality)
            val qualityImage = when {
                quality === "l" -> LARGE
                quality === "m" -> MEDIUM
                quality === "s" -> SMALL
                else -> throw Exception("No such quality")
            }

//            imageCompressing(pathFiles, outputImagePath, image, qualityImage[0])
            imageResize(pathFiles,  outputImagePath, image, qualityImage[1].toInt())
            if (!File(outputImagePath).exists())
                throw FileNotFoundException("File doesn't created")
        }

        //Image compressing
        private fun imageCompressing(pathFiles: String, outputImagePath: String, fileModel: FileModel, compressQuality: Float) {

            val ins: InputStream = FileInputStream(File(fileModel.getFilesFullPath(pathFiles)))
            val os: OutputStream = FileOutputStream(File(outputImagePath))

            // create a BufferedImage as the result of decoding the supplied InputStream

            // create a BufferedImage as the result of decoding the supplied InputStream
            val image = ImageIO.read(ins)

            // get all image writers for JPG format

            // get all image writers for JPG format
            val writers = ImageIO.getImageWritersByFormatName(fileModel.getFilesFormat())

            check(writers.hasNext()) { "No writers found" }

            val writer = writers.next()
            val ios = ImageIO.createImageOutputStream(os)
            writer.output = ios

            val param = writer.defaultWriteParam

            // compress to a given quality

            // compress to a given quality
            param.compressionMode = ImageWriteParam.MODE_EXPLICIT
            param.compressionQuality = compressQuality

            // appends a complete image stream containing a single image and
            //associated stream and image metadata and thumbnails to the output

            // appends a complete image stream containing a single image and
            //associated stream and image metadata and thumbnails to the output
            writer.write(null, IIOImage(image, null, null), param)

            // close all streams
            ins.close()
            os.close()
            ios.close()
            writer.dispose()

            //TODO Don't forget to delete and move temp file
        }

        //Image resizing
        private fun imageResize(pathFiles: String, outputImagePath: String, image: FileModel, scaledSize: Int) {

            // reads input image
            var inputImage = ImageIO.read(File(image.getFilesFullPath(pathFiles)))

            // get square size image center
            val imWidth = inputImage.width
            val imHeight = inputImage.height
            var x = 0
            var y = 0
            var wh = imWidth
            if (imWidth > imHeight) {
                x = (imWidth - imHeight) / 2
                wh = imHeight
            } else {
                y = (imHeight - imWidth) / 2
            }
            inputImage = inputImage.getSubimage(x, y, wh, wh)

            var outputImage: BufferedImage? = inputImage
            if (outputImage !== null && outputImage.width > scaledSize) {
                outputImage = resize(inputImage, scaledSize, scaledSize)
            }

            // extracts extension of output file
            val formatName = image.getFilesFormat()
            // writes to output file
//            ImageIO.write(outputImage, formatName, File(outputImagePath))
            ImageIO.write(outputImage, "png", File(outputImagePath))
        }

        @Throws(IOException::class)
        fun multipartFileToFile(
            multipart: MultipartFile,
            dir: String
        ): File {
            val filepath: Path = Paths.get(dir.toString(), "tmp_${UUID.randomUUID()}")
            multipart.transferTo(filepath)
            return File(filepath.toString())
        }

        private fun resize(img: BufferedImage, height: Int, width: Int): BufferedImage? {
            val tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH)
            val resized = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            val g2d = resized.createGraphics()
            g2d.drawImage(tmp, 0, 0, null)
            g2d.dispose()
            return resized
        }

        fun getMimeFromByte(content: ByteArray, default: String = ""): String {
            val isC: InputStream = BufferedInputStream(ByteArrayInputStream(content))
            var mime = URLConnection.guessContentTypeFromStream(isC)
            if (mime.isNullOrEmpty()) {
                return default;
            }
            return mime;
        }

        fun getContentTypeFromMime(contentMime: String?, default: MediaType = MediaType.IMAGE_JPEG): MediaType {
            var contentType: MediaType = default
            if (
                contentMime != null
                && contentMime.isNotEmpty()
                && contentMime.split("/").size == 2
            ) {
                val types = contentMime.split("/")
                contentType = MediaType(types[0], types[1])
            }
            return contentType
        }

        fun formatToMime(key: String): String {
            val data: MutableMap<String, String> = mutableMapOf(
                "123"			to "application/vnd.lotus-1-2-3",
                "3dml"			to "text/vnd.in3d.3dml",
                "3g2"			to "video/3gpp2",
                "3gp"			to "video/3gpp",
                "a"			    to "application/octet-stream",
                "aab"			to "application/x-authorware-bin",
                "aac"			to "audio/x-aac",
                "aam"			to "application/x-authorware-map",
                "aas"			to "application/x-authorware-seg",
                "abw"			to "application/x-abiword",
                "acc"			to "application/vnd.americandynamics.acc",
                "ace"			to "application/x-ace-compressed",
                "acu"			to "application/vnd.acucobol",
                "acutc"		    to "application/vnd.acucorp",
                "adp"			to "audio/adpcm",
                "aep"			to "application/vnd.audiograph",
                "afm"			to "application/x-font-type1",
                "afp"			to "application/vnd.ibm.modcap",
                "ai"			to "application/postscript",
                "aif"			to "audio/x-aiff",
                "aifc"			to "audio/x-aiff",
                "aiff"			to "audio/x-aiff",
                "air"			to "application/vnd.adobe.air-application-installer-package+zip",
                "ami"			to "application/vnd.amiga.ami",
                "apk"			to "application/vnd.android.package-archive",
                "application"	to "application/x-ms-application",
                "apr"			to "application/vnd.lotus-approach",
                "asc"			to "application/pgp-signature",
                "asf"			to "video/x-ms-asf",
                "asm"			to "text/x-asm",
                "aso"			to "application/vnd.accpac.simply.aso",
                "asx"			to "video/x-ms-asf",
                "atc"			to "application/vnd.acucorp",
                "atom"			to "application/atom+xml",
                "atomcat"		to "application/atomcat+xml",
                "atomsvc"		to "application/atomsvc+xml",
                "atx"			to "application/vnd.antix.game-component",
                "au"			to "audio/basic",
                "avi"			to "video/x-msvideo",
                "aw"			to "application/applixware",
                "azf"			to "application/vnd.airzip.filesecure.azf",
                "azs"			to "application/vnd.airzip.filesecure.azs",
                "azw"			to "application/vnd.amazon.ebook",
                "bat"			to "application/x-msdownload",
                "bcpio"		    to "application/x-bcpio",
                "bdf"			to "application/x-font-bdf",
                "bdm"			to "application/vnd.syncml.dm+wbxml",
                "bh2"			to "application/vnd.fujitsu.oasysprs",
                "bin"			to "application/octet-stream",
                "bmi"			to "application/vnd.bmi",
                "bmp"			to "image/bmp",
                "book"			to "application/vnd.framemaker",
                "box"			to "application/vnd.previewsystems.box",
                "boz"			to "application/x-bzip2",
                "bpk"			to "application/octet-stream",
                "btif"			to "image/prs.btif",
                "bz"			to "application/x-bzip",
                "bz2"			to "application/x-bzip2",
                "c"			    to "text/x-c",
                "c4d"			to "application/vnd.clonk.c4group",
                "c4f"			to "application/vnd.clonk.c4group",
                "c4g"			to "application/vnd.clonk.c4group",
                "c4p"			to "application/vnd.clonk.c4group",
                "c4u"			to "application/vnd.clonk.c4group",
                "cab"			to "application/vnd.ms-cab-compressed",
                "car"			to "application/vnd.curl.car",
                "cat"			to "application/vnd.ms-pki.seccat",
                "cc"			to "text/x-c",
                "cct"			to "application/x-director",
                "ccxml"		    to "application/ccxml+xml",
                "cdbcmsg"		to "application/vnd.contact.cmsg",
                "cdf"			to "application/x-netcdf",
                "cdkey"		    to "application/vnd.mediastation.cdkey",
                "cdx"			to "chemical/x-cdx",
                "cdxml"		    to "application/vnd.chemdraw+xml",
                "cdy"			to "application/vnd.cinderella",
                "cer"			to "application/pkix-cert",
                "cgm"			to "image/cgm",
                "chat"			to "application/x-chat",
                "chm"			to "application/vnd.ms-htmlhelp",
                "chrt"			to "application/vnd.kde.kchart",
                "cif"			to "chemical/x-cif",
                "cii"			to "application/vnd.anser-web-certificate-issue-initiation",
                "cil"			to "application/vnd.ms-artgalry",
                "cla"			to "application/vnd.claymore",
                "class"		    to "application/java-vm",
                "clkk"			to "application/vnd.crick.clicker.keyboard",
                "clkp"			to "application/vnd.crick.clicker.palette",
                "clkt"			to "application/vnd.crick.clicker.template",
                "clkw"			to "application/vnd.crick.clicker.wordbank",
                "clkx"			to "application/vnd.crick.clicker",
                "clp"			to "application/x-msclip",
                "cmc"			to "application/vnd.cosmocaller",
                "cmdf"			to "chemical/x-cmdf",
                "cml"			to "chemical/x-cml",
                "cmp"			to "application/vnd.yellowriver-custom-menu",
                "cmx"			to "image/x-cmx",
                "cod"			to "application/vnd.rim.cod",
                "com"			to "application/x-msdownload",
                "conf"			to "text/plain",
                "cpio"			to "application/x-cpio",
                "cpp"			to "text/x-c",
                "cpt"			to "application/mac-compactpro",
                "crd"			to "application/x-mscardfile",
                "crl"			to "application/pkix-crl",
                "crt"			to "application/x-x509-ca-cert",
                "csh"			to "application/x-csh",
                "csml"			to "chemical/x-csml",
                "csp"			to "application/vnd.commonspace",
                "css"			to "text/css",
                "cst"			to "application/x-director",
                "csv"			to "text/csv",
                "cu"			to "application/cu-seeme",
                "curl"			to "text/vnd.curl",
                "cww"			to "application/prs.cww",
                "cxt"			to "application/x-director",
                "cxx"			to "text/x-c",
                "daf"			to "application/vnd.mobius.daf",
                "dataless"		to "application/vnd.fdsn.seed",
                "davmount"		to "application/davmount+xml",
                "dcr"			to "application/x-director",
                "dcurl"		    to "text/vnd.curl.dcurl",
                "dd2"			to "application/vnd.oma.dd2+xml",
                "ddd"			to "application/vnd.fujixerox.ddd",
                "deb"			to "application/x-debian-package",
                "def"			to "text/plain",
                "deploy"		to "application/octet-stream",
                "der"			to "application/x-x509-ca-cert",
                "dfac"			to "application/vnd.dreamfactory",
                "dic"			to "text/x-c",
                "diff"			to "text/plain",
                "dir"			to "application/x-director",
                "dis"			to "application/vnd.mobius.dis",
                "dist"			to "application/octet-stream",
                "distz"		    to "application/octet-stream",
                "djv"			to "image/vnd.djvu",
                "djvu"			to "image/vnd.djvu",
                "dll"			to "application/x-msdownload",
                "dmg"			to "application/octet-stream",
                "dms"			to "application/octet-stream",
                "dna"			to "application/vnd.dna",
                "doc"			to "application/msword",
                "docm"			to "application/vnd.ms-word.document.macroenabled.12",
                "docx"			to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "dot"			to "application/msword",
                "dotm"			to "application/vnd.ms-word.template.macroenabled.12",
                "dotx"			to "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
                "dp"			to "application/vnd.osgi.dp",
                "dpg"			to "application/vnd.dpgraph",
                "dsc"			to "text/prs.lines.tag",
                "dtb"			to "application/x-dtbook+xml",
                "dtd"			to "application/xml-dtd",
                "dts"			to "audio/vnd.dts",
                "dtshd"		    to "audio/vnd.dts.hd",
                "dump"			to "application/octet-stream",
                "dvi"			to "application/x-dvi",
                "dwf"			to "model/vnd.dwf",
                "dwg"			to "image/vnd.dwg",
                "dxf"			to "image/vnd.dxf",
                "dxp"			to "application/vnd.spotfire.dxp",
                "dxr"			to "application/x-director",
                "ecelp4800"		to "audio/vnd.nuera.ecelp4800",
                "ecelp7470"		to "audio/vnd.nuera.ecelp7470",
                "ecelp9600"		to "audio/vnd.nuera.ecelp9600",
                "ecma"			to "application/ecmascript",
                "edm"			to "application/vnd.novadigm.edm",
                "edx"			to "application/vnd.novadigm.edx",
                "efif"			to "application/vnd.picsel",
                "ei6"			to "application/vnd.pg.osasli",
                "elc"			to "application/octet-stream",
                "eml"			to "message/rfc822",
                "emma"			to "application/emma+xml",
                "eol"			to "audio/vnd.digital-winds",
                "eot"			to "application/vnd.ms-fontobject",
                "eps"			to "application/postscript",
                "epub"			to "application/epub+zip",
                "es3"			to "application/vnd.eszigno3+xml",
                "esf"			to "application/vnd.epson.esf",
                "et3"			to "application/vnd.eszigno3+xml",
                "etx"			to "text/x-setext",
                "exe"			to "application/x-msdownload",
                "ext"			to "application/vnd.novadigm.ext",
                "ez"			to "application/andrew-inset",
                "ez2"			to "application/vnd.ezpix-album",
                "ez3"			to "application/vnd.ezpix-package",
                "f"			    to "text/x-fortran",
                "f4v"			to "video/x-f4v",
                "f77"			to "text/x-fortran",
                "f90"			to "text/x-fortran",
                "fbs"			to "image/vnd.fastbidsheet",
                "fdf"			to "application/vnd.fdf",
                "fe_launch"		to "application/vnd.denovo.fcselayout-link",
                "fg5"			to "application/vnd.fujitsu.oasysgp",
                "fgd"			to "application/x-director",
                "fh"			to "image/x-freehand",
                "fh4"			to "image/x-freehand",
                "fh5"			to "image/x-freehand",
                "fh7"			to "image/x-freehand",
                "fhc"			to "image/x-freehand",
                "fig"			to "application/x-xfig",
                "fli"			to "video/x-fli",
                "flo"			to "application/vnd.micrografx.flo",
                "flv"			to "video/x-flv",
                "flw"			to "application/vnd.kde.kivio",
                "flx"			to "text/vnd.fmi.flexstor",
                "fly"			to "text/vnd.fly",
                "fm"			to "application/vnd.framemaker",
                "fnc"			to "application/vnd.frogans.fnc",
                "for"			to "text/x-fortran",
                "fpx"			to "image/vnd.fpx",
                "frame"		    to "application/vnd.framemaker",
                "fsc"			to "application/vnd.fsc.weblaunch",
                "fst"			to "image/vnd.fst",
                "ftc"			to "application/vnd.fluxtime.clip",
                "fti"			to "application/vnd.anser-web-funds-transfer-initiation",
                "fvt"			to "video/vnd.fvt",
                "fzs"			to "application/vnd.fuzzysheet",
                "g3"			to "image/g3fax",
                "gac"			to "application/vnd.groove-account",
                "gdl"			to "model/vnd.gdl",
                "geo"			to "application/vnd.dynageo",
                "gex"			to "application/vnd.geometry-explorer",
                "ggb"			to "application/vnd.geogebra.file",
                "ggt"			to "application/vnd.geogebra.tool",
                "ghf"			to "application/vnd.groove-help",
                "gif"			to "image/gif",
                "gim"			to "application/vnd.groove-identity-message",
                "gmx"			to "application/vnd.gmx",
                "gnumeric"		to "application/x-gnumeric",
                "gph"			to "application/vnd.flographit",
                "gqf"			to "application/vnd.grafeq",
                "gqs"			to "application/vnd.grafeq",
                "gram"			to "application/srgs",
                "gre"			to "application/vnd.geometry-explorer",
                "grv"			to "application/vnd.groove-injector",
                "grxml"		    to "application/srgs+xml",
                "gsf"			to "application/x-font-ghostscript",
                "gtar"			to "application/x-gtar",
                "gtm"			to "application/vnd.groove-tool-message",
                "gtw"			to "model/vnd.gtw",
                "gv"			to "text/vnd.graphviz",
                "gz"			to "application/x-gzip",
                "h"			    to "text/x-c",
                "h261"			to "video/h261",
                "h263"			to "video/h263",
                "h264"			to "video/h264",
                "hbci"			to "application/vnd.hbci",
                "hdf"			to "application/x-hdf",
                "hh"			to "text/x-c",
                "hlp"			to "application/winhlp",
                "hpgl"			to "application/vnd.hp-hpgl",
                "hpid"			to "application/vnd.hp-hpid",
                "hps"			to "application/vnd.hp-hps",
                "hqx"			to "application/mac-binhex40",
                "htke"			to "application/vnd.kenameaapp",
                "htm"			to "text/html",
                "html"			to "text/html",
                "hvd"			to "application/vnd.yamaha.hv-dic",
                "hvp"			to "application/vnd.yamaha.hv-voice",
                "hvs"			to "application/vnd.yamaha.hv-script",
                "icc"			to "application/vnd.iccprofile",
                "ice"			to "x-conference/x-cooltalk",
                "icm"			to "application/vnd.iccprofile",
                "ico"			to "image/x-icon",
                "ics"			to "text/calendar",
                "ief"			to "image/ief",
                "ifb"			to "text/calendar",
                "ifm"			to "application/vnd.shana.informed.formdata",
                "iges"			to "model/iges",
                "igl"			to "application/vnd.igloader",
                "igs"			to "model/iges",
                "igx"			to "application/vnd.micrografx.igx",
                "iif"			to "application/vnd.shana.informed.interchange",
                "imp"			to "application/vnd.accpac.simply.imp",
                "ims"			to "application/vnd.ms-ims",
                "in"			to "text/plain",
                "ipk"			to "application/vnd.shana.informed.package",
                "irm"			to "application/vnd.ibm.rights-management",
                "irp"			to "application/vnd.irepository.package+xml",
                "iso"			to "application/octet-stream",
                "itp"			to "application/vnd.shana.informed.formtemplate",
                "ivp"			to "application/vnd.immervision-ivp",
                "ivu"			to "application/vnd.immervision-ivu",
                "jad"			to "text/vnd.sun.j2me.app-descriptor",
                "jam"			to "application/vnd.jam",
                "jar"			to "application/java-archive",
                "java"			to "text/x-java-source",
                "jisp"			to "application/vnd.jisp",
                "jlt"			to "application/vnd.hp-jlyt",
                "jnlp"			to "application/x-java-jnlp-file",
                "joda"			to "application/vnd.joost.joda-archive",
                "jpe"			to "image/jpeg",
                "jpeg"			to "image/jpeg",
                "jpg"			to "image/jpeg",
                "jpgm"			to "video/jpm",
                "jpgv"			to "video/jpeg",
                "jpm"			to "video/jpm",
                "js"			to "application/javascript",
                "json"			to "application/json",
                "kar"			to "audio/midi",
                "karbon"		to "application/vnd.kde.karbon",
                "kfo"			to "application/vnd.kde.kformula",
                "kia"			to "application/vnd.kidspiration",
                "kil"			to "application/x-killustrator",
                "kml"			to "application/vnd.google-earth.kml+xml",
                "kmz"			to "application/vnd.google-earth.kmz",
                "kne"			to "application/vnd.kinar",
                "knp"			to "application/vnd.kinar",
                "kon"			to "application/vnd.kde.kontour",
                "kpr"			to "application/vnd.kde.kpresenter",
                "kpt"			to "application/vnd.kde.kpresenter",
                "ksh"			to "text/plain",
                "ksp"			to "application/vnd.kde.kspread",
                "ktr"			to "application/vnd.kahootz",
                "ktz"			to "application/vnd.kahootz",
                "kwd"			to "application/vnd.kde.kword",
                "kwt"			to "application/vnd.kde.kword",
                "latex"		    to "application/x-latex",
                "lbd"			to "application/vnd.llamagraphics.life-balance.desktop",
                "lbe"			to "application/vnd.llamagraphics.life-balance.exchange+xml",
                "les"			to "application/vnd.hhe.lesson-player",
                "lha"			to "application/octet-stream",
                "link66"		to "application/vnd.route66.link66+xml",
                "list"			to "text/plain",
                "list3820"		to "application/vnd.ibm.modcap",
                "listafp"		to "application/vnd.ibm.modcap",
                "log"			to "text/plain",
                "lostxml"		to "application/lost+xml",
                "lrf"			to "application/octet-stream",
                "lrm"			to "application/vnd.ms-lrm",
                "ltf"			to "application/vnd.frogans.ltf",
                "lvp"			to "audio/vnd.lucent.voice",
                "lwp"			to "application/vnd.lotus-wordpro",
                "lzh"			to "application/octet-stream",
                "m13"			to "application/x-msmediaview",
                "m14"			to "application/x-msmediaview",
                "m1v"			to "video/mpeg",
                "m2a"			to "audio/mpeg",
                "m2v"			to "video/mpeg",
                "m3a"			to "audio/mpeg",
                "m3u"			to "audio/x-mpegurl",
                "m4u"			to "video/vnd.mpegurl",
                "m4v"			to "video/x-m4v",
                "ma"			to "application/mathematica",
                "mag"			to "application/vnd.ecowin.chart",
                "maker"		    to "application/vnd.framemaker",
                "man"			to "text/troff",
                "mathml"		to "application/mathml+xml",
                "mb"			to "application/mathematica",
                "mbk"			to "application/vnd.mobius.mbk",
                "mbox"			to "application/mbox",
                "mc1"			to "application/vnd.medcalcdata",
                "mcd"			to "application/vnd.mcd",
                "mcurl"		    to "text/vnd.curl.mcurl",
                "mdb"			to "application/x-msaccess",
                "mdi"			to "image/vnd.ms-modi",
                "me"			to "text/troff",
                "mesh"			to "model/mesh",
                "mfm"			to "application/vnd.mfmp",
                "mgz"			to "application/vnd.proteus.magazine",
                "mht"			to "message/rfc822",
                "mhtml"		    to "message/rfc822",
                "mid"			to "audio/midi",
                "midi"			to "audio/midi",
                "mif"			to "application/vnd.mif",
                "mime"			to "message/rfc822",
                "mj2"			to "video/mj2",
                "mjp2"			to "video/mj2",
                "mlp"			to "application/vnd.dolby.mlp",
                "mmd"			to "application/vnd.chipnuts.karaoke-mmd",
                "mmf"			to "application/vnd.smaf",
                "mmr"			to "image/vnd.fujixerox.edmics-mmr",
                "mny"			to "application/x-msmoney",
                "mobi"			to "application/x-mobipocket-ebook",
                "mov"			to "video/quicktime",
                "movie"		    to "video/x-sgi-movie",
                "mp2"			to "audio/mpeg",
                "mp2a"			to "audio/mpeg",
                "mp3"			to "audio/mpeg",
                "mp4"			to "video/mp4",
                "mp4a"			to "audio/mp4",
                "mp4s"			to "application/mp4",
                "mp4v"			to "video/mp4",
                "mpa"			to "video/mpeg",
                "mpc"			to "application/vnd.mophun.certificate",
                "mpe"			to "video/mpeg",
                "mpeg"			to "video/mpeg",
                "mpg"			to "video/mpeg",
                "mpg4"			to "video/mp4",
                "mpga"			to "audio/mpeg",
                "mpkg"			to "application/vnd.apple.installer+xml",
                "mpm"			to "application/vnd.blueice.multipass",
                "mpn"			to "application/vnd.mophun.application",
                "mpp"			to "application/vnd.ms-project",
                "mpt"			to "application/vnd.ms-project",
                "mpy"			to "application/vnd.ibm.minipay",
                "mqy"			to "application/vnd.mobius.mqy",
                "mrc"			to "application/marc",
                "ms"			to "text/troff",
                "mscml"		    to "application/mediaservercontrol+xml",
                "mseed"		    to "application/vnd.fdsn.mseed",
                "mseq"			to "application/vnd.mseq",
                "msf"			to "application/vnd.epson.msf",
                "msh"			to "model/mesh",
                "msi"			to "application/x-msdownload",
                "msl"			to "application/vnd.mobius.msl",
                "msty"			to "application/vnd.muvee.style",
                "mts"			to "model/vnd.mts",
                "mus"			to "application/vnd.musician",
                "musicxml"		to "application/vnd.recordare.musicxml+xml",
                "mvb"			to "application/x-msmediaview",
                "mwf"			to "application/vnd.mfer",
                "mxf"			to "application/mxf",
                "mxl"			to "application/vnd.recordare.musicxml",
                "mxml"			to "application/xv+xml",
                "mxs"			to "application/vnd.triscape.mxs",
                "mxu"			to "video/vnd.mpegurl",
                "n-gage"		to "application/vnd.nokia.n-gage.symbian.install",
                "nb"			to "application/mathematica",
                "nc"			to "application/x-netcdf",
                "ncx"			to "application/x-dtbncx+xml",
                "ngdat"		    to "application/vnd.nokia.n-gage.data",
                "nlu"			to "application/vnd.neurolanguage.nlu",
                "nml"			to "application/vnd.enliven",
                "nnd"			to "application/vnd.noblenet-directory",
                "nns"			to "application/vnd.noblenet-sealer",
                "nnw"			to "application/vnd.noblenet-web",
                "npx"			to "image/vnd.net-fpx",
                "nsf"			to "application/vnd.lotus-notes",
                "nws"			to "message/rfc822",
                "o"			    to "application/octet-stream",
                "oa2"			to "application/vnd.fujitsu.oasys2",
                "oa3"			to "application/vnd.fujitsu.oasys3",
                "oas"			to "application/vnd.fujitsu.oasys",
                "obd"			to "application/x-msbinder",
                "obj"			to "application/octet-stream",
                "oda"			to "application/oda",
                "odb"			to "application/vnd.oasis.opendocument.database",
                "odc"			to "application/vnd.oasis.opendocument.chart",
                "odf"			to "application/vnd.oasis.opendocument.formula",
                "odft"			to "application/vnd.oasis.opendocument.formula-template",
                "odg"			to "application/vnd.oasis.opendocument.graphics",
                "odi"			to "application/vnd.oasis.opendocument.image",
                "odp"			to "application/vnd.oasis.opendocument.presentation",
                "ods"			to "application/vnd.oasis.opendocument.spreadsheet",
                "odt"			to "application/vnd.oasis.opendocument.text",
                "oga"			to "audio/ogg",
                "ogg"			to "audio/ogg",
                "ogv"			to "video/ogg",
                "ogx"			to "application/ogg",
                "onepkg"		to "application/onenote",
                "onetmp"		to "application/onenote",
                "onetoc"		to "application/onenote",
                "onetoc2"		to "application/onenote",
                "opf"			to "application/oebps-package+xml",
                "oprc"			to "application/vnd.palm",
                "org"			to "application/vnd.lotus-organizer",
                "osf"			to "application/vnd.yamaha.openscoreformat",
                "osfpvg"		to "application/vnd.yamaha.openscoreformat.osfpvg+xml",
                "otc"			to "application/vnd.oasis.opendocument.chart-template",
                "otf"			to "application/x-font-otf",
                "otg"			to "application/vnd.oasis.opendocument.graphics-template",
                "oth"			to "application/vnd.oasis.opendocument.text-web",
                "oti"			to "application/vnd.oasis.opendocument.image-template",
                "otm"			to "application/vnd.oasis.opendocument.text-master",
                "otp"			to "application/vnd.oasis.opendocument.presentation-template",
                "ots"			to "application/vnd.oasis.opendocument.spreadsheet-template",
                "ott"			to "application/vnd.oasis.opendocument.text-template",
                "oxt"			to "application/vnd.openofficeorg.extension",
                "p"			    to "text/x-pascal",
                "p10"			to "application/pkcs10",
                "p12"			to "application/x-pkcs12",
                "p7b"			to "application/x-pkcs7-certificates",
                "p7c"			to "application/pkcs7-mime",
                "p7m"			to "application/pkcs7-mime",
                "p7r"			to "application/x-pkcs7-certreqresp",
                "p7s"			to "application/pkcs7-signature",
                "pas"			to "text/x-pascal",
                "pbd"			to "application/vnd.powerbuilder6",
                "pbm"			to "image/x-portable-bitmap",
                "pcf"			to "application/x-font-pcf",
                "pcl"			to "application/vnd.hp-pcl",
                "pclxl"		    to "application/vnd.hp-pclxl",
                "pct"			to "image/x-pict",
                "pcurl"		    to "application/vnd.curl.pcurl",
                "pcx"			to "image/x-pcx",
                "pdb"			to "application/vnd.palm",
                "pdf"			to "application/pdf",
                "pfa"			to "application/x-font-type1",
                "pfb"			to "application/x-font-type1",
                "pfm"			to "application/x-font-type1",
                "pfr"			to "application/font-tdpfr",
                "pfx"			to "application/x-pkcs12",
                "pgm"			to "image/x-portable-graymap",
                "pgn"			to "application/x-chess-pgn",
                "pgp"			to "application/pgp-encrypted",
                "pic"			to "image/x-pict",
                "pkg"			to "application/octet-stream",
                "pki"			to "application/pkixcmp",
                "pkipath"		to "application/pkix-pkipath",
                "pl"			to "text/plain",
                "plb"			to "application/vnd.3gpp.pic-bw-large",
                "plc"			to "application/vnd.mobius.plc",
                "plf"			to "application/vnd.pocketlearn",
                "pls"			to "application/pls+xml",
                "pml"			to "application/vnd.ctc-posml",
                "png"			to "image/png",
                "pnm"			to "image/x-portable-anymap",
                "portpkg"		to "application/vnd.macports.portpkg",
                "pot"			to "application/vnd.ms-powerpoint",
                "potm"			to "application/vnd.ms-powerpoint.template.macroenabled.12",
                "potx"			to "application/vnd.openxmlformats-officedocument.presentationml.template",
                "ppa"			to "application/vnd.ms-powerpoint",
                "ppam"			to "application/vnd.ms-powerpoint.addin.macroenabled.12",
                "ppd"			to "application/vnd.cups-ppd",
                "ppm"			to "image/x-portable-pixmap",
                "pps"			to "application/vnd.ms-powerpoint",
                "ppsm"			to "application/vnd.ms-powerpoint.slideshow.macroenabled.12",
                "ppsx"			to "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
                "ppt"			to "application/vnd.ms-powerpoint",
                "pptm"			to "application/vnd.ms-powerpoint.presentation.macroenabled.12",
                "pptx"			to "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "pqa"			to "application/vnd.palm",
                "prc"			to "application/x-mobipocket-ebook",
                "pre"			to "application/vnd.lotus-freelance",
                "prf"			to "application/pics-rules",
                "ps"			to "application/postscript",
                "psb"			to "application/vnd.3gpp.pic-bw-small",
                "psd"			to "image/vnd.adobe.photoshop",
                "psf"			to "application/x-font-linux-psf",
                "ptid"			to "application/vnd.pvi.ptid1",
                "pub"			to "application/x-mspublisher",
                "pvb"			to "application/vnd.3gpp.pic-bw-var",
                "pwn"			to "application/vnd.3m.post-it-notes",
                "pwz"			to "application/vnd.ms-powerpoint",
                "py"			to "text/x-python",
                "pya"			to "audio/vnd.ms-playready.media.pya",
                "pyc"			to "application/x-python-code",
                "pyo"			to "application/x-python-code",
                "pyv"			to "video/vnd.ms-playready.media.pyv",
                "qam"			to "application/vnd.epson.quickanime",
                "qbo"			to "application/vnd.intu.qbo",
                "qfx"			to "application/vnd.intu.qfx",
                "qps"			to "application/vnd.publishare-delta-tree",
                "qt"			to "video/quicktime",
                "qwd"			to "application/vnd.quark.quarkxpress",
                "qwt"			to "application/vnd.quark.quarkxpress",
                "qxb"			to "application/vnd.quark.quarkxpress",
                "qxd"			to "application/vnd.quark.quarkxpress",
                "qxl"			to "application/vnd.quark.quarkxpress",
                "qxt"			to "application/vnd.quark.quarkxpress",
                "ra"			to "audio/x-pn-realaudio",
                "ram"			to "audio/x-pn-realaudio",
                "rar"			to "application/x-rar-compressed",
                "ras"			to "image/x-cmu-raster",
                "rcprofile"		to "application/vnd.ipunplugged.rcprofile",
                "rdf"			to "application/rdf+xml",
                "rdz"			to "application/vnd.data-vision.rdz",
                "rep"			to "application/vnd.businessobjects",
                "res"			to "application/x-dtbresource+xml",
                "rgb"			to "image/x-rgb",
                "rif"			to "application/reginfo+xml",
                "rl"			to "application/resource-lists+xml",
                "rlc"			to "image/vnd.fujixerox.edmics-rlc",
                "rld"			to "application/resource-lists-diff+xml",
                "rm"			to "application/vnd.rn-realmedia",
                "rmi"			to "audio/midi",
                "rmp"			to "audio/x-pn-realaudio-plugin",
                "rms"			to "application/vnd.jcp.javame.midlet-rms",
                "rnc"			to "application/relax-ng-compact-syntax",
                "roff"			to "text/troff",
                "rpm"			to "application/x-rpm",
                "rpss"			to "application/vnd.nokia.radio-presets",
                "rpst"			to "application/vnd.nokia.radio-preset",
                "rq"			to "application/sparql-query",
                "rs"			to "application/rls-services+xml",
                "rsd"			to "application/rsd+xml",
                "rss"			to "application/rss+xml",
                "rtf"			to "application/rtf",
                "rtx"			to "text/richtext",
                "s"			    to "text/x-asm",
                "saf"			to "application/vnd.yamaha.smaf-audio",
                "sbml"			to "application/sbml+xml",
                "sc"			to "application/vnd.ibm.secure-container",
                "scd"			to "application/x-msschedule",
                "scm"			to "application/vnd.lotus-screencam",
                "scq"			to "application/scvp-cv-request",
                "scs"			to "application/scvp-cv-response",
                "scurl"		    to "text/vnd.curl.scurl",
                "sda"			to "application/vnd.stardivision.draw",
                "sdc"			to "application/vnd.stardivision.calc",
                "sdd"			to "application/vnd.stardivision.impress",
                "sdkd"			to "application/vnd.solent.sdkm+xml",
                "sdkm"			to "application/vnd.solent.sdkm+xml",
                "sdp"			to "application/sdp",
                "sdw"			to "application/vnd.stardivision.writer",
                "see"			to "application/vnd.seemail",
                "seed"			to "application/vnd.fdsn.seed",
                "sema"			to "application/vnd.sema",
                "semd"			to "application/vnd.semd",
                "semf"			to "application/vnd.semf",
                "ser"			to "application/java-serialized-object",
                "setpay"		to "application/set-payment-initiation",
                "setreg"		to "application/set-registration-initiation",
                "sfd-hdstx"		to "application/vnd.hydrostatix.sof-data",
                "sfs"			to "application/vnd.spotfire.sfs",
                "sgl"			to "application/vnd.stardivision.writer-global",
                "sgm"			to "text/sgml",
                "sgml"			to "text/sgml",
                "sh"			to "application/x-sh",
                "shar"			to "application/x-shar",
                "shf"			to "application/shf+xml",
                "si"			to "text/vnd.wap.si",
                "sic"			to "application/vnd.wap.sic",
                "sig"			to "application/pgp-signature",
                "silo"			to "model/mesh",
                "sis"			to "application/vnd.symbian.install",
                "sisx"			to "application/vnd.symbian.install",
                "sit"			to "application/x-stuffit",
                "sitx"			to "application/x-stuffitx",
                "skd"			to "application/vnd.koan",
                "skm"			to "application/vnd.koan",
                "skp"			to "application/vnd.koan",
                "skt"			to "application/vnd.koan",
                "sl"			to "text/vnd.wap.sl",
                "slc"			to "application/vnd.wap.slc",
                "sldm"			to "application/vnd.ms-powerpoint.slide.macroenabled.12",
                "sldx"			to "application/vnd.openxmlformats-officedocument.presentationml.slide",
                "slt"			to "application/vnd.epson.salt",
                "smf"			to "application/vnd.stardivision.math",
                "smi"			to "application/smil+xml",
                "smil"			to "application/smil+xml",
                "snd"			to "audio/basic",
                "snf"			to "application/x-font-snf",
                "so"			to "application/octet-stream",
                "spc"			to "application/x-pkcs7-certificates",
                "spf"			to "application/vnd.yamaha.smaf-phrase",
                "spl"			to "application/x-futuresplash",
                "spot"			to "text/vnd.in3d.spot",
                "spp"			to "application/scvp-vp-response",
                "spq"			to "application/scvp-vp-request",
                "spx"			to "audio/ogg",
                "src"			to "application/x-wais-source",
                "srx"			to "application/sparql-results+xml",
                "sse"			to "application/vnd.kodak-descriptor",
                "ssf"			to "application/vnd.epson.ssf",
                "ssml"			to "application/ssml+xml",
                "stc"			to "application/vnd.sun.xml.calc.template",
                "std"			to "application/vnd.sun.xml.draw.template",
                "stf"			to "application/vnd.wt.stf",
                "sti"			to "application/vnd.sun.xml.impress.template",
                "stk"			to "application/hyperstudio",
                "stl"			to "application/vnd.ms-pki.stl",
                "str"			to "application/vnd.pg.format",
                "stw"			to "application/vnd.sun.xml.writer.template",
                "sus"			to "application/vnd.sus-calendar",
                "susp"			to "application/vnd.sus-calendar",
                "sv4cpio"		to "application/x-sv4cpio",
                "sv4crc"		to "application/x-sv4crc",
                "svd"			to "application/vnd.svd",
                "svg"			to "image/svg+xml",
                "svgz"			to "image/svg+xml",
                "swa"			to "application/x-director",
                "swf"			to "application/x-shockwave-flash",
                "swi"			to "application/vnd.arastra.swi",
                "sxc"			to "application/vnd.sun.xml.calc",
                "sxd"			to "application/vnd.sun.xml.draw",
                "sxg"			to "application/vnd.sun.xml.writer.global",
                "sxi"			to "application/vnd.sun.xml.impress",
                "sxm"			to "application/vnd.sun.xml.math",
                "sxw"			to "application/vnd.sun.xml.writer",
                "t"			    to "text/troff",
                "tao"			to "application/vnd.tao.intent-module-archive",
                "tar"			to "application/x-tar",
                "tcap"			to "application/vnd.3gpp2.tcap",
                "tcl"			to "application/x-tcl",
                "teacher"		to "application/vnd.smart.teacher",
                "tex"			to "application/x-tex",
                "texi"			to "application/x-texinfo",
                "texinfo"		to "application/x-texinfo",
                "text"			to "text/plain",
                "tfm"			to "application/x-tex-tfm",
                "tgz"			to "application/x-gzip",
                "tif"			to "image/tiff",
                "tiff"			to "image/tiff",
                "tmo"			to "application/vnd.tmobile-livetv",
                "torrent"		to "application/x-bittorrent",
                "tpl"			to "application/vnd.groove-tool-template",
                "tpt"			to "application/vnd.trid.tpt",
                "tr"			to "text/troff",
                "tra"			to "application/vnd.trueapp",
                "trm"			to "application/x-msterminal",
                "tsv"			to "text/tab-separated-values",
                "ttc"			to "application/x-font-ttf",
                "ttf"			to "application/x-font-ttf",
                "twd"			to "application/vnd.simtech-mindmapper",
                "twds"			to "application/vnd.simtech-mindmapper",
                "txd"			to "application/vnd.genomatix.tuxedo",
                "txf"			to "application/vnd.mobius.txf",
                "txt"			to "text/plain",
                "u32"			to "application/x-authorware-bin",
                "udeb"			to "application/x-debian-package",
                "ufd"			to "application/vnd.ufdl",
                "ufdl"			to "application/vnd.ufdl",
                "umj"			to "application/vnd.umajin",
                "unityweb"		to "application/vnd.unity",
                "uoml"			to "application/vnd.uoml+xml",
                "uri"			to "text/uri-list",
                "uris"			to "text/uri-list",
                "urls"			to "text/uri-list",
                "ustar"		    to "application/x-ustar",
                "utz"			to "application/vnd.uiq.theme",
                "uu"			to "text/x-uuencode",
                "vcd"			to "application/x-cdlink",
                "vcf"			to "text/x-vcard",
                "vcg"			to "application/vnd.groove-vcard",
                "vcs"			to "text/x-vcalendar",
                "vcx"			to "application/vnd.vcx",
                "vis"			to "application/vnd.visionary",
                "viv"			to "video/vnd.vivo",
                "vor"			to "application/vnd.stardivision.writer",
                "vox"			to "application/x-authorware-bin",
                "vrml"			to "model/vrml",
                "vsd"			to "application/vnd.visio",
                "vsf"			to "application/vnd.vsf",
                "vss"			to "application/vnd.visio",
                "vst"			to "application/vnd.visio",
                "vsw"			to "application/vnd.visio",
                "vtu"			to "model/vnd.vtu",
                "vxml"			to "application/voicexml+xml",
                "w3d"			to "application/x-director",
                "wad"			to "application/x-doom",
                "wav"			to "audio/x-wav",
                "wax"			to "audio/x-ms-wax",
                "wbmp"			to "image/vnd.wap.wbmp",
                "wbs"			to "application/vnd.criticaltools.wbs+xml",
                "wbxml"		    to "application/vnd.wap.wbxml",
                "wcm"			to "application/vnd.ms-works",
                "wdb"			to "application/vnd.ms-works",
                "wiz"			to "application/msword",
                "wks"			to "application/vnd.ms-works",
                "wm"			to "video/x-ms-wm",
                "wma"			to "audio/x-ms-wma",
                "wmd"			to "application/x-ms-wmd",
                "wmf"			to "application/x-msmetafile",
                "wml"			to "text/vnd.wap.wml",
                "wmlc"			to "application/vnd.wap.wmlc",
                "wmls"			to "text/vnd.wap.wmlscript",
                "wmlsc"		    to "application/vnd.wap.wmlscriptc",
                "wmv"			to "video/x-ms-wmv",
                "wmx"			to "video/x-ms-wmx",
                "wmz"			to "application/x-ms-wmz",
                "wpd"			to "application/vnd.wordperfect",
                "wpl"			to "application/vnd.ms-wpl",
                "wps"			to "application/vnd.ms-works",
                "wqd"			to "application/vnd.wqd",
                "wri"			to "application/x-mswrite",
                "wrl"			to "model/vrml",
                "wsdl"			to "application/wsdl+xml",
                "wspolicy"		to "application/wspolicy+xml",
                "wtb"			to "application/vnd.webturbo",
                "wvx"			to "video/x-ms-wvx",
                "x32"			to "application/x-authorware-bin",
                "x3d"			to "application/vnd.hzn-3d-crossword",
                "xap"			to "application/x-silverlight-app",
                "xar"			to "application/vnd.xara",
                "xbap"			to "application/x-ms-xbap",
                "xbd"			to "application/vnd.fujixerox.docuworks.binder",
                "xbm"			to "image/x-xbitmap",
                "xdm"			to "application/vnd.syncml.dm+xml",
                "xdp"			to "application/vnd.adobe.xdp+xml",
                "xdw"			to "application/vnd.fujixerox.docuworks",
                "xenc"			to "application/xenc+xml",
                "xer"			to "application/patch-ops-error+xml",
                "xfdf"			to "application/vnd.adobe.xfdf",
                "xfdl"			to "application/vnd.xfdl",
                "xht"			to "application/xhtml+xml",
                "xhtml"		    to "application/xhtml+xml",
                "xhvml"		    to "application/xv+xml",
                "xif"			to "image/vnd.xiff",
                "xla"			to "application/vnd.ms-excel",
                "xlam"			to "application/vnd.ms-excel.addin.macroenabled.12",
                "xlb"			to "application/vnd.ms-excel",
                "xlc"			to "application/vnd.ms-excel",
                "xlm"			to "application/vnd.ms-excel",
                "xls"			to "application/vnd.ms-excel",
                "xlsb"			to "application/vnd.ms-excel.sheet.binary.macroenabled.12",
                "xlsm"			to "application/vnd.ms-excel.sheet.macroenabled.12",
                "xlsx"			to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "xlt"			to "application/vnd.ms-excel",
                "xltm"			to "application/vnd.ms-excel.template.macroenabled.12",
                "xltx"			to "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
                "xlw"			to "application/vnd.ms-excel",
                "xml"			to "application/xml",
                "xo"			to "application/vnd.olpc-sugar",
                "xop"			to "application/xop+xml",
                "xpdl"			to "application/xml",
                "xpi"			to "application/x-xpinstall",
                "xpm"			to "image/x-xpixmap",
                "xpr"			to "application/vnd.is-xpr",
                "xps"			to "application/vnd.ms-xpsdocument",
                "xpw"			to "application/vnd.intercon.formnet",
                "xpx"			to "application/vnd.intercon.formnet",
                "xsl"			to "application/xml",
                "xslt"			to "application/xslt+xml",
                "xsm"			to "application/vnd.syncml+xml",
                "xspf"			to "application/xspf+xml",
                "xul"			to "application/vnd.mozilla.xul+xml",
                "xvm"			to "application/xv+xml",
                "xvml"			to "application/xv+xml",
                "xwd"			to "image/x-xwindowdump",
                "xyz"			to "chemical/x-xyz",
                "zaz"			to "application/vnd.zzazz.deck+xml",
                "zip"			to "application/zip",
                "zir"			to "application/vnd.zul",
                "zirz"			to "application/vnd.zul",
                "zmm"			to "application/vnd.handheld-entertainment+xml"
            )
            if (data.containsKey(key) && data[key] != null) {
                return data[key].toString()
            }
            return ""
        }
    }
}
