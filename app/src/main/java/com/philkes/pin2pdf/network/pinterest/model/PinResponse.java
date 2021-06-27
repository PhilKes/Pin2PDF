package com.philkes.pin2pdf.network.pinterest.model;

import com.philkes.pin2pdf.data.Pin;

public class PinResponse {
    private ImagesResponse images;
    private String description;
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

    public String getBoardName() {
        return board!=null ? board.getName() : null;
    }

    public Pin toPin() {
        return new Pin(getDescription(), getSmallImg(), getLink(), getBoardName(), getId());

    }

    public Pin toBoardPin(String boardName) {
        return new Pin(getDescription(), getSmallImg(), getLink(), boardName, getId());

    }
}
