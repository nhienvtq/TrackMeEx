package com.example.trackmeex

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Chronometer
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.trackmeex.data.Section
import com.example.trackmeex.data.SectionViewModel
import com.example.trackmeex.databinding.FragmentRecordBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil


class recordFragment : Fragment(), OnMapReadyCallback
{
    private lateinit var binding: FragmentRecordBinding
    private lateinit var viewModel: RecordViewModel
    private lateinit var hisSectionViewModel: SectionViewModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var savesnapshot: Bitmap
    private var Bitmapstring: String? = null
    private var mapView: MapView? = null
    private var map: GoogleMap? = null
    private var marker: Marker? = null
    private val markerList: ArrayList<Marker> = ArrayList()
    private var past_location: Location? = null
    private var past_timerpoint: Float? = null
    private var current_location: Location? = null
    private var current_timerpoint: Float? = null

    private var distance_total: Float? = null
    private var distance: Float? = null

    private lateinit var timer: Chronometer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Binding inflate the layout for this fragment
        binding = DataBindingUtil.inflate(layoutInflater,R.layout.fragment_record, container, false)

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.getWindow()?.attributes?.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN
        //set up duration timer
        timer = binding.chronometer
        current_timerpoint = 0f
        timer.setOnChronometerTickListener {
            viewModel._current_timerpoint.value = ((SystemClock.elapsedRealtime() - timer.base).toFloat())/1000
            binding.timdDisplaytextView.text = formattimeDisplay(current_timerpoint!!)
        }
        //Create history viewmodel - Recyclerview
        viewModelmanage()
        //process viewButton action
        buttonsClickmanage()
        //binding and prepare mapview
        viewModel._mapView.value = binding.mapView
        viewModel._mapView.value!!.onCreate(savedInstanceState)
        viewModel._mapView.value!!.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }
    @SuppressLint("SetTextI18n")
    private fun viewModelmanage(){
        hisSectionViewModel = ViewModelProvider(this).get(SectionViewModel::class.java)

        viewModel = ViewModelProvider(this).get(RecordViewModel::class.java)

        viewModel._mapView.observe(viewLifecycleOwner, Observer {
            mapView = viewModel._mapView.value
        })
        viewModel._map.observe(viewLifecycleOwner, Observer {
            map = viewModel._map.value
        })
        viewModel._past_location.observe(viewLifecycleOwner, Observer {
            past_location = viewModel._past_location.value
        })
        viewModel._current_location.observe(viewLifecycleOwner, Observer {
            current_location = viewModel._current_location.value
        })
        viewModel._past_timerpoint.observe(viewLifecycleOwner, Observer {
            past_timerpoint = viewModel._past_timerpoint.value
        })
        viewModel._current_timerpoint.observe(viewLifecycleOwner, Observer {
            current_timerpoint = viewModel._current_timerpoint.value
        })
        viewModel._marker.observe(viewLifecycleOwner, Observer {
            marker = viewModel._marker.value
        })
        viewModel._distance.observe(viewLifecycleOwner, Observer {
            distance = viewModel._distance.value!!
        })
        viewModel._distance_total.observe(viewLifecycleOwner, Observer {
            distance_total = viewModel._distance_total.value!!
            binding.distancttextView.text = ("%.2f".format(it)) + " km"
        })
        viewModel._speed.observe(viewLifecycleOwner, Observer {
            binding.speedtextView.text = "%.2f".format(viewModel._speed.value) + " km/h"
        })
        viewModel._speed_avg.observe(viewLifecycleOwner, Observer {
        })
        viewModel._stateOnRecord.observe(viewLifecycleOwner, Observer {
            if (viewModel._stateOnRecord.value == true){
                binding.pauseimageView.visibility = View.VISIBLE
                binding.replayimageView.visibility = View.INVISIBLE
                binding.stopimageView.visibility = View.INVISIBLE
                viewModel._marker.value?.isVisible = false
                timer.base = SystemClock.elapsedRealtime() - 1000*(current_timerpoint!!).toLong()
                timer.start()
            } else {
                binding.pauseimageView.visibility = View.INVISIBLE
                binding.replayimageView.visibility = View.VISIBLE
                binding.stopimageView.visibility = View.VISIBLE
                viewModel._past_location.value = null
                timer.stop()
            }
        })
        viewModel._stateOnRecord.value = true
    }
    private fun buttonsClickmanage(){
        binding.stopimageView.setOnClickListener {
            //save data
            insertDataToDatabase()
            //navigate back to history
            findNavController().navigate(R.id.action_recordFragment_to_historyFragment)
        }
        binding.pauseimageView.setOnClickListener{
            viewModel._stateOnRecord.value = false
            //zoom to view full course
            zoomfullCourse()
            //take snapshot of full course
            map?.snapshot(object : GoogleMap.SnapshotReadyCallback {
                override fun onSnapshotReady(snapshot: Bitmap?) {
                    if (snapshot != null) {
                        savesnapshot = snapshot
                        Bitmapstring = ImageBitmapString.BitMapToString(savesnapshot)
                    }
                }
            })
        }
        binding.replayimageView.setOnClickListener{
            viewModel._stateOnRecord.value = true
        }
    }
    private fun formattimeDisplay(seconds: Float):String{
        val hour: Int = (seconds/3600).toInt()
        val minute: Int = ((seconds - hour*3600)/60).toInt()
        val second: Int = ((seconds - hour*3600)- minute*60).toInt()

        val hourHH: String
        if (hour < 10){
            hourHH = "0" + hour.toString()
        } else hourHH = hour.toString()
        val minuteMM: String
        if (minute < 10){
            minuteMM = "0" + minute.toString()
        } else minuteMM = minute.toString()
        val secondSS: String
        if (second < 10){
            secondSS = "0" + second.toString()
        } else secondSS = second.toString()
        val timeHHMMSS: String = hourHH + ":" + minuteMM + ":" + secondSS
        return timeHHMMSS
    }
    private fun insertDataToDatabase() {
        //prepare room data
        val distance = "%.2f".format(viewModel._distance_total.value) + " km"
        val time = formattimeDisplay(current_timerpoint!!)
        val speed: String
        if (viewModel.sizespeedList != 0){
            speed = "%.2f".format(viewModel._speed_avg.value?.toFloat()
                ?.div(viewModel.sizespeedList)) + " km/h"
        } else {
            speed = "0.00 km/h"
        }

        //save data in room
        val section = Section(0, distance, speed, time, Bitmapstring!!)
        hisSectionViewModel.addSection(section)
        Toast.makeText(requireContext(), "Successfully add to database", Toast.LENGTH_SHORT).show()
    }
    override fun onMapReady(googleMap: GoogleMap?) {
        viewModel._map.value = googleMap
        viewModel._distance_total.value = 0f
        viewModel._speed.value = 0f
        viewModel._speed_avg.value = 0f
        viewModel._distance.value = 0f
        getLocationAccess()
    }
    private fun getLocationAccess() {
            getLocationUpdates()
            startLocationUpdates()
    }
    private fun getLocationUpdates() {
        locationRequest = LocationRequest()
        locationRequest.interval = 3000
        locationRequest.fastestInterval = 2000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        //call back for location update
        locationCallback = object : LocationCallback() {
            @SuppressLint("SetTextI18n")
            override fun onLocationResult(locationResult: LocationResult) {
                //get valid location
                if (locationResult.locations.isNotEmpty() && viewModel._stateOnRecord.value == true) {
                    viewModel._current_location.value = locationResult.lastLocation
                    val current_latLng = LatLng(viewModel._current_location.value!!.latitude, viewModel._current_location.value!!.longitude)
                    val markerOptions = MarkerOptions().position(current_latLng)
                    if (current_location!= null) {
                        //init first location
                        if (past_location == null){
                            viewModel._past_location.value = viewModel._current_location.value
                            past_timerpoint = 0f
                            viewModel._map.value?.moveCamera(CameraUpdateFactory.newLatLngZoom(current_latLng, 15f))
                            viewModel._marker.value =viewModel._map.value?.addMarker(markerOptions.title("You").icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_BLUE)))
                            markerList.add(viewModel._marker.value!!)
                        }
                        val past_latLng = LatLng( viewModel._past_location.value!!.latitude,  viewModel._past_location.value!!.longitude)
                        //Process distance and speed
                        viewModel._distance.value = (SphericalUtil.computeDistanceBetween(past_latLng,current_latLng)/1000).toFloat()
                        val time_interval = current_timerpoint!! - past_timerpoint!!
                        viewModel._speed.value = (viewModel._distance.value!!/(time_interval/3600))
                        //detect change in location -> update
                        if ((past_latLng != current_latLng)){
                            viewModel._distance_total.value = viewModel._distance_total.value?.plus(
                                viewModel._distance.value!!)
                            viewModel._speed_avg.value = viewModel._speed_avg.value?.plus(viewModel._speed.value!!)
                            viewModel.sizespeedList += 1
                            if (past_timerpoint != 0f){ //implement onpause for hide marker on replay
                                viewModel._marker.value?.isVisible = false
                            }
                            viewModel._marker.value =viewModel._map.value?.addMarker(markerOptions!!.title("You").icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_RED)))
                            markerList.add(viewModel._marker.value!!)
                            zoomfullCourse()
                            val line = viewModel._map.value!!.addPolyline(PolylineOptions()
                                    .add(past_latLng ,current_latLng ).width(5f).color(Color.RED))
                            viewModel._past_location.value = viewModel._current_location.value
                            past_timerpoint = current_timerpoint
                        }
                    }
                }
            }
        }
    }
    private fun zoomfullCourse(){
        //zoom to view full course
        val builder = LatLngBounds.Builder()
        for (marker in markerList) {
            builder.include(marker.position)
            val bounds = builder.build()
            val padding = 200
            viewModel._map.value?.setMaxZoomPreference(18f)
            viewModel._map.value?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
        }
    }
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
    override fun onResume() {
        mapView!!.onResume()
        super.onResume()
    }
    override fun onPause() {
        super.onPause()
        mapView!!.onPause()
    }
    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }
}
