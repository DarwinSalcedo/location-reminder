package com.udacity.project4.locationreminders

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

object RemindersData {
    val listReminders = arrayListOf(
        ReminderDTO(
            "some title",
            "some Description",
            "Shoreline Bay Trailhead",
            37.4324,
            -122.0868
        ),
        ReminderDTO(
            "some title2",
            "some Description2",
            "Rengstorff House",
            37.4315,
            -122.0871
        ),
        ReminderDTO(
            "some title2",
            "some Description2",
            "Google Athletic Recreation Field Park",
            37.4241,
            -122.0881
        )
    )

    val reminderDataItem =
        ReminderDataItem(
            "some title",
            "some Description",
            "Shoreline Bay Trailhead",
            37.4324,
            -122.0868
        )


}