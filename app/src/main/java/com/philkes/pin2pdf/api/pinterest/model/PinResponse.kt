package com.philkes.pin2pdf.api.pinterest.model

import com.google.gson.annotations.SerializedName
import com.philkes.pin2pdf.fragment.boards.PinModel

class PinResponse {
    var images: ImagesResponse? = null
    var description: String? = null

    @SerializedName("grid_title")
    var gridTitle: String? = null
    var link: String? = null
    var id: String? = null
    var board: BoardResponse? = null
    val smallImg: String?
        get() = images!!.imgSmall!!.url
    val boardName: String?
        get() = if (board != null) board!!.name else null

    fun toPin(): PinModel {
        return PinModel(null, id, gridTitle, smallImg, link, null, boardName, null)
    }

    fun toBoardPin(boardName: String?): PinModel {
        return PinModel(null, id, gridTitle, smallImg, link, null, boardName, null)
    }
}