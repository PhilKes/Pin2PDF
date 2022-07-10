package com.philkes.pin2pdf.storage.database

import android.annotation.SuppressLint
import androidx.room.Database
import androidx.room.RoomDatabase

@SuppressLint("RestrictedApi")
@Database(entities = [Pin::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pinDao(): PinDao
}