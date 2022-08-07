package com.philkes.pin2pdf.storage.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.philkes.pin2pdf.Util.convertToCompatibleFileName
import com.philkes.pin2pdf.fragment.boards.PinModel

@Entity
class Pin {
    @JvmField
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    @JvmField
    @ColumnInfo
    var pinId: String? = null

    @JvmField
    @ColumnInfo
    var board: String? = null

    @JvmField
    @ColumnInfo
    var title: String? = null

    @JvmField
    @ColumnInfo
    var imgUrl: String? = null

    @JvmField
    @ColumnInfo
    var link: String? = null

    @JvmField
    @ColumnInfo
    var pdfLink: String? = null

    @JvmField
    @ColumnInfo
    var note: String? = null

    constructor() {}
    private constructor(
        id: Int?,
        pinId: String?,
        title: String?,
        imgUrl: String?,
        link: String?,
        pdfLink: String?,
        board: String?,
        note: String?
    ) {
        this.id = id
        this.pinId = pinId
        this.title = title
        this.imgUrl = imgUrl
        this.link = link
        this.pdfLink = pdfLink
        this.board = board
        this.note = note
    }

    fun toModel(): PinModel {
        return PinModel(id, pinId, title, imgUrl, link, pdfLink, board, note)
    }

    companion object {
        fun fromModel(model: PinModel): Pin {
            val title = if(model.title !=null && model.title!!.isNotEmpty()) model.title!! else model.link!!
            return Pin(
                model.id, model.pinId, title, model.imgUrl,
                model.link, model.pdfLink, model.board, model.note
            )
        }
    }
}