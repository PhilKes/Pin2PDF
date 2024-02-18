package io.github.philkes.pin2pdf.api.pinterest.model

import com.google.gson.annotations.SerializedName

class ImagesResponse {
    @SerializedName("236x")
    var imgSmall: ImageResponse? = null

    @SerializedName("474x")
    var imgBig: ImageResponse? = null
}