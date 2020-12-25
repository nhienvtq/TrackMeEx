package com.example.trackmeex.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SectionDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSection(section: Section)

    @Delete
    suspend fun deleteSection(section: Section)

    @Query("DELETE FROM sectionRV_table")
    suspend fun  deleteAllSections()

    @Query("SELECT * FROM sectionRV_table ORDER BY id DESC")
    fun readAllData(): LiveData<List<Section>>
}