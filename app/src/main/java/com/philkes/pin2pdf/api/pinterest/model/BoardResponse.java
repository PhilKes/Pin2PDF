package com.philkes.pin2pdf.api.pinterest.model;

public class BoardResponse {

    private String name;
    private String url;
    private String id;

    public BoardResponse() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name=name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url=url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id=id;
    }
}
