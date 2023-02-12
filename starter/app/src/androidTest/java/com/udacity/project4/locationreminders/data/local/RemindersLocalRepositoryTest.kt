package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
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

//    TODO: Add testing implementation to the RemindersLocalRepository.kt

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var localRepository: RemindersLocalRepository
    private lateinit var remindersDatabase: RemindersDatabase

    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        localRepository =
            RemindersLocalRepository(remindersDatabase.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDb() = remindersDatabase.close()

    @Test
    fun saveReminder_getReminder_test() = runBlocking {
        //GIVEN - a new reminder saved in the database
        val reminder = ReminderDTO("reminder title", "Reminder desc", "location", 1.0, 1.0)
        localRepository.saveReminder(reminder)
        //WHEN - reminder is retrieved by id
        val result = localRepository.getReminder(reminder.id)


        //THEN - same reminder is returned
        result as Result.Success
        MatcherAssert.assertThat(result.data.id, CoreMatchers.notNullValue())
        MatcherAssert.assertThat(result.data.id, CoreMatchers.`is`(reminder.id))
        MatcherAssert.assertThat(result.data.title, CoreMatchers.`is`(reminder.title))
        MatcherAssert.assertThat(result.data.description, CoreMatchers.`is`(reminder.description))
        MatcherAssert.assertThat(result.data.location, CoreMatchers.`is`(reminder.location))
        MatcherAssert.assertThat(result.data.longitude, CoreMatchers.`is`(reminder.longitude))
        MatcherAssert.assertThat(result.data.latitude, CoreMatchers.`is`(reminder.latitude))
    }

    @Test
    fun saveReminders_delete_all_reminders_test() = runBlocking {
        //GIVEN - 2 new reminders saved in the database
        val reminder1 = ReminderDTO("reminder title1", "Reminder desc1", "location", 1.0, 1.0)
        val reminder2 = ReminderDTO("reminder title2", "Reminder desc2", "location", 1.0, 1.0)
        localRepository.saveReminder(reminder1)
        localRepository.saveReminder(reminder2)
        //WHEN - reminders are deleted then retrieved
        localRepository.deleteAllReminders()
        val result = localRepository.getReminders()
        //THEN - retrieved list is empty
        result as Result.Success
        MatcherAssert.assertThat(result.data.isEmpty(), CoreMatchers.`is`(true))
    }

    @Test
    fun saveReminders_and_get_reminders_test() = runBlocking {
        //GIVEN - 2 new reminders saved in the database
        val reminder1 = ReminderDTO("reminder title1", "Reminder desc1", "location", 1.0, 1.0)
        val reminder2 = ReminderDTO("reminder title2", "Reminder desc2", "location", 1.0, 1.0)
        localRepository.saveReminder(reminder1)
        localRepository.saveReminder(reminder2)
        //WHEN - reminders are retrieved
        val result = localRepository.getReminders()
        //THEN - list with the two reminders are retrieved
//        MatcherAssert.assertThat(result.succeeded, CoreMatchers.`is`(true))
        result as Result.Success
        MatcherAssert.assertThat(result.data.size, CoreMatchers.`is`(2))
    }

    @Test
    fun saveReminder_getReminder_not_in_database_test() = runBlocking {
        //GIVEN - a new reminder saved in the database
        val reminder = ReminderDTO("Reminder title1", "Reminder desc1", "location", 1.0, 1.0)
        localRepository.saveReminder(reminder)
        //WHEN - get reminder with id not saved in the database
        val result = localRepository.getReminder("yxzABC")
        //THEN -error occurred
//        MatcherAssert.assertThat(result.error, CoreMatchers.`is`(true))
        result as Result.Error
        //Checking the message returned is the correct message
        MatcherAssert.assertThat(result.message, CoreMatchers.`is`("Reminder not found!"))
    }

}