package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    //TODO: provide testing to the SaveReminderView and its live data objects

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    //set the main coroutine dispatcher for unit testing
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    //subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    //use a fake repository to be injected in the saveReminderViewModel
    private lateinit var reminderFakeDataSource: FakeDataSource

    @Before
    fun initRemindersViewModel() {
        reminderFakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(), reminderFakeDataSource
        )
    }

    @After
    fun end() {
        stopKoin()
    }

    @Test
    fun saveReminder_show_loading_test() {
        val fakeReminder = ReminderDataItem(
            "fake reminder title",
            "fake Reminder description",
            "location",
            0.0,
            0.0
        )
        //pause dispatcher to verify values
        mainCoroutineRule.pauseDispatcher()
        //inert the dummy reminder
        saveReminderViewModel.saveReminder(fakeReminder)
        //assert that loading indicator is shown
        MatcherAssert.assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), Is.`is`(true))
        //execute pending coroutine functions
        mainCoroutineRule.resumeDispatcher()
        //assert that loading indicator is gone
        MatcherAssert.assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(), Is.`is`(false)
        )
    }

    @Test
    fun saveReminder_with_no_title_test() {
        val fakeReminder = ReminderDataItem("", "fake Reminder description", "location", 0.0, 0.0)
        //pause dispatcher to verify values
        mainCoroutineRule.pauseDispatcher()
        //assert that the validation function returns false
        MatcherAssert.assertThat(
            saveReminderViewModel.validateEnteredData(fakeReminder),
            Is.`is`(false)
        )
        //assert that the error message is shown
        MatcherAssert.assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            Is.`is`(R.string.err_enter_title)
        )
        mainCoroutineRule.resumeDispatcher()

    }

    @Test
    fun saveReminder_with_no_location_test() {
        val fakeReminder = ReminderDataItem("Test", "fake Reminder description", "", 0.0, 0.0)
        //pause dispatcher to verify values
        mainCoroutineRule.pauseDispatcher()
        //assert that the validation function returns false
        MatcherAssert.assertThat(
            saveReminderViewModel.validateEnteredData(fakeReminder),
            Is.`is`(false)
        )
        //assert that the error message is shown
        MatcherAssert.assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            Is.`is`(R.string.err_select_location)
        )
        mainCoroutineRule.resumeDispatcher()
    }

    @Test
    fun savaReminderSuccessfully_test() {
        //create fake reminder with all data correct.
        val fakeReminder =
            ReminderDataItem("Test", "fake Reminder description", "Location", 0.0, 0.0)
        //pause dispatcher to verify values
        mainCoroutineRule.pauseDispatcher()
        //assert that the validation function returns true
        MatcherAssert.assertThat(
            saveReminderViewModel.validateEnteredData(fakeReminder),
            Is.`is`(true)
        )

        mainCoroutineRule.resumeDispatcher()

    }

}