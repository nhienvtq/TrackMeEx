package com.example.trackmeex.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Section::class], version = 1, exportSchema = false)
abstract class SectionDatabase: RoomDatabase() {

    abstract fun sectionDAO(): SectionDAO

    companion object{
        @Volatile
        private var INSTANCE: SectionDatabase? = null

        fun getDatabase(context: Context): SectionDatabase{
            val tempInstance = INSTANCE
            //verify pre-exist database - else create instance
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(context.applicationContext,
                    SectionDatabase::class.java, "sectionRV_database").build()
                INSTANCE = instance
                return instance
            }
        }
    }
}