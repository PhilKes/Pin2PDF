package com.philkes.pin2pdf.api.pinterest.model

import org.apache.commons.lang3.StringEscapeUtils

class ArticleData {
    var name: String? = null
        get() = StringEscapeUtils.unescapeHtml3(field)
        set(name) {
            field = StringEscapeUtils.unescapeHtml3(name)
        }
}