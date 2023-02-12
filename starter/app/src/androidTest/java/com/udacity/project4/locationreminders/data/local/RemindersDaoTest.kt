package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    //    TODO: Add testing implementation to the RemindersDao.kt
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var remindersDatabase: RemindersDatabase

    @Before
    fun initDb() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = remindersDatabase.close()

    @Test
    fun saveReminder_and_get_by_id_test() = runTest {
        //GIVEN - insert a reminder
        val reminder = ReminderDTO("reminder title", "reminder description", "location", 1.0, 2.0)
        remindersDatabase.reminderDao().saveReminder(reminder)
        //WHEN - get the reminders from the database
        val loaded = remindersDatabase.reminderDao().getReminderById(reminder.id)
        //THEN - the loaded data contains the expected values
        assertThat<ReminderDTO>(loaded as ReminderDTO, CoreMatchers.notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.longitude, `is`(reminder.longitude))
        assertThat(loaded.latitude, `is`(reminder.latitude))
    }

    @Test
    fun saveReminders_and_delete_test() = runTest {
        //GIVEN - insert 2 reminders
        val reminder1 = ReminderDTO("reminder title", "reminder description", "location", 1.0, 2.0)
        val reminder2 = ReminderDTO("reminder title", "reminder description", "location", 1.0, 2.0)
        remindersDatabase.reminderDao().saveReminder(reminder1)
        remindersDatabase.reminderDao().saveReminder(reminder2)
        //WHEN - delete the reminders from the database and get them
        remindersDatabase.reminderDao().deleteAllReminders()
        val savedList = remindersDatabase.reminderDao().getReminders()
        //THEN - the loaded list is empty
        assertThat(savedList.isEmpty(), CoreMatchers.`is`(true))
    }
}