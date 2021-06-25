package com.philkes.pin2pdf.network.model;

import java.util.List;

public class PinsInfosResponse {
    private List<PinsInfosResponseData> data;

    public PinsInfosResponse() {
    }

    public List<PinsInfosResponseData> getData() {
        return data;
    }

    public void setData(List<PinsInfosResponseData> data) {
        this.data=data;
    }
}
