package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest :KoinTest{

//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.

    private lateinit var repository: ReminderDataSource
    //execute each task synchronously using architecture component
    @get:Rule
    private var instantTaskExecutorRule= InstantTaskExecutorRule()
    @Before
    fun init(){
        //stop koin
        stopKoin()
        //create koin module
        val myModule= module {
            viewModel {
                RemindersListViewModel(
                    getApplicationContext(),
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    getApplicationContext(),
                    get() as ReminderDataSource
                )
            }
            single {
                RemindersLocalRepository(get()) as ReminderDataSource
            }
            single {
                LocalDB.createRemindersDao(getApplicationContext())
            }
        }
        //start koin with the created module
        startKoin {
            modules(listOf(myModule))
        }
        //get real repository
        repository=get()
        //clear data
        runBlocking {
            repository.deleteAllReminders()
        }
    }
    @Test
    fun clickOnAddReminderButton_NavigateToSaveReminderFragment()
    {
        //  GIVEN - on the reminders List screen
        val scenario= launchFragmentInContainer<ReminderListFragment>(Bundle(),R.style.AppTheme)
        val navController= mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!,navController)
        }
        //WHEN - click on add reminder button
        onView(withId(R.id.addReminderFAB)).perform(click())
        //THEN - verify that we navigate to save reminder fragment
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }
    //    TODO: test the displayed data on the UI.
    @Test
    fun saveReminder_DisplayInUI()
    {
        //dummy reminder
        val reminder= ReminderDTO("Youssef","Mohamed","Rashed",1.0,1.0)
        //save the reminder
        runBlocking {
            repository.saveReminder(reminder)
        }
        //  GIVEN - on the reminders List screen
        val scenario= launchFragmentInContainer<ReminderListFragment>(Bundle(),R.style.AppTheme)
        //  THEN - verify data displayed
        onView(withId(R.id.title)).check(ViewAssertions.matches(ViewMatchers.withText(reminder.title)))
        onView(withId(R.id.description)).check(ViewAssertions.matches(ViewMatchers.withText(reminder.description)))
        onView(withId(R.id.tv_location_poi_name)).check(
            ViewAssertions.matches(
                ViewMatchers.withText(
                    reminder.location
                )
            )
        )
    }
    //    TODO: add testing for the error messages.
    @Test
    fun noReminders_CheckUI()
    {
        //Delete all reminders
        runBlocking {
            repository.deleteAllReminders()
        }
        //  GIVEN - on the reminders List screen
        val scenario= launchFragmentInContainer<ReminderListFragment>(Bundle(),R.style.AppTheme)
        //  THEN - no data textview is displayed
        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

}