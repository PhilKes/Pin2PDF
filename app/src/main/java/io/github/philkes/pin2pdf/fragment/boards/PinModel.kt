package io.github.philkes.pin2pdf.fragment.boards

import org.apache.commons.lang3.StringEscapeUtils

/**
 * DTO Object for [io.github.philkes.pin2pdf.storage.database.entity.Pin] Entity
 */
class PinModel(
    var id: Int?, var pinId: String?, mTitle: String?, mImgUrl: String?, mLink: String?,
    mPdfLink: String?, mBoard: String?,
    mNote: String?, mIsFavorite: Boolean
) {
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
    var note: String? = null
    var isFavorite: Boolean = false

    init {
        title = mTitle
        imgUrl = mImgUrl
        link = mLink
        pdfLink = mPdfLink
        board = mBoard
        note = mNote
        isFavorite = mIsFavorite
    }
}