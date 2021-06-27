package com.philkes.pin2pdf.api.pinterest;

import android.content.Context;

import androidx.core.util.Consumer;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.philkes.pin2pdf.model.PinModel;
import com.philkes.pin2pdf.api.Tasks;
import com.philkes.pin2pdf.api.pinterest.model.PinsInfosResponse;
import com.philkes.pin2pdf.api.pinterest.model.RichMetaData;
import com.philkes.pin2pdf.api.pinterest.model.UserPinsResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class PinterestAPI {
    private static final String PIN_BASE_URL="https://widgets.pinterest.com/v3/pidgets/";
    private static final String TAG="PinterestAPI";

    private static final Gson gson=new Gson();

    private static PinterestAPI instance;
    private final RequestQueue queue;

    private PinterestAPI(Context context) {
        queue=Volley.newRequestQueue(context);
    }

    public static PinterestAPI getInstance(Context context) {
        if(instance==null) {
            instance=new PinterestAPI(context);
        }
        return instance;
    }

    public void requestBoardsOfUser(String user, Consumer<List<String>> onSuccess) {
        String url=PIN_BASE_URL + "users/" + user + "/pins";

        // Request a string response from the provided URL.
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url,
                response -> {
                    UserPinsResponse responseObj=gson.fromJson(response, UserPinsResponse.class);
                    Set<String> boardNames=responseObj.getBoardPins().keySet();
                    if(onSuccess!=null) {
                        onSuccess.accept(new ArrayList<>(boardNames));
                    }

                }, error -> Log.e(TAG, "nErrorResponse: Failed"));

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    /**
     * Get Pins of the User's given Board with Pinterest API + Scrape PDF Links
     **/
    public void requestPinsOfBoard(String user, String boardName, boolean getPinInfos,
                                   boolean scrapePDFlinks, Consumer<List<PinModel>> consumer) {
        String url=PIN_BASE_URL + "boards/" + user + "/" + boardName + "/pins";

        // Request a string response from the provided URL.
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url,
                response -> {
                    UserPinsResponse responseObj=gson.fromJson(response, UserPinsResponse.class);
                    List<PinModel> boardPins=responseObj.getBoardPins(boardName);
                    // Scrape PDF/Print Links with JSoup
                    if(scrapePDFlinks) {
                        scrapePDFLinks(boardPins);
                    }
                    if(getPinInfos) {
                        // Counter to wait until requestPinsInfos is done
                        Integer requestCount=1;
                        CountDownLatch requestCountDown=new CountDownLatch(requestCount);
                        requestPinsInfos(boardPins,null,requestCountDown);
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
                    else {
                        if(consumer!=null) {
                            consumer.accept(boardPins);
                        }
                    }
                }, error -> Log.e(TAG, "nErrorResponse: Failed"));
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    /**
     * Try to scrape PDF/Print Links from original Recipe Link
     **/
    public void scrapePDFLinks(List<PinModel> boardPins) {
        try {
            List<String> pdfLinks=new Tasks.ScrapePDFLinksTask()
                    .execute(boardPins.stream().map(PinModel::getLink).collect(Collectors.toList()))
                    .get();
            for(int i=0; i<boardPins.size(); i++) {
                boardPins.get(i).setPdfLink(pdfLinks.get(i));
            }
        }
        catch(ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get detailed Infos of Pins (fill Title if present)
     **/
    public void requestPinsInfos(List<PinModel> pins, Consumer<List<PinModel>> onSuccess, CountDownLatch requestCountDown) {
        String idsStr=pins.stream().map(PinModel::getId).collect(Collectors.joining(","));
        String url=PIN_BASE_URL + "pins/info/?pin_ids=" + idsStr;
        // Request a string response from the provided URL.
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url,
                response -> {
                    PinsInfosResponse responseObj=gson.fromJson(response, PinsInfosResponse.class);
                    for(int i=0; i<pins.size(); i++) {
                        RichMetaData metaData=responseObj.getData().get(i).getRichMetaData();
                        String title=pins.get(i).getTitle();
                        if(metaData!=null && metaData.getArticle()!=null) {
                            title=metaData.getArticle().getName();
                        }
                        pins.get(i).setTitle(title);
                    }
                    if(requestCountDown!=null) {
                        requestCountDown.countDown();
                    }
                    if(onSuccess!=null){
                        onSuccess.accept(pins);
                    }
                }, error -> Log.e(TAG, "nErrorResponse: Failed"));
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

}
