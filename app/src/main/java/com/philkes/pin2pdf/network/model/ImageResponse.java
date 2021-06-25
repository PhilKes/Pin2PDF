package com.philkes.pin2pdf.network.model;

public class ImageResponse {
    private int width;
    private int height;
    private String url;

    public ImageResponse() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url=url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width=width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height=height;
    }
}
