package com.example.trackmeex

import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Location
import android.widget.Chronometer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil


class RecordViewModel: ViewModel(), OnMapReadyCallback
{

    val _fusedLocationClient: MutableLiveData<FusedLocationProviderClient> by lazy{ MutableLiveData<FusedLocationProviderClient>()}
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    var sizespeedList: Int = 0
    var _Bitmapstring: String? = null
    var _distance: Float = 0f
    var _speed_avg: Float = 0f
    var _past_timerpoint: Float = 0f
    var _current_timerpoint: Float = 0f

    val _timer: MutableLiveData<Chronometer> by lazy{ MutableLiveData<Chronometer>()}
    val _distance_total: MutableLiveData<Float> by lazy{ MutableLiveData<Float>()}
    val _speed: MutableLiveData<Float> by lazy{ MutableLiveData<Float>()}
    val _mapView: MutableLiveData<MapView> by lazy{MutableLiveData<MapView>()}
    val _map: MutableLiveData<GoogleMap> by lazy{MutableLiveData<GoogleMap>()}
    val _past_location: MutableLiveData<Location> by lazy{MutableLiveData<Location>()}
    val _current_location: MutableLiveData<Location> by lazy{MutableLiveData<Location>()}
    val _marker: MutableLiveData<Marker> by lazy{MutableLiveData<Marker>()}
    val _markerList: ArrayList<Marker> = ArrayList()
    val _stateOnRecord: MutableLiveData<Boolean> by lazy{MutableLiveData<Boolean>()}

    init {
        _past_location.value = null
        _current_location.value = null
        _current_timerpoint = 0f
        _past_timerpoint = 0f
        _stateOnRecord.value = true
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        _map.value = googleMap
        _distance_total.value = 0f
        _speed.value = 0f
        _speed_avg = 0f
        _distance = 0f

        //set default showing position
        val current_latLng = LatLng(37.4149, -122.1490)
        _map.value?.animateCamera(CameraUpdateFactory.newLatLngZoom(current_latLng, 10f))
        getLocationUpdates()
        startLocationUpdates()
    }
    private fun getLocationUpdates() {
        locationRequest = LocationRequest()
        //update each 3s
        locationRequest.interval = 3000 //mili sec
        locationRequest.fastestInterval = 2000 //mili sec
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        //call back for location update
        locationCallback = object : LocationCallback() {

            @SuppressLint("SetTextI18n")
            override fun onLocationResult(locationResult: LocationResult) {
                //get valid location
                if (locationResult.locations.isNotEmpty() && _stateOnRecord.value == true) {
                    _current_location.value = locationResult.lastLocation
                    val current_latLng = LatLng(_current_location.value!!.latitude, _current_location.value!!.longitude)
                    val markerOptions = MarkerOptions().position(current_latLng)
                    if (_current_location.value != null) {
                        //init first location
                        if (_past_location.value == null) {
                            _past_location.value = _current_location.value
                            _map.value?.moveCamera(CameraUpdateFactory.newLatLngZoom(current_latLng,15f))
                            _marker.value = _map.value?.addMarker(markerOptions.title("You").icon(
                                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
                        }
                        val past_latLng = LatLng( _past_location.value!!.latitude,  _past_location.value!!.longitude)
                        //distance - time - speed processing
                        _distance = ("%.2f".format((SphericalUtil.computeDistanceBetween(past_latLng,current_latLng)/1000))).toFloat()
                        val time_interval = ("%.2f".format(_current_timerpoint - _past_timerpoint)).toFloat()
                        _speed.value = ("%.2f".format(_distance/(time_interval/3600))).toFloat()
                        //detect location change and update
                        if ((past_latLng != current_latLng)){
                            _distance_total.value = _distance_total.value?.plus(
                                _distance)
                            _speed_avg = _speed_avg.plus(_speed.value!!)
                            sizespeedList += 1
                            //hide former markers except start point
                            if (_past_timerpoint != 0f) _marker.value?.isVisible = false
                            //add marker
                            _marker.value = _map.value?.addMarker(markerOptions!!.title("You").icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_RED)))
                            _markerList.add(_marker.value!!)
                            //draw path
                            val line = _map.value!!.addPolyline(PolylineOptions()
                                .add(past_latLng ,current_latLng ).width(5f).color(Color.RED))
                            _past_location.value = _current_location.value
                            _past_timerpoint = _current_timerpoint
                        }
                    }
                }
            }

        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        _fusedLocationClient.value?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    fun zoomfullCourse(){
        //zoom to view full course
        val builder = LatLngBounds.Builder()
        for (marker in _markerList) {
            builder.include(marker.position)
            val bounds = builder.build()
            val padding = 200
            _map.value?.setMaxZoomPreference(15f)
            _map.value?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
        }
    }

    fun formattimeDisplay(seconds: Float):String{
        val hour: Int = (seconds/3600).toInt()
        val minute: Int = ((seconds - hour*3600)/60).toInt()
        val second: Int = ((seconds - hour*3600)- minute*60).toInt()

        val hourHH: String
        hourHH = if (hour < 10){"0$hour"} else hour.toString()
        val minuteMM: String
        minuteMM = if (minute < 10){"0$minute"} else minute.toString()
        val secondSS: String
        secondSS = if (second < 10){"0$second"} else second.toString()
        return hourHH + ":" + minuteMM + ":" + secondSS
    }

    var _distanceDB: String? = null
    var _timeDB: String? = null
    var _speedDB: String? = null
    fun getinsertDataToDatabase() {
        //prepare room data
        _distanceDB = (_distance_total.value).toString() + " km"
        _timeDB = formattimeDisplay(_current_timerpoint)
        _speedDB = if (sizespeedList != 0){
            (_speed_avg.div(sizespeedList)).toString() + " km/h"
        } else {
            "0.00 km/h"
        }
    }
}