package com.philkes.pin2pdf.model

import org.apache.commons.lang3.StringEscapeUtils

class PinModel {
    var title: String? = null
        set(title) {
            field = StringEscapeUtils.unescapeHtml3(title)
        }
    var imgUrl: String? = null
    var link: String? = null
    var pdfLink: String? = null
    var board: String? = null
        set(board) {
            field = StringEscapeUtils.unescapeHtml3(board)
                .replace("/", "")
                .replace(" ", "-")
        }
    var id: String? = null

    constructor()
    constructor(
        title: String?,
        imgUrl: String?,
        link: String?,
        pdfLink: String?,
        board: String?,
        id: String?
    ) {
        this.title = title
        this.imgUrl = imgUrl
        this.link = link
        this.pdfLink = pdfLink
        this.board = board
        this.id = id
    }

}