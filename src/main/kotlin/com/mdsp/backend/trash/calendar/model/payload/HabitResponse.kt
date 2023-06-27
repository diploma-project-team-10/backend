package com.mdsp.backend.trash.calendar.model.payload

import com.fasterxml.jackson.annotation.JsonProperty
import com.mdsp.backend.trash.calendar.model.Habit
import java.util.*

class HabitResponse(habit: Habit) {
    var id: UUID? = null
    @JsonProperty("display_name")
    var title: String? = null
    var description: String? = ""
    var isExists: Boolean = false

    init {
        this.id = habit.id
        this.title = habit.title
        this.description = habit.description
    }
}
