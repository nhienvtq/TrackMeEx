package com.example.trackmeex.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "sectionRV_table")
data class Section(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val distance: String,
    val speed: String,
    val time: String,
    val image: String
): Parcelable