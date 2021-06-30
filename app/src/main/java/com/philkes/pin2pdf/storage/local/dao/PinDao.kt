package com.philkes.pin2pdf.storage.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.philkes.pin2pdf.storage.local.entity.Pin

@Dao
interface PinDao {
    @Query("SELECT * FROM Pin")
    fun getAll(): List<Pin>

    @Query("SELECT * FROM pin WHERE pinId IN (:ids) ORDER BY pinId DESC")
    fun loadAllByIds(ids: List<String>): List<Pin>

    @Insert
    fun insertAll(pins: List<Pin>)

    @Delete
    fun delete(pin: Pin?)
}