package com.philkes.pin2pdf.storage.local.dao

import androidx.room.*
import com.philkes.pin2pdf.storage.local.entity.Pin

@Dao
interface PinDao {
    @get:Query("SELECT * FROM Pin")
    val all: List<Pin?>?

    @Query("SELECT * FROM pin WHERE pinId IN (:pinIds) ORDER BY pinId DESC")
    fun loadAllByPinIds(pinIds: List<String>): List<Pin>

    @Insert
    fun insertAll(pins: List<Pin>?)

    @Delete
    fun delete(pin: Pin?)

    @Query("DELETE FROM Pin")
    fun clear()

    @Update
    fun update(pin: Pin?)
}