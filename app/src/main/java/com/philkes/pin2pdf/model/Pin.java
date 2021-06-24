package com.philkes.pin2pdf.model;

public class Pin {
    private String title;
    private String imgUrl;
    private String link;

    public Pin(String title, String imgUrl, String link) {
        this.title=title;
        this.imgUrl=imgUrl;
        this.link=link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title=title;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl=imgUrl;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link=link;
    }
}
