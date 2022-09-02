package com.philkes.pin2pdf.api.pinterest.model

class BoardResponse(
    var name: String? = null,
    var url: String? = null,
    var id: String? = null
){
    override fun toString(): String {
        return "$name"
    }
}