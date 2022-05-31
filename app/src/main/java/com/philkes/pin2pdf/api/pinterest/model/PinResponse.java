package com.philkes.pin2pdf.api.pinterest.model;

import com.google.gson.annotations.SerializedName;
import com.philkes.pin2pdf.model.PinModel;

public class PinResponse {
    private ImagesResponse images;
    private String description;
    @SerializedName("grid_title")
    private String gridTitle;
    private String link;
    private String id;
    private BoardResponse board;

    public PinResponse() {
    }

    public ImagesResponse getImages() {
        return images;
    }

    public void setImages(ImagesResponse images) {
        this.images=images;
    }

    public BoardResponse getBoard() {
        return board;
    }

    public void setBoard(BoardResponse board) {
        this.board=board;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description=description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link=link;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id=id;
    }

    public String getSmallImg() {
        return images.getImgSmall().getUrl();
    }

    public String getGridTitle() {
        return gridTitle;
    }

    public void setGridTitle(String gridTitle) {
        this.gridTitle=gridTitle;
    }

    public String getBoardName() {
        return board!=null ? board.getName() : null;
    }

    public PinModel toPin() {
        return new PinModel(null, getId(), getGridTitle(), getSmallImg(), getLink(), null, getBoardName(), null);

    }

    public PinModel toBoardPin(String boardName) {
        return new PinModel(null, getId(), getGridTitle(), getSmallImg(), getLink(), null, boardName, null);

    }
}
