package com.philkes.pin2pdf.api.pinterest;

import android.content.Context;

import androidx.core.util.Consumer;

import android.util.ArraySet;
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
import com.philkes.pin2pdf.storage.local.entity.Pin;

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
                    List<String> boardNames=responseObj.getBoardPins().keySet()
                            .stream()
                            .sorted()
                            .collect(Collectors.toList());
                    if(onSuccess!=null) {
                        onSuccess.accept(boardNames);
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
                        requestPinsInfos(boardPins, null, requestCountDown);
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

    public static class PDFScrapeResult {
        private String pdfLink;
        private String title;

        public PDFScrapeResult(String pdfLink, String title) {
            this.pdfLink=pdfLink;
            this.title=title;
        }

        public PDFScrapeResult() {
        }

        public String getPdfLink() {
            return pdfLink;
        }

        public void setPdfLink(String pdfLink) {
            this.pdfLink=pdfLink;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title=title;
        }
    }

    /**
     * Try to scrape PDF/Print Links + better Title from original Recipe Link
     **/
    public void scrapePDFLinks(List<PinModel> boardPins) {
        try {
            List<PDFScrapeResult> pdfLinks=new Tasks.ScrapePDFLinksTask()
                    .execute(boardPins.stream().map(PinModel::getLink).collect(Collectors.toList()))
                    .get();
            for(int i=0; i<boardPins.size(); i++) {
                PDFScrapeResult res=pdfLinks.get(i);
                if(res==null) {
                    continue;
                }
                PinModel pin=boardPins.get(i);
                pin.setPdfLink(res.getPdfLink());
                //if(pin.getTitle() == null || pin.getTitle().isEmpty()|| pin.getTitle().equals(" ")){
                if(res.getTitle()!=null && !res.getTitle().isEmpty() && !res.getTitle().equals(" ")) {
                    pin.setTitle(res.getTitle());
                }
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
        String idsStr=pins.stream().map(PinModel::getPinId).collect(Collectors.joining(","));
        String url=PIN_BASE_URL + "pins/info/?pin_ids=" + idsStr;
        // Request a string response from the provided URL.
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url,
                response -> {
                    PinsInfosResponse responseObj=gson.fromJson(response, PinsInfosResponse.class);
                    for(int i=0; i<pins.size(); i++) {
                        RichMetaData metaData=responseObj.getData().get(i).getRichMetaData();
                        PinModel pin=pins.get(i);
                        String title=pin.getTitle();
                        if(metaData!=null && metaData.getArticle()!=null) {
                            title=metaData.getArticle().getName();
                        }
                        if(title==null || title.equals(" ") || title.isEmpty()) {
                            System.out.println();
                        }
                        else {
                            System.out.println(title);
                        }
                        pin.setTitle(title);
                    }
                    if(requestCountDown!=null) {
                        requestCountDown.countDown();
                    }
                    if(onSuccess!=null) {
                        onSuccess.accept(pins);
                    }
                }, error -> Log.e(TAG, "nErrorResponse: Failed"));
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

}
