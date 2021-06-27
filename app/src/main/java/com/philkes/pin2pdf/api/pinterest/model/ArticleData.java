package com.philkes.pin2pdf.api.pinterest.model;

import static com.philkes.pin2pdf.Util.toStr;

public class ArticleData {
    private String name;

    public ArticleData() {
    }

    public String getName() {
        return toStr(name);
    }

    public void setName(String name) {
        this.name=toStr(name);
    }
}
