package com.philkes.pin2pdf.storage.local.database

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.philkes.pin2pdf.storage.local.dao.PinDao
import com.philkes.pin2pdf.storage.local.entity.Pin
import java.util.concurrent.Executors

@SuppressLint("RestrictedApi")
@Database(entities = [Pin::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pinDao(): PinDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val NUMBER_OF_THREADS = 4
        val databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS)
        fun getDatabase(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java, "app_database"
                        )
                            .build()
                    }
                }
            }
            return INSTANCE
        }
    }
}