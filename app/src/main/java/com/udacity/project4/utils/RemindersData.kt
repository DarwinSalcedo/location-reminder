package com.udacity.project4.utils

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

object RemindersData {
    val listReminders = arrayListOf(
        ReminderDTO(
            "test title one",
            "test Description",
            "test location",
            -34.5808186,-58.4972602
        ),
        ReminderDTO(
            "test title two",
            "test Description",
            "test location",
            -34.5808186,-58.4972602
        ),
        ReminderDTO(
            "test title three",
            "test Description",
            "test location",
            -34.5808186,-58.4972602
        )
    )

    val reminderDataItem =
        ReminderDataItem(
            "test title four",
            "test Description",
            "test location",
            -34.5808186,-58.4972602
        )


}