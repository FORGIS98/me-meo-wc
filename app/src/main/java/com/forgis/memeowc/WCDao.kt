package com.forgis.memeowc

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WCDao {
    @Query("SELECT * FROM wc")
    fun getAll(): List<WC>

    @Insert
    fun insertWc(wc: WC)

    @Delete
    fun deleteWc(wc: WC)
}