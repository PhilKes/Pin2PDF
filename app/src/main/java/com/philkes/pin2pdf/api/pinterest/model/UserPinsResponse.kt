package com.philkes.pin2pdf.api.pinterest.model

import com.philkes.pin2pdf.model.PinModel
import java.util.stream.Collectors

class UserPinsResponse() {
    var data: UserPinsResponseData? = null
    val boardPins: Map<String, List<PinModel>>
        get() {
            val boardPins: Map<String, List<PinModel>> = data!!.pins!!.stream()
                .map({ obj: PinResponse -> obj.toPin() })
                .collect(Collectors.groupingBy(PinModel::board))
            return boardPins
        }
    val pins: List<PinModel>
        get() = data!!.pins!!.map({ it.toPin() })

    fun getBoardPins(boardName: String?): List<PinModel> {
        return data!!.pins!!.map({ it.toBoardPin(boardName) })
    }
}