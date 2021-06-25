package com.philkes.pin2pdf.model;

public class Pin {
    private static final String PINTEREST_URL="https://www.pinterest.de";
    private String title;
    private String imgUrl;
    private String link;
    private String pdfLink;

    public Pin(String title, String imgUrl, String link) {
        this.title=title;
        this.imgUrl=imgUrl;
        this.link=link;
        this.pdfLink=null;
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
        return PINTEREST_URL+link;
    }

    public void setLink(String link) {
        this.link=link;
    }

    public String getPdfLink() {
        return pdfLink;
    }

    public void setPdfLink(String pdfLink) {
        this.pdfLink=pdfLink;
    }
}
