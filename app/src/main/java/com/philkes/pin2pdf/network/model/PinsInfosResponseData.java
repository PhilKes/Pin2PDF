package com.philkes.pin2pdf.network.model;

import com.google.gson.annotations.SerializedName;

public class PinsInfosResponseData {

    @SerializedName("rich_metadata")
    private RichMetaData richMetaData;

    public PinsInfosResponseData() {
    }

    public RichMetaData getRichMetaData() {
        return richMetaData;
    }

    public void setRichMetaData(RichMetaData richMetaData) {
        this.richMetaData=richMetaData;
    }
}
