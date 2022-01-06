package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    private val TAG = SelectLocationFragment::class.java.simpleName
    private val REQUEST_PERMISSION_LOCATION = 1


    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var selectedPoi: PointOfInterest


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment =   childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.faConfirm.setOnClickListener {onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {_viewModel.selectedPOI.value = selectedPoi
        _viewModel.latitude.value = selectedPoi.latLng.latitude
        _viewModel.longitude.value = selectedPoi.latLng.longitude
        _viewModel.reminderSelectedLocationStr.value = selectedPoi.name
        _viewModel.navigationCommand.postValue(NavigationCommand.Back)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableLocation()
        setMapLongClick(map)
        setMapStyle(map)
    }

    private fun enableLocation() {
        if ( isPermissionGranted()) {
            Log.d(TAG, "permissions granted, preparing user location")
            map.setMyLocationEnabled(true)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    run {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            val lat = location.latitude
                            val long = location.longitude
                            val currentPosition = LatLng(lat, long)
                              val zoom = 15f
                            map.addMarker(
                                MarkerOptions().position(currentPosition)
                                    .title(getString(R.string.current_position))
                            )
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, zoom))
                        }
                        setPoiClick(map)
                    }
                }

        } else {
            requestLocationPermission()
        }
    }

    /*
    * MAP STYLING FUNCTIONS
    * */
    private fun setPoiClick(map: GoogleMap){
        map.setOnPoiClickListener { poi ->
            selectedPoi = poi
            map.clear()
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker!!.showInfoWindow()
            binding.faConfirm.visibility = View.VISIBLE
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            selectedPoi = PointOfInterest(latLng, snippet, snippet)
            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
            binding.faConfirm.visibility = View.VISIBLE
        }
    }

    private fun setMapStyle(map: GoogleMap){
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.style_map))
            if(!success){
                Log.e(TAG, "Style parsing failed")
            }
        } catch (e:Resources.NotFoundException){
            Log.e(TAG, "Unable to find style. Error: ", e)
        }
    }

    /*
    * LOCATION PERMISSION FUNCTIONS
    * */
    @TargetApi(29)
    private fun isPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    }

    @TargetApi(29 )
    private fun requestLocationPermission() {
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION)

        if(shouldProvideRationale){
            Snackbar.make( binding.root
                , R.string.location_required_error
                , Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.permission_denied_explanation) {
                requestPermissions( permissionsArray, REQUEST_PERMISSION_LOCATION) }
                .setDuration(Snackbar.LENGTH_LONG)
                .show()
        } else {
            requestPermissions(permissionsArray, REQUEST_PERMISSION_LOCATION)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {

        Log.d(TAG, "onRequestPermissionsResult")
        if (grantResults.isEmpty() ||
            grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Snackbar.make(
                binding.faConfirm,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                // Create an action that opens the settings for the specific app
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            enableLocation()
        }
    }
}