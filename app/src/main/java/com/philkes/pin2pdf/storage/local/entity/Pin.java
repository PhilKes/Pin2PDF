package com.philkes.pin2pdf.storage.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.philkes.pin2pdf.model.PinModel;

@Entity
public class Pin {
    @PrimaryKey(autoGenerate=true)
    public Integer id;

    @ColumnInfo
    public String pinId;
    @ColumnInfo
    public String board;

    @ColumnInfo
    public String title;

    @ColumnInfo
    public String imgUrl;

    @ColumnInfo
    public String link;

    @ColumnInfo
    public String pdfLink;

    @ColumnInfo
    public String note;


    public Pin() {
    }

    private Pin(Integer id, String pinId, String title, String imgUrl, String link, String pdfLink, String board, String note) {
        this.id=id;
        this.pinId=pinId;
        this.title=title;
        this.imgUrl=imgUrl;
        this.link=link;
        this.pdfLink=pdfLink;
        this.board=board;
        this.note=note;
    }

    public PinModel toModel() {
        return new PinModel(id, pinId, title, imgUrl, link, pdfLink, board, note);
    }

    public static Pin fromModel(PinModel model) {
        return new Pin(model.getId(), model.getPinId(), model.getTitle(), model.getImgUrl(),
                model.getLink(), model.getPdfLink(), model.getBoard(), model.getNote());
    }
}
