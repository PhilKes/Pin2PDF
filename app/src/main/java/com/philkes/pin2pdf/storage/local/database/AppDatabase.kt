package com.philkes.pin2pdf.storage.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.philkes.pin2pdf.storage.local.dao.PinDao
import com.philkes.pin2pdf.storage.local.entity.Pin
import java.util.concurrent.Executors

@Database(entities = [Pin::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pinDao(): PinDao

    companion object {
        @kotlin.jvm.Volatile
        private lateinit var INSTANCE: AppDatabase;
        private const val NUMBER_OF_THREADS = 4
        val databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS)

        fun getDatabase(context: Context?): AppDatabase {
            if (context != null) {
                INSTANCE = Room.databaseBuilder<AppDatabase>(
                    context.applicationContext,
                    AppDatabase::class.java, "app_database"
                )
                    .build()
            }
            return INSTANCE
        }
    }
}