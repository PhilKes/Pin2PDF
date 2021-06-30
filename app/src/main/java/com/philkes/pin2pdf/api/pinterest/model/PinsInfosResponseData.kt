package com.philkes.pin2pdf.api.pinterest.model

import com.google.gson.annotations.SerializedName

class PinsInfosResponseData {
    @SerializedName("rich_metadata")
    var richMetaData: RichMetaData? = null
}