package com.philkes.pin2pdf.api.pinterest.model

import com.google.gson.annotations.SerializedName

class UserPinsResponse {
    @SerializedName("resource_response")
    var resourceResponse: PinterestResourceResponse<PinResponse?>? = null
}