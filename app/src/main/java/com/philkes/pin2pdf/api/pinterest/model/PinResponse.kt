package com.philkes.pin2pdf.api.pinterest.model

import com.philkes.pin2pdf.model.PinModel

class PinResponse {
    var images: ImagesResponse? = null
    var description: String? = null
    var link: String? = null
    var id: String? = null
    var board: BoardResponse? = null
    val boardName: String?
        get() = if (board != null) board!!.name else null

    fun toPin(): PinModel {
        return PinModel(description, getSmallImgUrl(), link, null, boardName, id!!)
    }

    fun toBoardPin(boardName: String?): PinModel {
        return PinModel(description, getSmallImgUrl(), link, null, boardName, id!!)
    }

    fun getSmallImgUrl(): String?{
        return images?.imgSmall?.url
    }
}