package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.RemindersData
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mockito
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class RemindersListViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    private lateinit var dataSource: FakeDataSource

    private lateinit var viewModel: RemindersListViewModel

    @Before
    fun setUp() {
        stopKoin()
        val applicationMock = Mockito.mock(Application::class.java)
        dataSource = FakeDataSource()
        viewModel = RemindersListViewModel(applicationMock, dataSource)
    }

    @Test
    fun loadReminders_AndVerify_ShowLoading() = runBlockingTest {

        mainCoroutineRule.pauseDispatcher()

        viewModel.loadReminders()

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadReminders_AndVerify_Success() = runBlockingTest {

        dataSource.returnError = false
        RemindersData.listReminders.forEach {reminderDTO ->
            dataSource.saveReminder(reminderDTO)
        }

        viewModel.loadReminders()

        assertThat(viewModel.remindersList.getOrAwaitValue().size, `is`(RemindersData.listReminders.size))
    }

    @Test
    fun loadReminders_AndVerify_Error() = runBlockingTest {

        dataSource.returnError = true

        viewModel.loadReminders()

        assertThat(viewModel.showSnackBar.getOrAwaitValue(), `is`("Fake Error get all"))
    }
}