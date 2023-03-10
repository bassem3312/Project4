package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver(), CoroutineScope {
    companion object {
        const val TAG = "GeofenceBroadcastRec"
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO
    private val geofenceHelper: GeofenceHelper by lazy { GeofenceHelper() }

    override fun onReceive(context: Context, intent: Intent) {
        geofenceHelper.onReceive(context, intent)

    }

    class GeofenceHelper : KoinComponent {
        private val remindersLocalRepository: ReminderDataSource by inject()

        fun onReceive(context: Context, intent: Intent) {
            Log.e(TAG, "on receiving geofence to location")
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            if (geofencingEvent != null) {
                if (geofencingEvent.hasError()) {
                    val errorMessage = GeofenceStatusCodes
                        .getStatusCodeString(geofencingEvent.errorCode)
                    Log.e(TAG, errorMessage)
                    return
                }

                // Get the transition type.
                val geofenceTransition = geofencingEvent.geofenceTransition

                // Test that the reported transition was of interest.
                if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                    geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
                ) {

                    // Get the geofences that were triggered. A single event can trigger
                    // multiple geofences.

                    val triggeringGeofences = geofencingEvent.triggeringGeofences


                    // Send notification and log the transition details.
                    if (triggeringGeofences != null)
                        sendNotification(
                            context,
                            triggeringGeofences[0].requestId
                        )
//                Log.i(TAG, geofenceTransitionDetails)
                } else {
                    // Log the error.
                    Log.e(
                        TAG, context.getString(
                            R.string.geofence_unknown_error
                        )
                    )
                }
            }
        }

        private fun sendNotification(context: Context, requestId: String) {
            //Get the local repository instance
//        Interaction to the repository has to be through a coroutine scope
            CoroutineScope(coroutineContext).launch(SupervisorJob()) {
                //get the reminder with the request id
                val result = remindersLocalRepository.getReminder(requestId)
                if (result is Result.Success<ReminderDTO>) {
                    val reminderDTO = result.data
                    //send a notification to the user with the reminder details
                    sendNotification(
                        context, ReminderDataItem(
                            reminderDTO.title,
                            reminderDTO.description,
                            reminderDTO.location,
                            reminderDTO.latitude,
                            reminderDTO.longitude,
                            reminderDTO.id
                        )
                    )
                }
            }
        }

    }
}