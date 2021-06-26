package com.philkes.pin2pdf.network;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.philkes.pin2pdf.Util.getUrlDomainName;

public class Tasks {

    public static class ScrapePDFLinksTask extends AsyncTask<List<String>, Void, List<String>> {

        private static final String TAG="GetPinsTask";

        protected List<String> doInBackground(List<String>... urls) {
            List<String> pdfLinks=new ArrayList<>(urls[0].size());
            for(int i=0; i<urls[0].size(); i++) {
                String recipeLink= urls[0].get(i);
                Document doc=null;
                try {
                    doc=Jsoup.connect(recipeLink)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36")
                            .get();
                }
                catch(IOException e) {
                    e.printStackTrace();
                    continue;
                }
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
                        pdfLink="http://" + getUrlDomainName(recipeLink) + pdfLink;
                          /*  System.out.println("ORIGINAL LINK: " + pin.getLink());
                            System.out.println("LINK: " + pdfLink);
                            System.out.println("Fused Link :" + (pdfLink));*/
                    }
                    if(!skipped) {
                        pdfLinks.set(i,pdfLink);
                    }
                }
            }
            return pdfLinks;
        }

        protected void onPostExecute(List<String> result) {
            Log.d(TAG,"ScrapeRecipePDFLinks done");
        }
    }
}
