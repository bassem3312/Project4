package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Resources
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.GPSUtils
import com.udacity.project4.utils.displayErrorAlertDialog
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {


    private val TAG = "SelectLocationFragment"
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var Poi: PointOfInterest? = null
    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private var title = ""
    private var isLocationSelected = false

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var mMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        MapsInitializer.initialize(this.requireContext(), MapsInitializer.Renderer.LATEST) {
            Log.e("==error", it.name)
        }
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        initMap()
        binding.btnSaveLocation.setOnClickListener {
            if (lat != 0.0 && lng != 0.0) {
                onLocationSelected()
            } else {
                displayErrorAlertDialog(
                    this.requireActivity(),
                    getString(R.string.select_location),
                    false
                )
            }
        }
//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location

        return binding.root
    }

    private fun initMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(binding.mapSelectLocation.id) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this.requireActivity())

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            enableLocationPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setMapStyle(mMap)
        setPoiClick(mMap)
        setMapLongClick(mMap)
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        _viewModel.reminderTitle.value = title
        _viewModel.latitude.value = lat
        _viewModel.longitude.value = lng
        _viewModel.selectedPOI.value = Poi
        _viewModel.reminderSelectedLocationStr.value = title
        _viewModel.navigationCommand.postValue(NavigationCommand.Back)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun enableLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */

        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Precise location access granted.
                    Log.e("====", "Precise location granted")
                    moveToCurrentLocation()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Only approximate location access granted.
                    Log.e("====", "Only approximate location access granted")
                    moveToCurrentLocation()
                }
                else -> {
                    // No location access granted.
                    displayErrorAlertDialog(
                        this.requireActivity(),
                        getString(R.string.permission_denied_explanation),
                        isShouldFinishActivity = false
                    )

                }
            }
        }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun moveToCurrentLocation() {
        getLocation()
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this.requireContext(), R.raw.map_style
                )
            )
            //Bassem
            if (!success) {
                Log.e("=====", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("========", "Can't find style. Error: ", e)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (GPSUtils(this.requireContext()).isGPSLocationEnabled()) {
            fusedLocationClient.lastLocation.addOnCompleteListener {
                var location = it.result
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    setMapLocation(location)
                } else {
                    Toast.makeText(this.requireContext(), "Location is null ", Toast.LENGTH_LONG)
                        .show()

                }
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    setMapLocation(location)
                } else {
                    Toast.makeText(
                        this.requireContext(), "Location is null ", Toast.LENGTH_LONG
                    ).show()

                }
            }
        } else {


            GPSUtils(this.requireContext()).turnOnGPS()
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener {
            map.setOnMapLongClickListener { latLng ->
                map.clear()
                val snippet = String.format(
                    Locale.getDefault(),
                    "Lat: %1$.5f, Long: %2$.5f",
                    latLng.latitude,
                    latLng.longitude
                )
                map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(getString(R.string.dropped_pin))
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

                )
                lat = latLng.latitude
                lng = latLng.longitude
                title = getString(R.string.dropped_pin)
                isLocationSelected = true


            }
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            var poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
//            poiMarker = null
            poiMarker!!.showInfoWindow()
            Poi = poi
            lat = poi.latLng.latitude
            lng = poi.latLng.longitude
            title = poi.name
            isLocationSelected = true

        }
    }

    @SuppressLint("MissingPermission")
    fun setMapLocation(location: Location) {
        mMap.isMyLocationEnabled = true
//        mMap.moveCamera(CameraUpdateFactory.zoomIn())
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude,location.longitude),5f))
    }
}

