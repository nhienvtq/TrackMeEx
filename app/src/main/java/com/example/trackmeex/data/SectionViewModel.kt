package com.example.trackmeex.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SectionViewModel(application: Application):AndroidViewModel(application) {

    val readAllData: LiveData<List<Section>>
    private val repository: SectionRepository

    init{
        val sectionDAO = SectionDatabase.getDatabase(application).sectionDAO()
        repository = SectionRepository(sectionDAO)
        readAllData = repository.readAllData
    }

    fun addSection(section: Section){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSection(section)
        }
    }

//    fun deleteSection(section: Section){
//        viewModelScope.launch(Dispatchers.IO){
//            repository.deleteSection(section)
//        }
//    }

    fun deleteAllSections(){
        viewModelScope.launch(Dispatchers.IO){
            repository.deleteAllSections()
        }
    }
}