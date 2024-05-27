package io.github.philkes.pin2pdf.storage.database

import androidx.lifecycle.LiveData
import io.github.philkes.pin2pdf.fragment.boards.PinModel
import io.github.philkes.pin2pdf.fragment.boards.PinModel.Companion.escapeForDB
import java.util.function.Consumer

/**
 * Singleton Service to interact with the device's database
 */
class DBService constructor(private val pinDao: PinDao) {
    suspend fun loadPins(pinIds: List<String>, onSuccess: Consumer<List<PinModel>>?) {
        val pins = pinDao.loadAllByPinIds(pinIds)
            .map { obj: Pin? -> obj!!.toModel() }
        onSuccess?.accept(pins)
    }

    fun loadPinsOfBoard(board: String): LiveData<List<Pin>> {
        return pinDao.loadAllPinsOfBoard(escapeForDB(board))
    }

    suspend fun loadAllPins(onSuccess: Consumer<List<PinModel>>?) {
        val pins = pinDao.loadAllPins().map { obj: Pin? -> obj!!.toModel() }
        onSuccess?.accept(pins)
    }

    suspend fun insertPins(pins: List<PinModel>, onSuccess: Runnable?) {
        pinDao.insertAll(
            pins.map { model: PinModel -> Pin.fromModel(model) }
        )
        onSuccess?.run()
    }

    suspend fun updatePin(pin: PinModel, onSuccess: Runnable?) {
        pinDao.update(Pin.fromModel(pin))
        onSuccess?.run()
    }


    suspend fun clearAll() {
        pinDao.clear()
    }

    fun loadFavoritePins(): LiveData<List<Pin>> {
        return pinDao.loadFavoritePins()
    }
}