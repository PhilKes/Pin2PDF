package com.philkes.pin2pdf.storage.local.service;

import android.content.Context;

import com.philkes.pin2pdf.model.PinModel;
import com.philkes.pin2pdf.storage.local.dao.PinDao;
import com.philkes.pin2pdf.storage.local.database.AppDatabase;
import com.philkes.pin2pdf.storage.local.entity.Pin;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DBService {
    private static DBService instance;

    private PinDao pinDao;

    private DBService(PinDao pinDao) {
        this.pinDao=pinDao;
    }

    public static DBService getInstance(Context context) {
        if(instance==null) {
            instance=new DBService(AppDatabase.getDatabase(context).pinDao());
        }
        return instance;
    }


    public void loadPins(List<String> pinIds, Consumer<List<PinModel>> onSuccess) {
        execute(() -> {
            List<PinModel> pins=pinDao.loadAllByIds(pinIds)
                    .stream()
                    .map(Pin::toModel).collect(Collectors.toList());
            if(onSuccess!=null) {
                onSuccess.accept(pins);
            }
        });
    }

    public void insertPins(List<PinModel> pins,Runnable onSuccess) {
        execute(() ->{
            pinDao.insertAll(
                pins.stream().map(Pin::fromModel).collect(Collectors.toList()));
            if(onSuccess!=null)
                onSuccess.run();
        });
    }

    private void execute(Runnable runnable) {
        AppDatabase.databaseWriteExecutor.execute(runnable);
    }
}
