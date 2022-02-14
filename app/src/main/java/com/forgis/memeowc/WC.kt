package com.forgis.memeowc

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WC(
    @ColumnInfo(name = "latitude") val latitude: Double?,
    @ColumnInfo(name = "longitude") val longitude: Double?,
    @ColumnInfo(name = "description") val description: String?,

    @PrimaryKey(autoGenerate = true) val uid: Long = 0
)