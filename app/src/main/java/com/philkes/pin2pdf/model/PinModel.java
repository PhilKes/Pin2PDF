package com.philkes.pin2pdf.model;

import org.apache.commons.lang3.StringEscapeUtils;

public class PinModel {
    private String title;
    private String imgUrl;
    private String link;
    private String pdfLink;
    private String board;
    private String id;

    public PinModel(String title, String imgUrl, String link, String pdfLink,String board, String id) {
        setTitle(title);
        setImgUrl(imgUrl);
        setLink(link);
        setPdfLink(pdfLink);
        setBoard(board);
        this.id=id;
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board=StringEscapeUtils.unescapeHtml3(board)
                .replace("/", "")
                .replace(" ","-");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title=StringEscapeUtils.unescapeHtml3(title);
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

    public String getPdfLink() {
        return pdfLink;
    }

    public void setPdfLink(String pdfLink) {
        this.pdfLink=pdfLink;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id=id;
    }
}
