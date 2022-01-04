package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.RemindersData
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SaveReminderViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var fakeDataSource: FakeDataSource

    private lateinit var viewModel: SaveReminderViewModel

    private lateinit var context: Application


    @Before
    fun setUp() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        context = ApplicationProvider.getApplicationContext()

        viewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

    @Test
    fun saveReminder_AndVerify_Loading() = runBlockingTest {
        mainCoroutineRule.pauseDispatcher()

        viewModel.saveReminder(RemindersData.reminderDataItem)

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }


    @Test
    fun saveReminder_AndVerify_Success() {

        viewModel.saveReminder(RemindersData.reminderDataItem)

        assertThat(viewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))
        assertEquals(viewModel.navigationCommand.getOrAwaitValue(), NavigationCommand.Back)
    }


    @Test
    fun onClear_AndVerify_Success() {

        viewModel.latitude.value = RemindersData.reminderDataItem.latitude
        viewModel.longitude.value = RemindersData.reminderDataItem.longitude
        viewModel.reminderDescription.value = RemindersData.reminderDataItem.description
        viewModel.reminderSelectedLocationStr.value = RemindersData.reminderDataItem.location
        viewModel.reminderTitle.value = RemindersData.reminderDataItem.title

        viewModel.onClear()

        assertThat(viewModel.reminderTitle.getOrAwaitValue(), `is`(nullValue()))
        assertEquals(viewModel.reminderDescription.getOrAwaitValue(), null)
        assertEquals(viewModel.reminderSelectedLocationStr.getOrAwaitValue(), null)
        assertEquals(viewModel.selectedPOI.getOrAwaitValue(), null)
        assertEquals(viewModel.latitude.getOrAwaitValue(), null)
        assertEquals(viewModel.longitude.getOrAwaitValue(), null)
    }

    @Test
    fun validateAndSaveReminder_AndVerify_Success() {
        viewModel.validateAndSaveReminder(RemindersData.reminderDataItem)

        assertThat(viewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))
        assertEquals(viewModel.navigationCommand.getOrAwaitValue(), NavigationCommand.Back)
    }

    @Test
    fun validateAndSaveReminder_AndVerify_Error() {
        val reminderData = RemindersData.reminderDataItem.clone()
        reminderData.title = null
        viewModel.validateAndSaveReminder(reminderData)

        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun validateEnteredData_AndVerify_TitleError(){
        val reminderData = RemindersData.reminderDataItem.clone()
        reminderData.title = null
        viewModel.validateEnteredData(reminderData)

        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun validateEnteredData_AndVerify_LocationError(){
        val reminderData = RemindersData.reminderDataItem.clone()
        reminderData.location = null
        viewModel.validateEnteredData(reminderData)

        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }

}