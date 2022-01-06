package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.RemindersData
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var repository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        repository =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanUp()= database.close()


    @Test
    fun saveReminder_AndVerify_Insert() = runBlocking {

        val reminderDTO = RemindersData.listReminders[0]

        repository.saveReminder(reminderDTO)
        val reminderResult = repository.getReminder(reminderDTO.id)

        assert(reminderResult is Result.Success)
        reminderResult as Result.Success
        assertThat(reminderResult.data.id, `is`(reminderDTO.id))
        assertThat(reminderResult.data.title, `is`(reminderDTO.title))
        assertThat(reminderResult.data.description, `is`(reminderDTO.description))
        assertThat(reminderResult.data.location, `is`(reminderDTO.location))
        assertThat(reminderResult.data.latitude, `is`(reminderDTO.latitude))
        assertThat(reminderResult.data.longitude, `is`(reminderDTO.longitude))
    }

    @Test
    fun saveReminder_AndReturn_Error() = runBlocking {

        val reminderDTO = RemindersData.listReminders[0]

        val reminderResult = repository.getReminder(reminderDTO.id)

        assert(reminderResult is Result.Error)
        reminderResult as Result.Error
        assertThat(reminderResult.message, `is`("Reminder not found!"))
    }

    @Test
    fun getReminderById_AndVerify_Item() = runBlocking  {

        RemindersData.listReminders.forEach { reminderDTO ->
            repository.saveReminder(reminderDTO)
        }

        val reminderResult = repository.getReminder(RemindersData.listReminders[1].id)

        assert(reminderResult is Result.Success)
        reminderResult as Result.Success
        assertThat(reminderResult.data.id, `is`(RemindersData.listReminders[1].id))
        assertThat(reminderResult.data.title, `is`(RemindersData.listReminders[1].title))
        assertThat(
            reminderResult.data.description,
            `is`(RemindersData.listReminders[1].description)
        )
        assertThat(reminderResult.data.location, `is`(RemindersData.listReminders[1].location))
        assertThat(reminderResult.data.latitude, `is`(RemindersData.listReminders[1].latitude))
        assertThat(reminderResult.data.longitude, `is`(RemindersData.listReminders[1].longitude))
    }

    @Test
    fun deleteAllReminders_AndVerify_noItemOnDB() = runBlocking  {

        RemindersData.listReminders.forEach { reminderDTO ->
            repository.saveReminder(reminderDTO)
        }

        val listRemindersResult = repository.getReminders()

        assert(listRemindersResult is Result.Success)
        listRemindersResult as Result.Success
        assertThat(listRemindersResult.data.size, `is`(RemindersData.listReminders.size))

        repository.deleteAllReminders()

        val listRemindersAfterDelete = repository.getReminders()


        assert(listRemindersAfterDelete is Result.Success)
        listRemindersAfterDelete as Result.Success
        assertThat(listRemindersAfterDelete.data.size, `is`(0))

    }

}