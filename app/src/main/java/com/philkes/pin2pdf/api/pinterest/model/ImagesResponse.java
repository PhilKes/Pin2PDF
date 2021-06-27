package com.philkes.pin2pdf.api.pinterest.model;

import com.google.gson.annotations.SerializedName;

public class ImagesResponse {

    @SerializedName("237x")
    private ImageResponse imgSmall;

    @SerializedName("564x")
    private ImageResponse imgBig;

    public ImagesResponse() {
    }

    public ImageResponse getImgSmall() {
        return imgSmall;
    }

    public void setImgSmall(ImageResponse imgSmall) {
        this.imgSmall=imgSmall;
    }

    public ImageResponse getImgBig() {
        return imgBig;
    }

    public void setImgBig(ImageResponse imgBig) {
        this.imgBig=imgBig;
    }
}
