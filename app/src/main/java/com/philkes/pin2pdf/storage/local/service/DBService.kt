package com.philkes.pin2pdf.storage.local.service

import android.content.Context
import com.philkes.pin2pdf.model.PinModel
import com.philkes.pin2pdf.storage.local.dao.PinDao
import com.philkes.pin2pdf.storage.local.database.AppDatabase
import com.philkes.pin2pdf.storage.local.entity.Pin
import java.util.function.Consumer
import java.util.stream.Collectors

class DBService private constructor(private val pinDao: PinDao) {
    fun loadPins(pinIds: List<String>, onSuccess: Consumer<List<PinModel>>?) {
        execute {
            val pins = pinDao.loadAllByIds(pinIds)
                .stream()
                .map { obj: Pin -> obj.toModel() }.collect(Collectors.toList())
            onSuccess?.accept(pins)
        }
    }

    fun insertPins(pins: List<PinModel>, onSuccess: Runnable?) {
        execute {
            pinDao.insertAll(
                pins.stream().map(Pin::fromModel).collect(Collectors.toList())
            )
            onSuccess?.run()
        }
    }

    private fun execute(runnable: Runnable) {
        AppDatabase.databaseWriteExecutor.execute(runnable)
    }

    companion object {
        private lateinit var instance: DBService

        fun getInstance(context: Context?): DBService {
            if (context != null) {
                instance = DBService(AppDatabase.getDatabase(context)!!.pinDao())
            }
            return instance
        }
    }
}