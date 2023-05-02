package com.mdsp.backend.app.profile.controller

import com.mdsp.backend.app.profile.model.*
import com.mdsp.backend.app.profile.repository.*
import com.mdsp.backend.app.reference.repository.IReferenceRepository
import com.mdsp.backend.app.reference.service.RecordNoteService
import com.mdsp.backend.app.system.config.DataSourceConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@RequestMapping("/api")
class ProfileController {

    @Autowired
    lateinit var profileRepository: IProfileRepository

    @Autowired
    lateinit var encoder: PasswordEncoder

    @Autowired
    lateinit var referenceRepository: IReferenceRepository

    @Autowired
    lateinit var recordNoteService: RecordNoteService

    @Autowired
    lateinit var dataSourceConfig: DataSourceConfiguration

    @GetMapping("/profiles/{id}")
    @PreAuthorize("isAuthenticated()")
    fun getPersons(@PathVariable id: UUID): Optional<Profile> {
        var _profile = profileRepository.findByIdAndDeletedAtIsNull(id)
        if (_profile.isPresent) {
            _profile.get().setPassword("")
        }
        return _profile
    }

    @GetMapping("/myprofile")
    @PreAuthorize("isAuthenticated()")
    fun getMyProfile(authentication: Authentication): Optional<Profile> {
        var _profile = profileRepository.findByUsernameAndDeletedAtIsNull(authentication.name)
        if (_profile.isPresent) {
            _profile.get().setPassword("")
        }
        return _profile
    }

//    @GetMapping("/reads/user-place/{id}")
//    @PreAuthorize("isAuthenticated()")
//    fun getReadersPlace(@PathVariable(value = "id") userId: UUID): ResponseEntity<*> {
//        val readsUsers: ArrayList<Profile> = profileRepository.findAllReadersPlace("ROLE_STUDENT")
//        var counter = 0
//
//        var profileCandidate = profileRepository.findByIdAndDeletedAtIsNull(userId);
//
//        if(profileCandidate.isPresent) {
//            for (readsUser in readsUsers) {
//                counter++;
//                if (readsUser.getId() == userId) {
//                    return ResponseEntity(counter, HttpStatus.OK)
//                }
//            }
//            return ResponseEntity("ADMIN", HttpStatus.OK)
//        }
//        return ResponseEntity("NOT FOUND", HttpStatus.OK)
//    }
//
//    @GetMapping("/reads/user-list/{page}")
//    @PreAuthorize("isAuthenticated()")
//    fun getReadersList(@PathVariable(value = "page") page: Int): ResponseEntity<*> {
//        val page: PageRequest1 = org.springframework.data.domain.PageRequest.of(page - 1, 20)
//        val readsUsers: Page<Profile> = profileRepository.findAllReaders("ROLE_STUDENT", page)
//        var readersList: ArrayList<Profile.ReadsUsers> = arrayListOf()
//        for(readsUser in readsUsers) {
//            var st = Profile.ReadsUsers(
//                    readsUser.getId().toString(),
//                    readsUser.getFirstName(),
//                    readsUser.getLastName(),
//                    readsUser.getMiddleName(),
//                    readsUser.getReadsFinishedBooks(),
//                    readsUser.getReadsReviewNumber(),
//                    readsUser.getReadsPoint(),
//                    readsUser.getGroupId().toString(),
//                    readsUser.getAvatar(),
//                    readsUser.getReadsRecommendation(),
//                    readsUser.getEmail(),
//                    readsUser.getPhone(),
//                    readsUser.getGender()
//            )
//            readersList.add(st)
//        }
//        return ResponseEntity(readersList, HttpStatus.OK)
//    }
//
//    @GetMapping("/reads/admin-list/{page}")
//    @PreAuthorize("isAuthenticated()")
//    fun getAdminReadersList(@PathVariable(value = "page") page: Int): ResponseEntity<*> {
//        val page: PageRequest1 = org.springframework.data.domain.PageRequest.of(page - 1, 20)
//        val readsAdmins: Page<Profile> = profileRepository.findAllReaders("ROLE_READS_MENTOR", page)
//        var readersList: ArrayList<Profile.ReadsUsers> = arrayListOf()
//
//        for(readsUser in readsAdmins) {
//            var st = Profile.ReadsUsers(
//                    readsUser.getId().toString(),
//                    readsUser.getFirstName(),
//                    readsUser.getLastName(),
//                    readsUser.getMiddleName(),
//                    readsUser.getReadsFinishedBooks(),
//                    readsUser.getReadsReviewNumber(),
//                    readsUser.getReadsPoint(),
//                    readsUser.getGroupId().toString(),
//                    readsUser.getAvatar(),
//                    readsUser.getReadsRecommendation(),
//                    readsUser.getEmail(),
//                    readsUser.getPhone(),
//                    readsUser.getGender()
//            )
//            readersList.add(st)
//        }
//        return ResponseEntity(readersList, HttpStatus.OK)
//    }

//    @RequestMapping(value = ["/reads/user-list/{page}/search"], method = [RequestMethod.GET])
//    @PreAuthorize("isAuthenticated()")
//    fun getReadersListSearch(@PathVariable(value = "page") page: Int, @RequestParam("word") word: String): ResponseEntity<*>{
//        if(word.length >= 3) {
//            val page: PageRequest1 = org.springframework.data.domain.PageRequest.of(page - 1, 20)
//            var profilesCandidate: Page<Profile> = profileRepository.findAllReadersBySearch("%$word%", "ROLE_STUDENT", page)
//            var readersList: ArrayList<Profile.ReadsUsers> = arrayListOf()
//
//            for (readsUser in profilesCandidate) {
//                var st = Profile.ReadsUsers(
//                        readsUser.getId().toString(),
//                        readsUser.getFirstName(),
//                        readsUser.getLastName(),
//                        readsUser.getMiddleName(),
//                        readsUser.getReadsFinishedBooks(),
//                        readsUser.getReadsReviewNumber(),
//                        readsUser.getReadsPoint(),
//                        readsUser.getGroupId().toString(),
//                        readsUser.getAvatar(),
//                        readsUser.getReadsRecommendation(),
//                        readsUser.getEmail(),
//                        readsUser.getPhone(),
//                        readsUser.getGender()
//                )
//                readersList.add(st)
//            }
//
//            return ResponseEntity(readersList, HttpStatus.OK)
//        } else {
//            return ResponseEntity("Word length is smaller than 3", HttpStatus.BAD_REQUEST)
//        }
//    }
//
//    @GetMapping("/reads/gold-user-list/{page}")
//    @PreAuthorize("isAuthenticated()")
//    fun getGoldReadersList(@PathVariable(value = "page") page: Int): ResponseEntity<*> {
//        val page: org.springframework.data.domain.PageRequest = org.springframework.data.domain.PageRequest.of(page - 1, 20)
//        val readsUsers: Page<Profile> = profileRepository.findGoldReaders("ROLE_STUDENT", page)
//        var goldReadersList: ArrayList<Profile.ReadsUsers> = arrayListOf()
//
//        for(goldReadsUser in readsUsers) {
//            var st = Profile.ReadsUsers(
//                    goldReadsUser.getId().toString(),
//                    goldReadsUser.getFirstName(),
//                    goldReadsUser.getLastName(),
//                    goldReadsUser.getMiddleName(),
//                    goldReadsUser.getReadsFinishedBooks(),
//                    goldReadsUser.getReadsReviewNumber(),
//                    goldReadsUser.getReadsPoint(),
//                    goldReadsUser.getGroupId().toString(),
//                    goldReadsUser.getAvatar(),
//                    goldReadsUser.getReadsRecommendation(),
//                    goldReadsUser.getEmail(),
//                    goldReadsUser.getPhone(),
//                    goldReadsUser.getGender()
//            )
//            goldReadersList.add(st)
//        }
//        return ResponseEntity(goldReadersList, HttpStatus.OK)
//    }
//
//    @GetMapping("/reads/silver-user-list/{page}")
//    @PreAuthorize("isAuthenticated()")
//    fun getSilverReadersList(@PathVariable(value = "page") page: Int): ResponseEntity<*> {
//        val page: org.springframework.data.domain.PageRequest = org.springframework.data.domain.PageRequest.of(page - 1, 20)
////        print(profileRepository.findSilverReaders())
//        val readsUsers: Page<Profile> = profileRepository.findSilverReaders("ROLE_STUDENT", page)
//        var silverReadersList: ArrayList<Profile.ReadsUsers> = arrayListOf()
//
//        for(goldReadsUser in readsUsers) {
//            var st = Profile.ReadsUsers(
//                    goldReadsUser.getId().toString(),
//                    goldReadsUser.getFirstName(),
//                    goldReadsUser.getLastName(),
//                    goldReadsUser.getMiddleName(),
//                    goldReadsUser.getReadsFinishedBooks(),
//                    goldReadsUser.getReadsReviewNumber(),
//                    goldReadsUser.getReadsPoint(),
//                    goldReadsUser.getGroupId().toString(),
//                    goldReadsUser.getAvatar(),
//                    goldReadsUser.getReadsRecommendation(),
//                    goldReadsUser.getEmail(),
//                    goldReadsUser.getPhone(),
//                    goldReadsUser.getGender()
//            )
//            silverReadersList.add(st)
//        }
//        return ResponseEntity(silverReadersList, HttpStatus.OK)
//    }
//
//    @GetMapping("/reads/bronze-user-list/{page}")
//    @PreAuthorize("isAuthenticated()")
//    fun getBronzeReadersList(@PathVariable(value = "page") page: Int): ResponseEntity<*> {
//        val page: org.springframework.data.domain.PageRequest = org.springframework.data.domain.PageRequest.of(page - 1, 20)
//        val readsUsers: Page<Profile> = profileRepository.findBronzeReaders("ROLE_STUDENT", page);
//        var bronzeReadersList: ArrayList<Profile.ReadsUsers> = arrayListOf()
//
//        for(goldReadsUser in readsUsers) {
//            var st = Profile.ReadsUsers(
//                    goldReadsUser.getId().toString(),
//                    goldReadsUser.getFirstName(),
//                    goldReadsUser.getLastName(),
//                    goldReadsUser.getMiddleName(),
//                    goldReadsUser.getReadsFinishedBooks(),
//                    goldReadsUser.getReadsReviewNumber(),
//                    goldReadsUser.getReadsPoint(),
//                    goldReadsUser.getGroupId().toString(),
//                    goldReadsUser.getAvatar(),
//                    goldReadsUser.getReadsRecommendation(),
//                    goldReadsUser.getEmail(),
//                    goldReadsUser.getPhone(),
//                    goldReadsUser.getGender()
//            )
//            bronzeReadersList.add(st)
//        }
//        return ResponseEntity(bronzeReadersList, HttpStatus.OK)
//    }
//
//    @GetMapping("/reads/unrated-user-list/{page}")
//    @PreAuthorize("isAuthenticated()")
//    fun getUnratedReadersList(@PathVariable(value = "page") page: Int): ResponseEntity<*> {
//        val page: org.springframework.data.domain.PageRequest = org.springframework.data.domain.PageRequest.of(page - 1, 20)
//        val readsUsers: Page<Profile> = profileRepository.findUnratedReaders("ROLE_STUDENT", page);
//        var unratedReadersList: ArrayList<Profile.ReadsUsers> = arrayListOf()
//
//        for(goldReadsUser in readsUsers) {
//            var st = Profile.ReadsUsers(
//                    goldReadsUser.getId().toString(),
//                    goldReadsUser.getFirstName(),
//                    goldReadsUser.getLastName(),
//                    goldReadsUser.getMiddleName(),
//                    goldReadsUser.getReadsFinishedBooks(),
//                    goldReadsUser.getReadsReviewNumber(),
//                    goldReadsUser.getReadsPoint(),
//                    goldReadsUser.getGroupId().toString(),
//                    goldReadsUser.getAvatar(),
//                    goldReadsUser.getReadsRecommendation(),
//                    goldReadsUser.getEmail(),
//                    goldReadsUser.getPhone(),
//                    goldReadsUser.getGender()
//            )
//            unratedReadersList.add(st)
//        }
//        return ResponseEntity(unratedReadersList, HttpStatus.OK)
//    }
//
//    @GetMapping("/reads/group-user-list/{id}")
//    @PreAuthorize("isAuthenticated()")
//    fun getGroupReadersList(@PathVariable(value = "id") id: UUID): ResponseEntity<*> {
//        val groupUsers: ArrayList<Profile> = profileRepository.findAllByReadsGroupIdAndDeletedAtIsNullOrderByReadsPointDescReadsFinishedBooksDesc(id)
//        var groupReadersList: ArrayList<Profile.ReadsUsers> = arrayListOf()
//
//        for(groupUser in groupUsers) {
//            var st = Profile.ReadsUsers(
//                    groupUser.getId().toString(),
//                    groupUser.getFirstName(),
//                    groupUser.getLastName(),
//                    groupUser.getMiddleName(),
//                    groupUser.getReadsFinishedBooks(),
//                    groupUser.getReadsReviewNumber(),
//                    groupUser.getReadsPoint(),
//                    groupUser.getGroupId().toString(),
//                    groupUser.getAvatar(),
//                    groupUser.getReadsRecommendation(),
//                    groupUser.getEmail(),
//                    groupUser.getPhone(),
//                    groupUser.getGender()
//            )
//            groupReadersList.add(st)
//        }
//        return ResponseEntity(groupReadersList, HttpStatus.OK)
//    }
//
//    @GetMapping("/profiles/roles/readsmentorlist")
//    @PreAuthorize("hasRole('ADMIN')")
//    @ResponseBody
//    fun getReadsMentorDetails(): ArrayList<Profile.ReadsMentors> {
//        var mentors = profileRepository.getListMentors("ROLE_READS_MENTOR")
//        var mentorList: ArrayList<Profile.ReadsMentors> = arrayListOf()
//
//        for (_mentor in mentors) {
//            var st = Profile.ReadsMentors(
//                    id = _mentor[0].toString(),
//                    fio = _mentor[1].toString(),
//                    gender = _mentor[2].toString().toInt()
//            )
//            mentorList.add(st)
//        }
//        return mentorList
//    }

}
