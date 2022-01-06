package com.udacity.project4

import android.app.Activity
import android.app.Application
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.RecyclerViewItemCountAssertion
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.EasyMock2Matchers.equalTo
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed

import androidx.test.espresso.matcher.RootMatchers.withDecorView


import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not


@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest :
    KoinTest {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    /*lateinit var decorView: View

    @get:Rule
    val activityScenarioRule: ActivityTestRule<RemindersActivity> =
        ActivityTestRule<RemindersActivity>(RemindersActivity::class.java)*/

    private lateinit var repository: ReminderDataSource
    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var appContext: Application

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                AuthenticationViewModel(appContext)
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()
        viewModel = SaveReminderViewModel(getApplicationContext(), repository)


        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }

       /* activityScenarioRule.getScenario().onActivity(ActivityScenario.ActivityAction {
            decorView = it.getWindow().getDecorView()
        })*/

    }


    @Before
    fun registerIdlingResource(): Unit = IdlingRegistry.getInstance().run {
        register(EspressoIdlingResource.countingIdlingResource)
        register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource(): Unit = IdlingRegistry.getInstance().run {
        unregister(EspressoIdlingResource.countingIdlingResource)
        unregister(dataBindingIdlingResource)
    }

    fun withItemContent(expectedText: String): Matcher<Object> {
        checkNotNull(expectedText)
        return withItemContent(equalTo(expectedText))
    }



    @Test
    fun addReminderSavesReminder_showsSuccessToast() = runBlocking {
        val activityScenario = launchActivity<RemindersActivity>()
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        val reminder = ReminderDTO("title", "description", "location", 0.0, 0.0)
        onView(withId(R.id.reminderTitle)).perform(typeText(reminder.title))
        onView(withId(R.id.reminderDescription)).perform(typeText(reminder.description))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.selectLocation)).perform(click())

        onView(withId(R.id.map)).perform(longClick())
        onView(withId(R.id.fa_confirm)).perform(click())

        onView(withId(R.id.saveReminder)).perform(click())

      // Is toast displayed and is the message correct? //1 way
      //onView(withText(R.string.reminder_saved)).inRoot(ToastMatcher())
          //.check(matches(isDisplayed()))

       // onView(withText(R.string.reminder_saved)) //2 way
          //  .inRoot(withDecorView(IsNot.not((getActivity(activityScenario)?.window?.decorView)))).check(matches(isDisplayed()))

       // onView(withText(R.string.reminder_saved)) //3 way
         //   .inRoot(withDecorView(not((getActivity(activityScenario)?.window?.decorView)))).check(matches(isDisplayed()))


       // onView(withText(R.string.reminder_saved)) //4 way
        // .inRoot(withDecorView(not(decorView))).check(matches(isDisplayed()))

       /** onView(withText(R.string.reminder_saved)).inRoot(
            withDecorView(
                not(
                    `is`(
                        activityScenarioRule.getActivity().getWindow().getDecorView()
                    )
                )
            )
        ).check(
            matches(
                isDisplayed()
            )
        )*/

        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.reminder_saved)))


        onView(withText("title")).check(matches(isDisplayed()))

        onView(withId(R.id.reminderssRecyclerView)).check(RecyclerViewItemCountAssertion(1))

        onView(withId(R.id.reminderssRecyclerView))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        onView(withText("Reminder Title")).check(matches(isDisplayed()))
        onView(withText("title")).check(matches(isDisplayed()))
        onView(withText("Description")).check(matches(isDisplayed()))
        onView(withText("description")).check(matches(isDisplayed()))
        activityScenario.close()
    }

   /* private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }*/
}
