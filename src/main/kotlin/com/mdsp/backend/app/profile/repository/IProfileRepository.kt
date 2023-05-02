package com.mdsp.backend.app.profile.repository

import com.google.firebase.messaging.Notification
import com.mdsp.backend.app.profile.model.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*
import kotlin.collections.ArrayList

interface IProfileRepository: JpaRepository<Profile, UUID> {
    fun findByIdAndDeletedAtIsNull(id: UUID): Optional<Profile>

    fun findAllByDeletedAtIsNull(): ArrayList<Profile>
    fun findAllByDeletedAtIsNullAndCourseIsNotNull(): ArrayList<Profile>

    fun findByUsernameAndDeletedAtIsNull(@Param("username") username: String): Optional<Profile>
    fun findByUsernameIgnoreCaseAndDeletedAtIsNull(@Param("username") username: String): Optional<Profile>

    fun findByUsernameOrEmail(@Param("username") username: String, @Param("email") email: String): ArrayList<Profile>
    fun findByUsernameIgnoreCaseOrEmailIgnoreCase(@Param("username") username: String, @Param("email") email: String): ArrayList<Profile>

    fun findByEmailAndDeletedAtIsNull(@Param("email") email: String): Optional<Profile>
    fun findByEmailIgnoreCaseAndDeletedAtIsNull(@Param("email") email: String): Optional<Profile>

    override fun findById(@Param("id") id: UUID): Optional<Profile>
    fun findByIdAndDeletedAtIsNotNull(@Param("id") id: UUID): Optional<Profile>

    fun findAllByEnableNotificationAndDeletedAtIsNull(enableNotification: Boolean?): ArrayList<Profile>

    @Query(value = "SELECT * FROM profiles as u WHERE deleted_at IS NULL AND " +
            "db_array_key_exists(:city, u.city) = 1 AND id =:id",
        nativeQuery = true)
    fun findByIdAndCityDeletedAtIsNull(city: String, id: UUID): Optional<Profile>

    @Query(value = "SELECT * FROM profiles as u WHERE deleted_at IS NULL AND " +
            "db_array_key_exists(:city, u.city) = 1 ",
            nativeQuery = true)
    fun findAllByCityDeletedAtIsNull(city: String): ArrayList<Profile>


//    @Transactional
//    fun deleteByUsername(@Param("username") username: String)

//    @Transactional
//    @Modifying
//    @Query(value = "UPDATE profiles SET deleted_at = CURRENT_TIMESTAMP WHERE id =:id",
//            nativeQuery = true)
//    fun deletedAt( @Param("id") id: UUID)

//    @Transactional
//    @Modifying
//    @Query(value = "UPDATE profiles SET deleted_at = null WHERE id =:id",
//            nativeQuery = true)
//    fun returnAt( @Param("id") id: UUID)
//
//    fun getAllByDeletedAtIsNull(): List<Profile>
//
//    @Query("SELECT CAST(profiles.id AS character varying), TRIM(CONCAT(profiles.last_name, ' ', profiles.first_name)) AS FIO, \n" +
//            "profiles.gender, profiles.grants, \n" +
//            "edu_type.title, edu_type.address, educations.speciality, educations.course \n" +
//            "FROM profiles \n" +
//            "INNER JOIN educations \n" +
//            "ON profiles.id = educations.profile_id \n" +
//            "INNER JOIN edu_type  \n" +
//            "ON educations.edu_id = edu_type.id \n" +
//            "INNER JOIN users_roles  \n" +
//            "ON profiles.id = users_roles.user_id \n" +
//            "INNER JOIN roles \n" +
//            "ON users_roles.role_id = roles.id \n" +
//            "WHERE roles.name = :role AND profiles.deleted_at IS NULL \n" +
//            "ORDER BY FIO, educations.course, profiles.grants \n", nativeQuery = true)
//    fun getListStudents(@Param("role")  role: String): ArrayList<ArrayList<Any>>
//
//    @Query("SELECT CAST(profiles.id AS character varying), TRIM(CONCAT(profiles.last_name, ' ', profiles.first_name)) AS FIO, \n" +
//            "profiles.gender, profiles.grants \n" +
//            "FROM profiles \n" +
//            "INNER JOIN users_roles  \n" +
//            "ON profiles.id = users_roles.user_id \n" +
//            "INNER JOIN roles \n" +
//            "ON users_roles.role_id = roles.id \n" +
//            "WHERE roles.name = :role AND profiles.deleted_at IS NULL \n" +
//            "ORDER BY FIO, profiles.grants \n", nativeQuery = true)
//    fun getListMentors(@Param("role")  role: String): ArrayList<ArrayList<Any>>
//
//    @Query("SELECT CAST(profiles.id AS character varying), TRIM(CONCAT(profiles.last_name, ' ', profiles.first_name)) \n" +
//            "FROM profiles \n" +
//            "INNER JOIN users_roles  \n" +
//            "ON profiles.id = users_roles.user_id \n" +
//            "INNER JOIN roles \n" +
//            "ON users_roles.role_id = roles.id \n" +
//            "WHERE roles.name = :role AND profiles.deleted_at IS NULL \n" +
//            "AND profiles.id NOT IN :ids \n", nativeQuery = true)
//    fun getListStudentsByRoleOnlyNames(@Param("role")  role: String, @Param("ids")  ids: ArrayList<UUID>): ArrayList<ArrayList<Any>>


    //    @Query("SELECT * FROM profiles AS u \n" +
//            "INNER JOIN users_roles  \n" +
//            "ON u.id = users_roles.user_id \n" +
//            "INNER JOIN roles \n" +
//            "ON users_roles.role_id = roles.id \n" +
//            "WHERE roles.name = :role AND u.deleted_at IS NULL \n" +
//            "ORDER BY u.reads_point DESC, u.id  \n", nativeQuery = true)
//    fun findAllReadersPlace(@Param("role") role: String) : ArrayList<Profile>
//
//    @Query("SELECT * FROM profiles AS u \n" +
//            "INNER JOIN users_roles  \n" +
//            "ON u.id = users_roles.user_id \n" +
//            "INNER JOIN roles \n" +
//            "ON users_roles.role_id = roles.id \n" +
//            "WHERE roles.name = :role AND u.deleted_at IS NULL \n" +
//            "ORDER BY u.reads_point DESC, u.id  \n", nativeQuery = true)
//    fun findAllReaders(@Param("role")  role: String, page: Pageable) : Page<Profile>
//
//    @Query("SELECT * FROM profiles  AS u \n" +
//            "INNER JOIN users_roles  \n" +
//            "ON u.id = users_roles.user_id \n" +
//            "INNER JOIN roles \n" +
//            "ON users_roles.role_id = roles.id \n" +
//            "WHERE u.reads_point >= 750 AND u.reads_finished_books >= 50 \n" +
//            "AND u.reads_reviews_number >= 50 \n" +
//            "AND roles.name = :role AND u.deleted_at IS NULL \n" +
//            "ORDER BY u.reads_point DESC, u.id  \n", nativeQuery = true)
//    fun findGoldReaders(@Param("role")  role: String, page: Pageable): Page<Profile>
//
//    @Query("SELECT * FROM profiles  AS u \n" +
//            "INNER JOIN users_roles  \n" +
//            "ON u.id = users_roles.user_id \n" +
//            "INNER JOIN roles \n" +
//            "ON users_roles.role_id = roles.id \n" +
//            "WHERE u.reads_point >= 300 AND u.reads_finished_books >= 20 \n" +
//            "AND u.reads_reviews_number >= 20 \n" +
//            "AND (u.reads_point < 750 OR u.reads_finished_books < 50 OR u.reads_reviews_number < 50) \n" +
//            "AND roles.name = :role AND u.deleted_at IS NULL \n" +
//            "ORDER BY u.reads_point DESC, u.id  \n", nativeQuery = true)
//    fun findSilverReaders(@Param("role")  role: String, page: Pageable): Page<Profile>
//
//    @Query("SELECT * FROM profiles  AS u \n" +
//            "INNER JOIN users_roles  \n" +
//            "ON u.id = users_roles.user_id \n" +
//            "INNER JOIN roles \n" +
//            "ON users_roles.role_id = roles.id \n" +
//            "WHERE u.reads_point >= 75 AND u.reads_finished_books >= 5 \n" +
//            "AND u.reads_reviews_number >= 5 \n" +
//            "AND (u.reads_point < 300 OR u.reads_finished_books < 20 OR u.reads_reviews_number < 20) \n" +
//            "AND roles.name = :role AND u.deleted_at IS NULL \n" +
//            "ORDER BY u.reads_point DESC, u.id  \n", nativeQuery = true)
//    fun findBronzeReaders(@Param("role")  role: String, page: Pageable): Page<Profile>
//
//    @Query("SELECT * FROM profiles  AS u \n" +
//            "INNER JOIN users_roles  \n" +
//            "ON u.id = users_roles.user_id \n" +
//            "INNER JOIN roles \n" +
//            "ON users_roles.role_id = roles.id \n" +
//            "WHERE u.reads_point >= 0 AND u.reads_finished_books >= 0 \n" +
//            "AND u.reads_reviews_number >= 0 \n" +
//            "AND (u.reads_point < 75 OR u.reads_finished_books < 5 OR u.reads_reviews_number < 5) \n" +
//            "AND roles.name = :role AND u.deleted_at IS NULL \n" +
//            "ORDER BY u.reads_point DESC, u.id  \n", nativeQuery = true)
//    fun findUnratedReaders(@Param("role")  role: String, page: Pageable): Page<Profile>
//
//    @Query("SELECT * FROM profiles  AS u \n" +
//            "INNER JOIN users_roles  \n" +
//            "ON u.id = users_roles.user_id \n" +
//            "INNER JOIN roles \n" +
//            "ON users_roles.role_id = roles.id \n" +
//            "WHERE roles.name = :role AND u.deleted_at IS NULL \n" +
//            "AND (LOWER (u.first_name) LIKE LOWER (:word) \n" +
//            "OR LOWER (u.last_name) LIKE LOWER(:word) \n" +
//            "OR LOWER (u.middle_name) LIKE LOWER(:word)) \n" +
//            "ORDER BY u.reads_point DESC, u.id  \n", nativeQuery = true)
//    fun findAllReadersBySearch(@Param("word")  word: String, @Param("role")  role: String, page: Pageable): Page<Profile>
}
