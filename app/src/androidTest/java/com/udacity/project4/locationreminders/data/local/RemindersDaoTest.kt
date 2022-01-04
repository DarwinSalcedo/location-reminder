package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.RemindersData
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase



    @Before
    fun init() {
        database = Room.inMemoryDatabaseBuilder(
    getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminder_andVerify_insert() = runBlockingTest {

        val reminderDTO = RemindersData.listReminders[0]

        database.reminderDao().saveReminder(reminderDTO)
        val reminderResult = database.reminderDao().getReminderById(reminderDTO.id)


        assertThat(reminderResult?.id, `is`(reminderDTO.id))
        assertThat(reminderResult?.title, `is`(reminderDTO.title))
        assertThat(reminderResult?.description, `is`(reminderDTO.description))
        assertThat(reminderResult?.location, `is`(reminderDTO.location))
        assertThat(reminderResult?.latitude, `is`(reminderDTO.latitude))
        assertThat(reminderResult?.longitude, `is`(reminderDTO.longitude))
    }

    @Test
    fun getReminderById_AndVerify_Item() = runBlockingTest {

        RemindersData.listReminders.forEach { reminderDTO ->
            database.reminderDao().saveReminder(reminderDTO)
        }

        val reminderResult = database.reminderDao().getReminderById(RemindersData.listReminders[1].id)

        assertThat(reminderResult?.id, `is`(RemindersData.listReminders[1].id))
        assertThat(reminderResult?.title, `is`(RemindersData.listReminders[1].title))
        assertThat(reminderResult?.description, `is`(RemindersData.listReminders[1].description))
        assertThat(reminderResult?.location, `is`(RemindersData.listReminders[1].location))
        assertThat(reminderResult?.latitude, `is`(RemindersData.listReminders[1].latitude))
        assertThat(reminderResult?.longitude, `is`(RemindersData.listReminders[1].longitude))
    }

    @Test
    fun deleteAllReminders_AndVerify_noItemOnDB() = runBlockingTest {

        RemindersData.listReminders.forEach { reminderDTO ->
            database.reminderDao().saveReminder(reminderDTO)
        }

        val listRemindersResult = database.reminderDao().getReminders()

        assertThat(listRemindersResult.size, `is`(RemindersData.listReminders.size))

        database.reminderDao().deleteAllReminders()

        val listRemindersAfterDelete = database.reminderDao().getReminders()

        assertThat(listRemindersAfterDelete.size, `is`(0))

    }


}