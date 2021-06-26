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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static com.philkes.pin2pdf.Util.getUrlDomainName;

public class PinterestAPI {
    private static final String PIN_BASE_URL="https://widgets.pinterest.com/v3/pidgets/";
    private static final String TAG="PinterestAPI";

    private static final Gson gson=new Gson();

    public static void requestBoardsOfUser(Context context, String user, Consumer<List<String>> onSuccess) {
        RequestQueue queue=Volley.newRequestQueue(context);
        String url=PIN_BASE_URL + "users/" + user + "/pins";

        // Request a string response from the provided URL.
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        UserPinsResponse responseObj=gson.fromJson(response, UserPinsResponse.class);
                        Set<String> boardNames=responseObj.getBoardPins().keySet();
                        if(onSuccess!=null) {
                            onSuccess.accept(new ArrayList<>(boardNames));
                        }
                       /* Integer requestCount=boardNames.size();
                        CountDownLatch requestCountDown=new CountDownLatch(requestCount);
                        for(String boardName : boardNames) {
                            requestPinsOfBoard(context, queue, user, boardName, pins, requestCountDown, null);
                        }
                        new Thread(() -> {
                            try {
                                requestCountDown.await();
                                onSuccess.accept(new ArrayList<>(boardNames));
                            }
                            catch(InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();*/
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

    /**
     * Get Pins of the User's given Board
     **/
    public static void requestPinsOfBoard(Context context, String user, String boardName,
                                          Consumer<List<Pin>> consumer) {
        RequestQueue queue=Volley.newRequestQueue(context);
        String url=PIN_BASE_URL + "boards/" + user + "/" + boardName + "/pins";

        // Request a string response from the provided URL.
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        UserPinsResponse responseObj=gson.fromJson(response, UserPinsResponse.class);
                        List<Pin> boardPins=responseObj.getBoardPins(boardName);
                        // Scrape PDF/Print Links with JSoup
                        fillPDFLinks(boardPins);
                        // Counter to wait until requestPinsInfos is done
                        Integer requestCount=1;
                        CountDownLatch requestCountDown=new CountDownLatch(requestCount);
                        RequestQueue queue2=Volley.newRequestQueue(context);
                        requestPinsInfos(queue2, requestCountDown, boardPins);
                        new Thread(() -> {
                            try {
                                requestCountDown.await();
                                // Execute consumer if all Pins were loaded
                                if(consumer!=null) {
                                    consumer.accept(boardPins);
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

    /**
     * Try to scrape PDF/Print Links from original Recipe Link
     **/
    private static void fillPDFLinks(List<Pin> boardPins) {
        for(Pin pin : boardPins) {
            Thread t1=new Thread(() -> {
                try {

                    Document doc=Jsoup.connect(pin.getLink())
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36")
                            .get();
                    // Get all Links (<a>)
                    Elements pinsHrefs=doc
                            .select("a[href]");
                    // Find Link with Text containing "Print" or "Drucken"
                    Element pinHref=pinsHrefs
                            .select(":contains(Print)").first();
                    if(pinHref==null) {
                        pinHref=pinsHrefs
                                .select(":contains(Drucken)").first();
                    }
                    if(pinHref!=null) {
                        String pdfLink=pinHref.attr("href");
                        // Check if found Link is actual link (not e.g. javascript code)
                        boolean skipped=pdfLink.equals("#") || pdfLink.contains("javascript") || pdfLink.contains("()");
                        System.out.println(skipped ? "PDFLink Skipped: " + pdfLink : "PDFLink: " + pdfLink);
                        if(pdfLink.startsWith("/")) {
                            pdfLink="http://" + getUrlDomainName(pin.getLink()) + pdfLink;
                          /*  System.out.println("ORIGINAL LINK: " + pin.getLink());
                            System.out.println("LINK: " + pdfLink);
                            System.out.println("Fused Link :" + (pdfLink));*/
                        }
                        if(!skipped) {
                            pin.setPdfLink(pdfLink);
                        }
                    }

                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            });
            t1.start();
            try {
                t1.join();
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Get detailed Infos of Pins (fill Title if present)
     **/
    private static void requestPinsInfos(RequestQueue queue2, CountDownLatch requestCountDown2, List<Pin> pins) {
        String idsStr=pins.stream().map(Pin::getId).collect(Collectors.joining(","));
        String url=PIN_BASE_URL + "pins/info/?pin_ids=" + idsStr;
        // Request a string response from the provided URL.
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        PinsInfosResponse responseObj=gson.fromJson(response, PinsInfosResponse.class);
                        for(int i=0; i<pins.size(); i++) {
                            RichMetaData metaData=responseObj.getData().get(i).getRichMetaData();
                            String title=pins.get(i).getTitle();
                            if(metaData!=null && metaData.getArticle()!=null) {
                                title=metaData.getArticle().getName();
                            }
                            pins.get(i).setTitle(title);
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
