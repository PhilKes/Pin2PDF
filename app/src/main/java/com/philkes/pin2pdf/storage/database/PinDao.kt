package com.philkes.pin2pdf.storage.database

import androidx.room.*

@Dao
interface PinDao {
    @get:Query("SELECT * FROM Pin")
    val all: List<Pin?>?

    @Query("SELECT * FROM pin WHERE pinId IN (:pinIds) ORDER BY pinId DESC")
    suspend fun loadAllByPinIds(pinIds: List<String>): List<Pin>

    @Query("SELECT * FROM pin WHERE board=:board ORDER BY pinId DESC")
    suspend fun loadAllPinsOfBoard(board: String): List<Pin>

    @Query("SELECT * FROM pin WHERE isFavorite = 1 ORDER BY pinId DESC")
    suspend fun loadFavoritePins(): List<Pin>

    @Insert
    suspend fun insertAll(pins: List<Pin>?)

    @Delete
    suspend fun delete(pin: Pin?)

    @Query("DELETE FROM Pin")
    suspend fun clear()

    @Update
    suspend fun update(pin: Pin?)

}