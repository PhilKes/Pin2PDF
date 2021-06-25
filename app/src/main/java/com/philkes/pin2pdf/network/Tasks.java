package com.philkes.pin2pdf.network;

import android.os.AsyncTask;
import android.util.Log;

import com.philkes.pin2pdf.mock.MockData;
import com.philkes.pin2pdf.model.Pin;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Tasks {
    public static class GetPinsTask extends AsyncTask<String, Void, List<Pin>> {

        private static final String TAG="GetPinsTask";

        protected List<Pin> doInBackground(String... urls) {
            List<Pin> pins=new ArrayList<>();
            /**
             * TODOS:
             * GET https://www.pinterest.de/cryster0416/beilagen/
             * -> <section class="gridCentered" data-test-id="pinGrid"...>
             * -> <div class="Collection">
             *     -> every <div class="Collection-item"> is a Pin
             *     -> <div class="GrowthUnauthPinImage"> contains Img and Link
             *       -> <a href="/pin/{pinId}" title="{pinTitle}">
             *       -> <img src="{imgSrc}" >
             * **/

            // see https://stackoverflow.com/questions/5120171/extract-links-from-a-web-page
            try {
                Document doc=Jsoup.connect(urls[0])
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36")
                        .get();
                Elements pinsAHrefs=doc
                        .select("div.GrowthUnauthPinImage")
                        .select("a[href]");
                for(Element pinAHref : pinsAHrefs) {
                    String title=pinAHref.attr("title");
                    // If no title is found use img alt attribute instead
                    if(title.isEmpty() || title.equals(" ")) {
                        title=pinAHref.select("img").attr("alt").substring(0, 80);
                    }
                    String link=pinAHref.attr("href");
                    String imgUrl=pinAHref.select("img").attr("src");
                    Pin pin=new Pin(title, imgUrl, link,"Test","");
                    pins.add(pin);
                    String pinLink=pin.getLink();
                    doc=Jsoup.connect(pinLink)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36")
                            .get();
                    System.out.println(doc.toString());
                    Element domainHover=doc.selectFirst(".imageDomainLinkHover");
                    String recipeLink=domainHover.selectFirst("a[href]").attr("href");
                    pin.setLink(recipeLink);

                }

            }
            catch(IOException e) {
                e.printStackTrace();
            }
          /*  for(int i=0; i<3; i++) {
                pins.addAll(MockData.MockPins);
            }*/
            return pins;
        }

        protected void onPostExecute(List<Pin> result) {
            Log.d(TAG, String.format("Loaded %d Pins", result.size()));
        }
    }
   /* public static class ScrapeRecipePDFLink extends AsyncTask<String, Void, String> {

        private static final String TAG="GetPinsTask";

        protected String doInBackground(String... urls) {

        }
        protected void onPostExecute(String result) {
            Log.d(TAG, String.format("Loaded %d Pins",result ));
        }
    }*/
}
