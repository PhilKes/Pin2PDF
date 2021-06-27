package com.philkes.pin2pdf.storage.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.philkes.pin2pdf.storage.local.entity.Pin;

import java.util.List;

@Dao
public interface PinDao {

    @Query("SELECT * FROM Pin")
    List<Pin> getAll();

    @Query("SELECT * FROM pin WHERE pinId IN (:ids)")
    List<Pin> loadAllByIds(String[] ids);

    @Insert
    void insertAll(Pin... pins);

    @Delete
    void delete(Pin pin);

}
