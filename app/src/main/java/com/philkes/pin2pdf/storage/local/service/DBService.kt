package com.philkes.pin2pdf.storage.local.service

import android.content.Context
import com.philkes.pin2pdf.model.PinModel
import com.philkes.pin2pdf.storage.local.dao.PinDao
import com.philkes.pin2pdf.storage.local.database.AppDatabase
import com.philkes.pin2pdf.storage.local.entity.Pin
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors

/**
 * Singleton Service to interact with the device's database
 */
class DBService private constructor(private val pinDao: PinDao) {
    fun loadPins(pinIds: List<String?>?, onSuccess: Consumer<List<PinModel?>?>?) {
        execute {
            val pins = pinDao.loadAllByPinIds(pinIds)
                .stream()
                .map { obj: Pin? -> obj!!.toModel() }.collect(Collectors.toList())
            onSuccess?.accept(pins)
        }
    }

    fun insertPins(pins: MutableList<PinModel>, onSuccess: Runnable?) {
        execute {
            pinDao.insertAll(
                pins.stream()
                    .map { model: PinModel -> Pin.fromModel(model) }
                    .collect(
                        Collectors.toList()
                    )
            )
            onSuccess?.run()
        }
    }

    fun updatePin(pin: PinModel, onSuccess: Runnable?) {
        execute {
            pinDao.update(Pin.fromModel(pin))
            onSuccess?.run()
        }
    }

    private fun execute(runnable: Runnable) {
        AppDatabase.Companion.databaseWriteExecutor.execute(runnable)
    }

    fun clearAll() {
        execute { pinDao.clear() }
    }

    companion object {
        private var instance: DBService? = null
        @JvmStatic
        fun getInstance(context: Context): DBService {
            if (instance == null) {
                instance = DBService(
                    AppDatabase.getDatabase(context)!!.pinDao()
                )
            }
            return instance!!
        }
    }
}