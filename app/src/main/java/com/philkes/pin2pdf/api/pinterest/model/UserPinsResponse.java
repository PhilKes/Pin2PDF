package com.philkes.pin2pdf.api.pinterest.model;

import com.google.gson.annotations.SerializedName;

public class UserPinsResponse {

    @SerializedName("resource_response")
    private PinterestResourceResponse<PinResponse> resourceResponse;

    public UserPinsResponse() {
    }

    public PinterestResourceResponse<PinResponse> getResourceResponse() {
        return resourceResponse;
    }

    public void setResourceResponse(PinterestResourceResponse<PinResponse> resourceResponse) {
        this.resourceResponse=resourceResponse;
    }
}
