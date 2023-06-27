package com.mdsp.backend.trash.calendar.controller

import com.mdsp.backend.trash.calendar.model.CalendarEvent
import com.mdsp.backend.trash.calendar.model.Habit
import com.mdsp.backend.trash.calendar.model.payload.HabitResponse
import com.mdsp.backend.trash.calendar.repository.ICalendarEventRepository
import com.mdsp.backend.trash.calendar.repository.IHabitRepository
import com.mdsp.backend.app.profile.repository.IProfileRepository
import com.mdsp.backend.app.profile.service.ProfileService
import com.mdsp.backend.app.system.config.FeignUserConfig
import com.mdsp.backend.app.system.model.Status
import com.mdsp.backend.app.system.model.Util
import com.mdsp.backend.app.user.model.UserPrincipal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.sql.Timestamp
import java.util.*
import javax.validation.Valid


@RestController
@RequestMapping("/api/calendar")
class CalendarEventController {
//    comment
    @Autowired
    lateinit var calendarEventRepository: ICalendarEventRepository

    @Autowired
    lateinit var habitRepository: IHabitRepository

    @Autowired
    lateinit var profileService: ProfileService

    @Autowired
    lateinit var profileRepository: IProfileRepository

    @Autowired
    private lateinit var feignConfig: FeignUserConfig

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    fun getCalendarEvents(authentication: Authentication): ArrayList<CalendarEvent> {
        return calendarEventRepository.findAllByProfileIdAndDeletedAtIsNull(
            (authentication.principal as UserPrincipal).id,
            Sort.by(Sort.Direction.ASC, "startDate")
        )
    }

    @PostMapping(value = ["/new/event/{habitId}", "/new/event"])
    @PreAuthorize("isAuthenticated()")
    fun createCalendarEvent(
        @Valid @RequestBody calendarEvent: CalendarEvent,
        @PathVariable(required = false) habitId: UUID?,
        authentication: Authentication
    ): ResponseEntity<*> {
        val status = Status()
        val profileId = (authentication.principal as UserPrincipal).id
        calendarEvent.profileId = profileId
        calendarEventRepository.save(calendarEvent)
        if (habitId != null) {
            val habit = habitRepository.findByIdAndDeletedAtIsNull(habitId)
            val events = habit.get().getCalendarEvent();
            events.add(mutableMapOf("id" to calendarEvent.id, "value" to profileId))
            val list: ArrayList<MutableMap<String, Any?>> = arrayListOf()
            for (item in events.distinct()) {
                list.add(item)
            }
            habit.get().setCalendarEvent(list)
            habitRepository.save(habit.get())
        }
        status.status = 1
        status.message = "Successful"
        return ResponseEntity(status, HttpStatus.OK)
    }

    @DeleteMapping("/remove/event/{id}")
    @PreAuthorize("isAuthenticated()")
    fun removeCalendarEvent(
        @PathVariable id: UUID,
        authentication: Authentication
    ): ResponseEntity<*> {
        val status = Status()
        val profileId = (authentication.principal as UserPrincipal).id
        val calendarEvent = calendarEventRepository.findByIdAndDeletedAtIsNull(id)
        if (calendarEvent.isPresent) {
            calendarEvent.get().deletedAt = (Timestamp(System.currentTimeMillis()))
            calendarEventRepository.save(calendarEvent.get())
            status.status = 1
            status.message = "Successful"
            return ResponseEntity(status, HttpStatus.OK)
        }
        status.status = 0
        status.message = "Not Successful"
        return ResponseEntity(status, HttpStatus.OK)
    }

    /**
     *
     *    HABITS
     *
     * */

    // for front
    @GetMapping("/habits/list")
    @PreAuthorize("isAuthenticated()")
    fun getHabitResponce(authentication: Authentication): ArrayList<HabitResponse> {
        val result: ArrayList<HabitResponse> = arrayListOf()
        val habits = getHabits(authentication)
        for (habit in habits) {
            val response = HabitResponse(habit)
            if(habit.id != null) {
                response.isExists = habitRepository.existsByIdAndDeletedAtIsNull(habit.id!!)
            }
            result.add(response)
        }
        return result
    }

    // for mobile
    @GetMapping("/habit/list")
    @PreAuthorize("isAuthenticated()")
    fun getHabits(authentication: Authentication): ArrayList<Habit> {
        val habits: ArrayList<Habit> = habitRepository.findAllByDeletedAtIsNull(
            Sort.by(Sort.Direction.DESC, "createdAt")
        )
        val profileId = (authentication.principal as UserPrincipal).id.toString()
        habits.forEach { it.isActive = (Util.mapToArray(it.getCalendarEvent())[1].contains(profileId)) }
        for (habit in habits) {
            habit.setCalendarEvent(arrayListOf())
        }
        return habits
    }

    @PostMapping("/new/habit")
    @PreAuthorize("isAuthenticated()")
    fun createHabit(
        @Valid @RequestBody habit: Habit,
        authentication: Authentication
    ): ResponseEntity<*> {
        val status = Status()
        if (habit.id != null) {
            val habit1 = habitRepository.findByIdAndDeletedAtIsNull(habit.id!!)
            if (habit1.isPresent) {
                status.status = 1
                status.message = "Successful Changed"
                status.value = habit
                habit.setCalendarEvent(habit1.get().getCalendarEvent())
                habitRepository.save(habit)
                return ResponseEntity(status, HttpStatus.OK)
            } else {
                status.message = "Not Created"
                return ResponseEntity(status, HttpStatus.OK)
            }
        }
        habit.setCalendarEvent(arrayListOf())
        habitRepository.save(habit)
        status.status = 1
        status.message = "Successful Saved"
        status.value = habit
        return ResponseEntity(status, HttpStatus.OK)
    }

//    @PostMapping("/edit/habitEvent/{id}")
//    @PreAuthorize("isAuthenticated()")
//    fun editHabit(
//        @PathVariable id: UUID,
//        @Valid @RequestBody calendarEvents: ArrayList<MutableMap<String, Any?>>,
//    ): ResponseEntity<*> {
//        val status = Status()
//        val habit = habitRepository.findByIdAndDeletedAtIsNull(id)
//        if (habit.isPresent) {
//            val events = habit.get().getCalendarEvent();
//            events.addAll(calendarEvents)
//            habitRepository.save(habit.get())
//            status.status = 1
//            status.message = "Successful"
//            return ResponseEntity(status, HttpStatus.OK)
//        } else {
//            try {
//
//            } catch (ex: Exception) {
//                println("Habits2: $ex")
//            }
//        }
//        status.status = 0
//        status.message = "Not Successful"
//        return ResponseEntity(status, HttpStatus.OK)
//    }

    @DeleteMapping("/remove/habit/{id}")
    @PreAuthorize("isAuthenticated()")
    fun removeHabit(
        @PathVariable id: UUID,
        authentication: Authentication
    ): ResponseEntity<*> {
        val status = Status()
        val profileId = (authentication.principal as UserPrincipal).id
        val habit = habitRepository.findByIdAndDeletedAtIsNull(id)
        if (habit.isPresent) {
            habit.get().deletedAt = (Timestamp(System.currentTimeMillis()))
            habitRepository.save(habit.get())
            status.status = 1
            status.message = "Successful"
            return ResponseEntity(status, HttpStatus.OK)
        }
        status.status = 0
        status.message = "Not Successful"
        return ResponseEntity(status, HttpStatus.OK)
    }
}
