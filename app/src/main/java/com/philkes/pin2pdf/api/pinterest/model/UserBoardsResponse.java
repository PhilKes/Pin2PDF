package com.philkes.pin2pdf.api.pinterest.model;


public class UserBoardsResponse {

    private PinterestResourceResponse<BoardResponse> resource_response;

    public UserBoardsResponse() {
    }

    public PinterestResourceResponse<BoardResponse> getResource_response() {
        return resource_response;
    }

    public void setResource_response(PinterestResourceResponse<BoardResponse> resource_response) {
        this.resource_response=resource_response;
    }
}
