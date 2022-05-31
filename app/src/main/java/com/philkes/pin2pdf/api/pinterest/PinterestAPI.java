package com.philkes.pin2pdf.api.pinterest;

import android.content.Context;
import android.util.Log;

import androidx.core.util.Consumer;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.philkes.pin2pdf.api.Tasks;
import com.philkes.pin2pdf.api.pinterest.model.BoardResponse;
import com.philkes.pin2pdf.api.pinterest.model.PinResponse;
import com.philkes.pin2pdf.api.pinterest.model.UserBoardsResponse;
import com.philkes.pin2pdf.api.pinterest.model.UserPinsResponse;
import com.philkes.pin2pdf.model.PinModel;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Methods to call the Pinterest JSON API for access to user's Boards + Pins
 */
public class PinterestAPI {
    private static final String PIN_GET_BOARDS_URL="https://www.pinterest.com/_ngjs/resource/BoardsResource/get/?data={\"options\":{\"username\":\"%s\",\"page_size\":250,\"prepend\":false}}";
    private static final String PIN_PINS_OF_BOARD_URL="https://www.pinterest.de/resource/BoardFeedResource/get/?data={\"options\":{\"board_id\":\"%s\",\"field_set_key\":\"react_grid_pin\",\"page_size\":250}}";

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

    /**
     * Fetch Boards of given User from Pinterest JSON API
     */
    public void requestBoardsOfUser(String user, Consumer<List<BoardResponse>> onSuccess, Consumer<VolleyError> onError) {
        String url=String.format(PIN_GET_BOARDS_URL, user);

        StringRequest stringRequest=new StringRequest(Request.Method.GET, url,
                response -> {
                    UserBoardsResponse responseObj=gson.fromJson(response, UserBoardsResponse.class);
                    List<BoardResponse> boardResponses=responseObj.getResource_response().getData().stream()
                            .sorted(Comparator.comparing(BoardResponse::getName))
                            .collect(Collectors.toList());
                    if(onSuccess!=null) {
                        onSuccess.accept(boardResponses);
                    }

                }, onError::accept);
        queue.add(stringRequest);
    }

    /**
     * Get Pins of the User's given Board with Pinterest API + Scrape PDF Links
     **/
    public void requestPinsOfBoard(String boardId, boolean scrapePDFlinks, Consumer<List<PinModel>> consumer) {
        String url=String.format(PIN_PINS_OF_BOARD_URL, boardId);

        // Request a string response from the provided URL.
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url,
                response -> {
                    UserPinsResponse responseObj=gson.fromJson(response, UserPinsResponse.class);
                    List<PinModel> boardPins=responseObj.getResourceResponse().getData().stream()
                            .filter(pin -> pin.getBoard()!=null)
                            .map(PinResponse::toPin)
                            .collect(Collectors.toList());
                    // Scrape PDF/Print Links with JSoup
                    if(scrapePDFlinks) {
                        scrapePDFLinks(boardPins);
                    }
                    else {
                        if(consumer!=null) {
                            consumer.accept(boardPins);
                        }
                    }
                }, error -> Log.e(TAG, String.format("nErrorResponse: Failed: %s", error)));
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
            }
        }
        catch(ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
