package com.mdsp.backend.app.profile.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.mdsp.backend.app.profile.service.ProfileService
import com.mdsp.backend.app.system.model.DateAudit
import com.mdsp.backend.app.system.model.Util
import com.vladmihalcea.hibernate.type.array.StringArrayType
import com.vladmihalcea.hibernate.type.array.UUIDArrayType
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import java.sql.Timestamp
import java.util.*
import javax.persistence.*
import kotlin.collections.ArrayList

@Entity(name = "Profiles")
@Table(name = "profiles")
@JsonIgnoreProperties(value = ["password", "isBlocked", "path"], allowGetters = false)
@TypeDefs(
    TypeDef(
        name = "string-array",
        typeClass = StringArrayType::class
    ),
    TypeDef(
        name = "uuid-array",
        typeClass = UUIDArrayType::class
    ),
    TypeDef(
        name = "jsonb",
        typeClass = JsonBinaryType::class
    )
)
class Profile(): DateAudit() {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private var id: UUID? = null

    @Column(name = "first_name", columnDefinition = "character varying")
    private var firstName: String? = null

    @Column(name = "last_name", columnDefinition = "character varying")
    private var lastName: String? = null

    @Column(name = "middle_name", columnDefinition = "character varying")
    private var middleName: String? = null

    @Column(name = "fio", columnDefinition = "character varying")
    private var fio: String = ""

    @Column(name = "username", unique = true, nullable = false)
    private var username: String? = null

    @Column(name = "email", unique = true)
    private var email: String? = null

    @Column(name = "password", columnDefinition = "character varying")
    private var password: String? = null

    @Column(name = "enabled")
    private var enabled: Boolean? = false

    @Column(name = "birthday")
    private var birthday: Date? = null

    @Column(name = "grants")
    private var grants: Boolean = false

    @Column(name = "phone", columnDefinition = "character varying")
    private var phone: String? = null

    @Column(name = "skills", columnDefinition = "character varying")
    private var skills: String? = null

    @Column(name = "address", columnDefinition = "character varying")
    private var address: String? = null

    @Type(type = "string-array")
    @Column(
        name = "avatar_ref",
        columnDefinition = "character varying(256)[]"
    )
    private var avatarRef: Array<Array<String>> = arrayOf()

    @Column(name = "description", columnDefinition = "TEXT")
    private var description: String? = null

    @Type(type = "string-array")
    @Column(
        name = "university",
        columnDefinition = "character varying(256)[]"
    )
    private var university: Array<Array<String>> = arrayOf()

    @Column(name = "course")
    private var course: Int? = 0

    @Type(type = "string-array")
    @Column(
        name = "edu_degree",
        columnDefinition = "character varying(256)[]"
    )
    private var eduDegree: Array<Array<String>> = arrayOf()

    @Type(type = "string-array")
    @Column(
        name = "speciality",
        columnDefinition = "character varying(256)[]"
    )
    private var speciality: Array<Array<String>> = arrayOf()

    @Type(type = "string-array")
    @Column(
        name = "gender_enum",
        columnDefinition = "character varying(256)[]"
    )
    private var genderEnum: Array<Array<String>> = arrayOf()

    @Type(type = "string-array")
    @Column(
        name = "english_level",
        columnDefinition = "character varying(256)[]"
    )
    @JsonProperty("english_level")
    private var englishLevel: Array<Array<String>> = arrayOf()

    @Type(type = "string-array")
    @Column(
        name = "city",
        columnDefinition = "character varying(256)[]"
    )
    private var city: Array<Array<String>> = arrayOf()

    @Type(type = "string-array")
    @Column(
        name = "attachment",
        columnDefinition = "character varying(256)[]"
    )
    private var attachment: Array<Array<String>> = arrayOf()

    @Type(type = "string-array")
    @Column(
        name = "sertificates",
        columnDefinition = "character varying(256)[]"
    )
    private var sertificates: Array<Array<String>> = arrayOf()

    @Column(
        name = "path",
        columnDefinition = "character varying(256)"
    )
    private var path: String? = ""

    @Column(name = "email_verified")
    private var emailVerified: Boolean? = null

    @Column(name = "login_attempts")
    private var loginAttempts: Int? = 0

    @Column(name = "is_blocked")
    private var isBlocked: Timestamp? = null

    @Column(name = "enable_notification")
    var enableNotification: Boolean? = true
        set(value) {
            field = true
            if (value != null) {
                field = value
            }
        }
        get() {
            if (field == null) {
                return true
            }
            return field
        }

    @Transient
    var roles: String? = ""

    fun getId() = this.id

    fun getFirstName() = this.firstName
    fun setFirstName(firstName: String?) { this.firstName = firstName }

    fun getLastName() = this.lastName
    fun setLastName(lastName: String?) { this.lastName = lastName }

    fun getMiddleName() = this.middleName
    fun setMiddleName(middleName: String?) { this.middleName = middleName }

    fun getFio() = this.fio
    fun setFio(fio: String?) {
        val result: ArrayList<String> = arrayListOf<String>(this.lastName!!, this.firstName!!)
        if (this.middleName != null) {
            result.add(this.middleName!!)
        }
        this.fio = result.joinToString(" ").trim()

    }

    fun getUsername() = this.username
    fun setUsername(username: String?) {
        if (username != null && ProfileService.isUsernameValid(username.lowercase())) {
            this.username = username.lowercase()
        }
    }

    fun getEmail() = this.email
    fun setEmail(email: String?) {
        if (email != null && ProfileService.isEmailValid(email.lowercase())) {
            this.email = email.lowercase()
        }
    }

    fun pwd() = this.password
    fun setPassword(password: String?) { this.password = password }

    fun getEnabled() = this.enabled
    fun setEnabled(enabled: Boolean?) { this.enabled = enabled }

    fun getBirthday() = this.birthday
    fun setBirthday(birthday: Date?) { this.birthday = birthday }

    fun getGrants() = this.grants
    fun setGrants(grants: Boolean?) { this.grants = grants!! }

    fun getPhone() = this.phone
    fun setPhone(phone: String?) { this.phone = phone }

    fun getSkills() = this.skills
    fun setSkills(skills: String?) { this.skills = skills }

    fun getAddress() = this.address
    fun setAddress(address: String?) { this.address = address }

    fun getAvatarRef(type: String = "", size: String = ""): String {
        if (this.avatarRef != null && this.avatarRef.isNotEmpty() && this.avatarRef[0].isNotEmpty()) {
            return "/api/media/file/${this.avatarRef[0][0]}?type=${type}&size=${size}"
        }
        return ""
    }
    fun setAvatarRef(avatarRef: ArrayList<MutableMap<String, Any?>>) {
        this.avatarRef = Util.mapToArray(avatarRef)
    }

    fun getDescription() = this.description
    fun setDescription(description: String?) { this.description = description }

    fun getUniversity() = Util.arrayToMap(this.university)
    fun setUniversity(university: ArrayList<MutableMap<String, Any?>>) {
        this.university = Util.mapToArray(university)
    }

    fun getCourse() = this.course
    fun setCourse(course: Int?) { this.course = course }

    fun getEduDegree() = Util.arrayToMap(this.eduDegree)
    fun setEduDegree(eduDegree: ArrayList<MutableMap<String, Any?>>) {
        this.eduDegree = Util.mapToArray(eduDegree)
    }

    fun getSpeciality() = Util.arrayToMap(this.speciality)
    fun setSpeciality(speciality: ArrayList<MutableMap<String, Any?>>) {
        this.speciality = Util.mapToArray(speciality)
    }

    fun getGenderEnum() = Util.arrayToMap(this.genderEnum)
    fun setGenderEnum(genderEnum: ArrayList<MutableMap<String, Any?>>) {
        this.genderEnum = Util.mapToArray(genderEnum)
    }

    fun getEnglishLevel() = Util.arrayToMap(this.englishLevel)
    fun setEnglishLevel(englishLevel: ArrayList<MutableMap<String, Any?>>) {
        this.englishLevel = Util.mapToArray(englishLevel)
    }

    fun getCity() = Util.arrayToMap(this.city)
    fun setCity(city: ArrayList<MutableMap<String, Any?>>) {
        this.city = Util.mapToArray(city)
    }

    fun getAttachment() = Util.arrayToMap(this.attachment)
    fun setAttachment(attachment: ArrayList<MutableMap<String, Any?>>) {
        this.attachment = Util.mapToArray(attachment)
    }

    fun getSertificates() = Util.arrayToMap(this.sertificates)
    fun setSertificates(sertificates: ArrayList<MutableMap<String, Any?>>) {
        this.sertificates = Util.mapToArray(sertificates)
    }

    fun getPath() = this.path
    fun setPath(path: String) {
        this.path = ""
        if (path != null) {
            this.path = path
        }
    }

    fun getEmailVerified() = this.emailVerified
    fun setEmailVerified(emailVerified: Boolean) { this.emailVerified = emailVerified }

    fun getLoginAttempts(): Int {
        return if (this.loginAttempts != null)
            this.loginAttempts!!
        else 0
    }
    fun setLoginAttempts(loginAttempts: Int) { this.loginAttempts = loginAttempts }

    fun getIsBlocked() = this.isBlocked
    fun setIsBlocked(isBlocked: Timestamp?) { this.isBlocked = isBlocked }

    // ================================================================================
    fun markVerificationConfirmed() { setEmailVerified(true) }
    fun incrementLoginAttempts() { setLoginAttempts(this.getLoginAttempts() + 1) }

}
