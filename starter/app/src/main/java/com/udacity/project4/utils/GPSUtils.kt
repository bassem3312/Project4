package com.udacity.project4.utils

import android.app.Activity
import android.content.Context
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

/**
 * @author Bassem Mohsen : basem3312@gmail.com on 2/7/2023.
 */
class GPSUtils(context: Context) {
    object DEFAULTS {
        const val GPS_CODE = 1002
    }

    private val TAG = "GPS"
    private val mContext: Context = context


    private var mSettingClient: SettingsClient? = null
    private var mLocationSettingsRequest: LocationSettingsRequest? = null

    private var mLocationManager: LocationManager? = null
    private var mLocationRequest: LocationRequest? = null


    init {
        mLocationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

        mSettingClient = LocationServices.getSettingsClient(mContext)

        mLocationRequest = LocationRequest.create()
        mLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        mLocationRequest?.interval = 1000
        mLocationRequest?.fastestInterval = 500


        if (mLocationRequest != null) {
            val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
            builder.addLocationRequest(mLocationRequest!!)
            mLocationSettingsRequest = builder.build()
        }
    }

    fun turnOnGPS() {
        if (mLocationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
            mLocationSettingsRequest?.let {
                mSettingClient?.checkLocationSettings(it)
                    ?.addOnSuccessListener(mContext as Activity) {
                        Log.d(TAG, "turnOnGPS: Already Enabled")
                    }
                    ?.addOnFailureListener { ex ->
                        if ((ex as ApiException).statusCode
                            == LocationSettingsStatusCodes.RESOLUTION_REQUIRED
                        ) {
                            try {
                                val resolvableApiException = ex as ResolvableApiException
//                                startIntentSenderForResult(mContext,resolvableApiException.resolution.intentSender, DEFAULTS.GPS_CODE, null, 0, 0, 0, null)
                                resolvableApiException.startResolutionForResult(
                                    mContext as Activity,
                                    DEFAULTS.GPS_CODE
                                )

                            } catch (e: Exception) {
                                Log.d(TAG, "turnOnGPS: Unable to start default functionality of GPS")
                            }

                        } else {
                            if (ex.statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                                val errorMessage =
                                    "Location settings are inadequate, and cannot be " +
                                            "fixed here. Fix in Settings."
                                Log.e(TAG, errorMessage)
                                Toast.makeText(
                                    mContext,
                                    errorMessage,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
            }
        }
    }

}