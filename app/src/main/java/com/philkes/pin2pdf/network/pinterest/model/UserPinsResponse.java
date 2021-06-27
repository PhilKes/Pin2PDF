package com.philkes.pin2pdf.network.pinterest.model;

import com.philkes.pin2pdf.data.Pin;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserPinsResponse {
    private UserPinsResponseData data;

    public UserPinsResponse() {
    }

    public UserPinsResponseData getData() {
        return data;
    }

    public void setData(UserPinsResponseData data) {
        this.data=data;
    }

    public Map<String, List<Pin>> getBoardPins() {
        Map<String, List<Pin>> boardPins=data.getPins().stream()
                .map(PinResponse::toPin)
                .collect(Collectors.groupingBy(Pin::getBoard));
        return boardPins;
    }

    public List<Pin> getPins() {
        return data.getPins().stream().map(PinResponse::toPin).collect(Collectors.toList());
    }

    public List<Pin> getBoardPins(String boardName) {
        return data.getPins().stream().map(pinResponse -> pinResponse.toBoardPin(boardName)).collect(Collectors.toList());
    }
}
