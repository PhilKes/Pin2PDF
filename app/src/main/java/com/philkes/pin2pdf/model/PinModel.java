package com.philkes.pin2pdf.model;

import org.apache.commons.lang3.StringEscapeUtils;

public class PinModel {
    private Integer id;
    private String pinId;
    private String title;
    private String imgUrl;
    private String link;
    private String pdfLink;
    private String board;
    private String note;

    public PinModel(Integer id, String pinId, String title, String imgUrl, String link,
                    String pdfLink, String board,
                    String note) {
        this.id=id;
        this.pinId=pinId;
        setTitle(title);
        setImgUrl(imgUrl);
        setLink(link);
        setPdfLink(pdfLink);
        setBoard(board);
        setNote(note);
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board=StringEscapeUtils.unescapeHtml3(board)
                .replace("/", "")
                .replace(" ", "-");
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id=id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note=note;
    }

    public String getPinId() {
        return pinId;
    }

    public void setPinId(String pinId) {
        this.pinId=pinId;
    }
}
