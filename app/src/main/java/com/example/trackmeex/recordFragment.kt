package com.example.trackmeex

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
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
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView


class recordFragment : Fragment()
{
    private lateinit var binding: FragmentRecordBinding
    private lateinit var viewModel: RecordViewModel
    private lateinit var hisSectionViewModel: SectionViewModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mapView: MapView? = null
    private var map: GoogleMap? = null
    private var past_location: Location? = null
    private var current_location: Location? = null

    private lateinit var timer: Chronometer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Binding inflate the layout for this fragment
        binding = DataBindingUtil.inflate(layoutInflater,R.layout.fragment_record, container, false)

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.getWindow()?.attributes?.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN

        buttonsClickmanage()
        viewModelmanage()
        viewModel._mapView.value = binding.mapView
        viewModel._mapView.value!!.onCreate(savedInstanceState)
        viewModel._mapView.value!!.getMapAsync(viewModel)
        viewModel._fusedLocationClient.value = LocationServices.getFusedLocationProviderClient(requireContext())

        timer = binding.chronometer
        timer.setOnChronometerTickListener {
            viewModel._current_timerpoint = ((SystemClock.elapsedRealtime() - timer.base).toFloat())/1000
            binding.timdDisplaytextView.text = viewModel.formattimeDisplay(viewModel._current_timerpoint)
        }
    }
    private fun buttonsClickmanage(){
        binding.stopimageView.setOnClickListener {
            //prepare data
            viewModel.getinsertDataToDatabase()
            val section = Section(0, viewModel._distanceDB!!, viewModel._speedDB!!,  viewModel._timeDB!!,  viewModel._Bitmapstring!!)
            //save data in room
            hisSectionViewModel.addSection(section)
            Toast.makeText(requireContext(), "Successfully add to database", Toast.LENGTH_SHORT).show()
            //navigate back to history
            findNavController().navigate(R.id.action_recordFragment_to_historyFragment)
        }
        binding.pauseimageView.setOnClickListener{
            viewModel._stateOnRecord.value = false
        }
        binding.replayimageView.setOnClickListener{
            viewModel._stateOnRecord.value = true
        }
    }
    @SuppressLint("SetTextI18n")
    private fun viewModelmanage(){
        hisSectionViewModel = ViewModelProvider(this).get(SectionViewModel::class.java)

        viewModel = ViewModelProvider(this).get(RecordViewModel::class.java)
        viewModel._fusedLocationClient.observe(viewLifecycleOwner, Observer {
            fusedLocationClient = viewModel._fusedLocationClient.value!!
        })

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
        viewModel._timer.observe(viewLifecycleOwner, Observer {
            timer = viewModel._timer.value!!
        })
        viewModel._marker.observe(viewLifecycleOwner, Observer {
            viewModel._markerList.add(viewModel._marker.value!!)
            viewModel.zoomfullCourse()
            viewModel._distance_total.value = viewModel._distance_total.value?.plus(
                viewModel._distance)
            viewModel._speed_avg = viewModel._speed_avg.plus(viewModel._speed.value!!)
        })
        viewModel._distance_total.observe(viewLifecycleOwner, Observer {
            binding.distancttextView.text = "$it km"
        })
        viewModel._speed.observe(viewLifecycleOwner, Observer {
            binding.speedtextView.text = "$it km/h"
        })
        viewModel._stateOnRecord.observe(viewLifecycleOwner, Observer {
            if (viewModel._stateOnRecord.value == true){
                binding.pauseimageView.visibility = View.VISIBLE
                binding.replayimageView.visibility = View.INVISIBLE
                binding.stopimageView.visibility = View.INVISIBLE
                viewModel._marker.value?.isVisible = false
                viewModel._timer.value?.base = SystemClock.elapsedRealtime() - 1000*(viewModel._current_timerpoint).toLong()
                timer.start()
            } else {
                //take snapshot - might miss
                map?.snapshot(object : GoogleMap.SnapshotReadyCallback {
                    override fun onSnapshotReady(snapshot: Bitmap?) {
                        if (snapshot != null) {
                            viewModel._Bitmapstring = ImageBitmapString.BitMapToString(snapshot)
                        }
                    }
                })
                binding.pauseimageView.visibility = View.INVISIBLE
                binding.replayimageView.visibility = View.VISIBLE
                binding.stopimageView.visibility = View.VISIBLE
                viewModel._past_location.value = null
                timer.stop()
            }
        })
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
