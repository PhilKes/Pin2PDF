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
import com.philkes.pin2pdf.network.model.PinsInfosResponse;
import com.philkes.pin2pdf.network.model.RichMetaData;
import com.philkes.pin2pdf.network.model.UserPinsResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

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
                        UserPinsResponse responseObj=gson.fromJson(response, UserPinsResponse.class);
                        Set<String> boardNames=responseObj.getBoardPins().keySet();
                        Integer requestCount=boardNames.size();
                        CountDownLatch requestCountDown=new CountDownLatch(requestCount);
                        for(String boardName : boardNames) {
                            requestPinsOfBoard(context, queue, user, boardName, pins, requestCountDown,null);
                        }
                        new Thread(() -> {
                            try {
                                requestCountDown.await();
                                onSuccess.accept(pins);
                            }
                            catch(InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
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

    public static void requestPinsOfBoard(Context context, RequestQueue queue, String user, String boardName,
                                          Map<String, List<Pin>> pins, CountDownLatch requestCountDown,
                                          Consumer<Map<String, List<Pin>>> consumer) {
        String url=PIN_BASE_URL + "boards/" + user + "/" + boardName + "/pins";

        // Request a string response from the provided URL.
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        UserPinsResponse responseObj=gson.fromJson(response, UserPinsResponse.class);
                        List<Pin> boardPins=responseObj.getBoardPins(boardName);
                        Integer requestCount=1;
                        CountDownLatch requestCountDown2=new CountDownLatch(requestCount);
                        RequestQueue queue2=Volley.newRequestQueue(context);
                        requestPinsInfos(queue2, requestCountDown2, boardPins);
                        new Thread(() -> {
                            try {
                                requestCountDown2.await();
                                pins.put(boardName, boardPins);
                                if(requestCountDown!=null) {
                                    requestCountDown.countDown();
                                }
                                if(consumer!=null) {
                                    consumer.accept(pins);
                                }
                            }
                            catch(InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
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

    private static void requestPinsInfos(RequestQueue queue2, CountDownLatch requestCountDown2, List<Pin> boardPins) {
        String idsStr=boardPins.stream().map(Pin::getId).collect(Collectors.joining(","));
        String url=PIN_BASE_URL + "pins/info/?pin_ids=" + idsStr;
        // Request a string response from the provided URL.
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        PinsInfosResponse responseObj=gson.fromJson(response, PinsInfosResponse.class);
                        for(int i=0; i<boardPins.size(); i++) {
                            RichMetaData metaData=responseObj.getData().get(i).getRichMetaData();
                            String title=boardPins.get(i).getTitle();
                            if(metaData!=null && metaData.getArticle()!=null) {
                                title=metaData.getArticle().getName();
                            }
                            boardPins.get(i).setTitle(title);
                        }
                        requestCountDown2.countDown();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "nErrorResponse: Failed");
            }
        });
        // Add the request to the RequestQueue.
        queue2.add(stringRequest);

    }

}
