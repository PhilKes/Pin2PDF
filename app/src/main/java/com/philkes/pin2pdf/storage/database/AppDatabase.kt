package com.philkes.pin2pdf.storage.database

import android.annotation.SuppressLint
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@SuppressLint("RestrictedApi")
@Database(entities = [Pin::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pinDao(): PinDao

    companion object {
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Pin ADD COLUMN isFavorite BOOLEAN NOT NULL DEFAULT(False)");
            }
        }
    }


}