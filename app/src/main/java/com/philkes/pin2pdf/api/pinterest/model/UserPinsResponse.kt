package com.philkes.pin2pdf.api.pinterest.model;

import com.philkes.pin2pdf.model.PinModel;

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

    public Map<String, List<PinModel>> getBoardPins() {
        Map<String, List<PinModel>> boardPins=data.getPins().stream()
                .map(PinResponse::toPin)
                .collect(Collectors.groupingBy(PinModel::getBoard));
        return boardPins;
    }

    public List<PinModel> getPins() {
        return data.getPins().stream().map(PinResponse::toPin).collect(Collectors.toList());
    }

    public List<PinModel> getBoardPins(String boardName) {
        return data.getPins().stream().map(pinResponse -> pinResponse.toBoardPin(boardName)).collect(Collectors.toList());
    }
}
