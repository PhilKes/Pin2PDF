package com.philkes.pin2pdf.storage.local.service;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.philkes.pin2pdf.storage.local.database.AppDatabase;

public class DBService {
    private static AppDatabase db;

    public static AppDatabase build(Context context){
        db=Room.databaseBuilder(context, AppDatabase.class,"database").build();
        return db;
    }

    public static AppDatabase getInstance(){
        return db;
    }
}
