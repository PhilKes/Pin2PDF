package com.philkes.pin2pdf.network;

import android.content.Context;
import android.support.v4.util.Consumer;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.philkes.pin2pdf.model.Pin;
import com.philkes.pin2pdf.network.model.UserPinsResponse;
import com.philkes.pin2pdf.network.model.UserPinsResponseData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class PinterestAPI {
    private static final String PIN_BASE_URL="https://widgets.pinterest.com/v3/pidgets/";
    private static final String TAG="PinterestAPI";

    private static final Gson gson=new Gson();

    public static void requestPinsOfUser(Context context, String user, Consumer<Map<String, List<Pin>>> onSuccess) {
        RequestQueue queue=Volley.newRequestQueue(context);
        String url=PIN_BASE_URL + "users/" + user + "/pins";

        // Request a string response from the provided URL.
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        HashMap<String, List<Pin>> pins=new HashMap<>();
                        System.out.println(response);
                        UserPinsResponse responseObj=gson.fromJson(response, UserPinsResponse.class);
                        Set<String> boardNames=responseObj.getBoardPins().keySet();
                        Integer requestCount=boardNames.size();
                        CountDownLatch requestCountDown = new CountDownLatch(requestCount);
                        for(String boardName :boardNames) {
                            requestPinsOfBoard(queue, user, boardName, pins,requestCountDown);
                        }
                        new Thread(()->{
                        try {
                            requestCountDown.await();
                            onSuccess.accept(pins);
                        }
                        catch(InterruptedException e) {
                            e.printStackTrace();
                        }}).start();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "nErrorResponse: Failed");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public static void requestPinsOfBoard(RequestQueue queue, String user, String boardName, HashMap<String, List<Pin>> pins,CountDownLatch requestCountDown ) {
        String url=PIN_BASE_URL + "boards/" + user + "/" + boardName + "/pins";

        // Request a string response from the provided URL.
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                        UserPinsResponse responseObj=gson.fromJson(response, UserPinsResponse.class);
                        pins.put(boardName,responseObj.getBoardPins(boardName));
                        requestCountDown.countDown();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "nErrorResponse: Failed");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

}
