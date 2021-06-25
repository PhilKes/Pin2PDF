package com.philkes.pin2pdf.network.model;

import java.util.List;

public class UserPinsResponseData {
    private List<PinResponse> pins;

    public UserPinsResponseData() {
    }

    public List<PinResponse> getPins() {
        return pins;
    }

    public void setPins(List<PinResponse> pins) {
        this.pins=pins;
    }
}
