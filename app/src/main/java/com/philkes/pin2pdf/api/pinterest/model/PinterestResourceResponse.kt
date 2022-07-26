package com.philkes.pin2pdf.api.pinterest.model

import com.google.gson.annotations.SerializedName

class PinterestResourceResponse<T> {
    var data: List<T>? = null

    @SerializedName("http_status")
    var httpStatus: Int? = null
}