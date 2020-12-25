package com.example.trackmeex

import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.Marker


class RecordViewModel: ViewModel()
{

    var sizespeedList: Int = 0


    val _distance_total: MutableLiveData<Float> by lazy{ MutableLiveData<Float>()}
    val _distance: MutableLiveData<Float> by lazy{ MutableLiveData<Float>()}
    val _speed_avg: MutableLiveData<Float> by lazy{ MutableLiveData<Float>()}
    val _speed: MutableLiveData<Float> by lazy{ MutableLiveData<Float>()}
    val _past_timerpoint: MutableLiveData<Float> by lazy{ MutableLiveData<Float>()}
    val _current_timerpoint: MutableLiveData<Float> by lazy{ MutableLiveData<Float>()}
    val _mapView: MutableLiveData<MapView> by lazy{MutableLiveData<MapView>()}
    val _map: MutableLiveData<GoogleMap> by lazy{MutableLiveData<GoogleMap>()}
    val _past_location: MutableLiveData<Location> by lazy{MutableLiveData<Location>()}
    val _current_location: MutableLiveData<Location> by lazy{MutableLiveData<Location>()}
    val _marker: MutableLiveData<Marker> by lazy{MutableLiveData<Marker>()}
    val _stateOnRecord: MutableLiveData<Boolean> by lazy{MutableLiveData<Boolean>()}

    override fun onCleared() {
        super.onCleared()
        Log.i("flowTag", "RecordViewModel Cleared")
    }
}