package com.philkes.pin2pdf.api.pinterest.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PinterestResourceResponse<T> {

    private List<T> data;

    @SerializedName("http_status")
    private Integer httpStatus;

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data=data;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus=httpStatus;
    }
}
