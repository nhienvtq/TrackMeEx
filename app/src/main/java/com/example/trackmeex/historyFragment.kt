package com.example.trackmeex

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trackmeex.data.SectionViewModel
import com.example.trackmeex.databinding.FragmentHistoryBinding
import com.example.trackmeex.decorection.TopPaddingDecoration


class historyFragment : Fragment(){

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var sectionViewModel: SectionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(layoutInflater,R.layout.fragment_history, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recordImageButton.setOnClickListener{ ImageButtonview: View ->
            ImageButtonview.findNavController().navigate(R.id.action_historyFragment_to_recordFragment)
        }
        binding.deleteImageButton.setOnClickListener{
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Delete all history data?")
            builder.setPositiveButton("Delete"){_, _ ->
                sectionViewModel.deleteAllSections()
                Toast.makeText(requireContext(), "all history are deleted", Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton("Cancel"){ _, _  ->}
            builder.create().show()
        }
        initRecyclerView()
    }

    private fun initRecyclerView(){
        val sectionadapter = sectionRecyclerViewAdapter()
        val recyclerView = binding.historyRecyclerView
        recyclerView.adapter = sectionadapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val TopPaddingDecoration = TopPaddingDecoration(30)
        recyclerView.addItemDecoration(TopPaddingDecoration)
        //viewmodel
        sectionViewModel = ViewModelProvider(this).get(SectionViewModel::class.java)
        sectionViewModel.readAllData.observe(viewLifecycleOwner, Observer{
                section -> sectionadapter.setData(section)
        })
    }
}