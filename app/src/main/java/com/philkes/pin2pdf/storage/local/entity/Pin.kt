package com.philkes.pin2pdf.storage.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.philkes.pin2pdf.model.PinModel

@Entity(primaryKeys = ["pinId", "board"])
class Pin {

    var pinId: String? = null
    var board: String? = null

    @ColumnInfo
    var title: String? = null

    @ColumnInfo
    var imgUrl: String? = null

    @ColumnInfo
    var link: String? = null

    @ColumnInfo
    var pdfLink: String? = null

    constructor() {}
    private constructor(
        pinId: String?,
        title: String?,
        imgUrl: String?,
        link: String?,
        pdfLink: String?,
        board: String?
    ) {
        this.pinId = pinId
        this.title = title
        this.imgUrl = imgUrl
        this.link = link
        this.pdfLink = pdfLink
        this.board = board
    }

    fun toModel(): PinModel {
        return PinModel(title, imgUrl, link, pdfLink, board, pinId)
    }

    companion object {
        fun fromModel(model: PinModel): Pin {
            return Pin(
                model.id, model.title, model.imgUrl,
                model.link, model.pdfLink, model.board
            )
        }
    }
}