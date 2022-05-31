package com.philkes.pin2pdf.storage.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.philkes.pin2pdf.storage.local.entity.Pin;

import java.util.List;

@Dao
public interface PinDao {

    @Query("SELECT * FROM Pin")
    List<Pin> getAll();

    @Query("SELECT * FROM pin WHERE pinId IN (:pinIds) ORDER BY pinId DESC")
    List<Pin> loadAllByPinIds(List<String> pinIds);

    @Insert
    void insertAll(List<Pin> pins);

    @Delete
    void delete(Pin pin);

    @Query("DELETE FROM Pin")
    void clear();

    @Update
    void update(Pin pin);


}
