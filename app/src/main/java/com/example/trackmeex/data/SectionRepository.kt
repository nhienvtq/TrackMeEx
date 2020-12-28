package com.example.trackmeex.data

import androidx.lifecycle.LiveData

class SectionRepository(private val sectionDAO: SectionDAO) {
    val readAllData: LiveData<List<Section>> = sectionDAO.readAllData()

    suspend fun addSection(section: Section){
        sectionDAO.addSection(section)
    }

//    suspend fun deleteSection(section: Section){
//        sectionDAO.deleteSection(section)
//    }

    suspend fun deleteAllSections(){
        sectionDAO.deleteAllSections()
    }
}