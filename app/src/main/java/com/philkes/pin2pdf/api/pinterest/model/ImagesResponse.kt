package com.philkes.pin2pdf.api.pinterest.model

import com.google.gson.annotations.SerializedName

class ImagesResponse {
    @SerializedName("237x")
    var imgSmall: ImageResponse? = null

    @SerializedName("564x")
    var imgBig: ImageResponse? = null
}