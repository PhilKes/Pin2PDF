package com.philkes.pin2pdf.model;

import com.philkes.pin2pdf.Util;

import static com.philkes.pin2pdf.Util.toStr;

public class Pin {
    private static final String PINTEREST_URL="https://www.pinterest.de";
    private String title;
    private String imgUrl;
    private String link;
    private String pdfLink;
    private String board;

    public Pin(String title, String imgUrl, String link, String board) {
        setTitle(title);
        setImgUrl(imgUrl);
        setLink(link);
        setPdfLink(null);
        setBoard(board);
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board=toStr(board).replace("/","");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title=toStr(title);
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl=imgUrl;
    }

    public String getLink() {
        return PINTEREST_URL + link;
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
