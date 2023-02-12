package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    //executes each task synchronously using architecture component
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    //set the main coroutine dispatcher for unit testing
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    //subject under test
    private lateinit var remindersListViewModel: RemindersListViewModel

    //use a fake repository to be injected into the view model
    private lateinit var reminderFakeDataSource: FakeDataSource

    @Before
    fun initRemindersViewModel() {
        reminderFakeDataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(), reminderFakeDataSource
        )
    }

    @After
    fun end() {
        stopKoin()
    }

    @Test
    fun loadReminders_show_loading_test() {
        //pause dispatcher so you can verify initial values
        mainCoroutineRule.pauseDispatcher()
        //load reminders in the view model
        remindersListViewModel.loadReminders()
        //assert that loading indicator is show
        MatcherAssert.assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(),
            Is.`is`(true)
        )
        //execute pending coroutine functions
        mainCoroutineRule.resumeDispatcher()
        //assert that the loading indicator is hidden
        MatcherAssert.assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(),
            Is.`is`(false)
        )
    }

    fun loadRemindersWhenRemindersAreSaved_returnsReminders_test()=mainCoroutineRule.runBlockingTest {
        //load the live data before adding any reminders
        remindersListViewModel.loadReminders()
        //should return an empty list
        MatcherAssert.assertThat(
            remindersListViewModel.remindersList.getOrAwaitValue().isEmpty(),
            Is.`is`(true)
        )
        //dummy values
        val reminder= ReminderDTO("x","x","x",0.0,0.0)
        reminderFakeDataSource.saveReminder(reminder)
        //load the reminder to the live data
        remindersListViewModel.loadReminders()
        //should return a non-empty list
        MatcherAssert.assertThat(
            remindersListViewModel.remindersList.getOrAwaitValue().isEmpty(),
            Is.`is`(false)
        )
    }

}