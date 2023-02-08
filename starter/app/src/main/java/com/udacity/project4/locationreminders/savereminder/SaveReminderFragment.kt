package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.displayErrorAlertDialog
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SaveReminderFragment : BaseFragment() {
    private var isBackgroungLocationPermissionEnabled: Boolean = false

    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private var locationPermissionRequest: ActivityResultLauncher<String>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            if (it) {
                isBackgroungLocationPermissionEnabled = true
                saveReminderAndCreateGeofence()
            } else {
                isBackgroungLocationPermissionEnabled = false
                displayErrorAlertDialog(
                    this.requireActivity(),
                    getString(R.string.permission_denied_explanation),
                    isShouldFinishActivity = false
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            if (!isBackgroungLocationPermissionEnabled)
                enableBackGroundPermission()
            else {
                saveReminderAndCreateGeofence()


            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    private fun enableBackGroundPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            locationPermissionRequest?.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    /**
    TODO: use the user entered reminder details to:
    1) add a geofencing request
    2) save the reminder to the local db
     */
    private fun saveReminderAndCreateGeofence() {

        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value
        val geofenceId = UUID.randomUUID().toString()

        if (latitude != null && longitude != null && !TextUtils.isEmpty(title))
            addGeofence(LatLng(latitude, longitude), geofenceId)

        _viewModel.validateAndSaveReminder(
            ReminderDataItem(
                title,
                description,
                location,
                latitude,
                longitude
            )
        )
    }


    @SuppressLint("MissingPermission")
    private fun addGeofence(
        latLng: LatLng,
        geofenceId: String
    ) {
        val geofence = Geofence.Builder()
            .setRequestId(geofenceId)
            .setCircularRegion(
                latLng.latitude,
                latLng.longitude,
                Companion.GEOFENCE_RADIUS
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val client = LocationServices.getGeofencingClient(requireContext())

        client.addGeofences(request, pendingIntent)?.run {
            addOnSuccessListener {
                Log.d(TAG, "Added geofence. Reminder has id $geofenceId .")
            }
            addOnFailureListener { e ->
                val errorMessage: String? = e.localizedMessage
                Toast.makeText(
                    context,
                    "Please give background location permission",
                    Toast.LENGTH_LONG
                ).show()
                Log.d(TAG, "fail in creating geofence: $errorMessage")
            }
        }
    }

    companion object {
        private const val TAG = "SaveReminderFragment"
        const val GEOFENCE_RADIUS = 500f
        const val ACTION_GEOFENCE_EVENT = "ACTION_GEOFENCE_EVENT"

    }
}
