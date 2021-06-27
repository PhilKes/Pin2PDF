package com.philkes.pin2pdf.storage.local.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.philkes.pin2pdf.storage.local.dao.PinDao;
import com.philkes.pin2pdf.storage.local.entity.Pin;

@Database(entities={Pin.class}, version=1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PinDao pinDao();
}
