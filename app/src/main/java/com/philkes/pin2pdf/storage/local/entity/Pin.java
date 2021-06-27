package com.philkes.pin2pdf.storage.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.TypeConverters;

import com.philkes.pin2pdf.model.PinModel;

import java.util.Date;

@Entity(primaryKeys={"pinId", "board"})
public class Pin {

    @NonNull
    public String pinId;
    @NonNull
    public String board;

    @ColumnInfo
    public String title;

    @ColumnInfo
    public String imgUrl;

    @ColumnInfo
    public String link;

    @ColumnInfo
    public String pdfLink;


    public Pin() {
    }

    private Pin(String pinId, String title, String imgUrl, String link, String pdfLink, String board) {
        this.pinId=pinId;
        this.title=title;
        this.imgUrl=imgUrl;
        this.link=link;
        this.pdfLink=pdfLink;
        this.board=board;
    }

    public PinModel toModel() {
        return new PinModel(title, imgUrl, link, pdfLink, board, pinId);
    }

    public static Pin fromModel(PinModel model) {
        return new Pin(model.getId(), model.getTitle(), model.getImgUrl(),
                model.getLink(), model.getPdfLink(), model.getBoard());
    }
}
