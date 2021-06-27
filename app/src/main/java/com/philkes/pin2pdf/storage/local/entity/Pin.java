package com.philkes.pin2pdf.storage.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Pin {
    @PrimaryKey
    public String pinId;

    @ColumnInfo
    public String title;

    @ColumnInfo
    public String imgUrl;

    @ColumnInfo
    public String link;

    @ColumnInfo
    public String pdfLink;

    @ColumnInfo
    public String board;
}
