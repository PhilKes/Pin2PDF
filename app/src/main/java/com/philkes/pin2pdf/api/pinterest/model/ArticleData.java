package com.philkes.pin2pdf.api.pinterest.model;

import org.apache.commons.lang3.StringEscapeUtils;


public class ArticleData {
    private String name;

    public ArticleData() {
    }

    public String getName() {
        return StringEscapeUtils.unescapeHtml3(name);
    }

    public void setName(String name) {
        this.name=StringEscapeUtils.unescapeHtml3(name);
    }
}
