package com.philkes.pin2pdf.api;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.philkes.pin2pdf.Util.getUrlDomainName;

public class Tasks {

    public static class ScrapePDFLinksTask extends AsyncTask<List<String>, Void, List<String>> {

        private static final String TAG="GetPinsTask";

        protected List<String> doInBackground(List<String>... urls) {
            List<String> pdfLinks=new ArrayList<>();

            for(int i=0; i<urls[0].size(); i++) {
                String recipeLink=urls[0].get(i);
                String pdfLink=null;

                Document doc=null;
                try {
                    UrlValidator urlValidator=new UrlValidator();
                    if(!urlValidator.isValid(recipeLink)) {
                        throw new Exception("Invalid URL found: " + recipeLink);
                    }
                    if(recipeLink.contains("youtube.com")) {
                        throw new Exception("Recipe from YouTube skipped: " + recipeLink);
                    }
                    doc=Jsoup.connect(recipeLink)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36")
                            .get();
                }
                catch(Exception e) {
                    e.printStackTrace();
                    pdfLinks.add(null);
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
                    pdfLink=pinHref.attr("href");
                    // Check if found Link is actual link (not e.g. javascript code)
                    pdfLink=(pdfLink.equals("#") || pdfLink.contains("javascript") || pdfLink.contains("()")) ? null : pdfLink;
                    if(pdfLink!=null && pdfLink.startsWith("/")) {
                        pdfLink="http://" + getUrlDomainName(recipeLink) + pdfLink;
                          /*  System.out.println("ORIGINAL LINK: " + pin.getLink());
                            System.out.println("LINK: " + pdfLink);
                            System.out.println("Fused Link :" + (pdfLink));*/
                    }
                }
                if(pdfLink==null) {
                    // TODO Try to Scrape Instructions HTML
                    // and save as .pdf locally
                    // Try to find recipe anchor tag
                    Elements recipeAnchors=doc
                            .select("[id~=.*recipe.*]");
                    if(recipeAnchors.isEmpty()) {
                        recipeAnchors=doc
                                .select("[id~=.*rezept.*]");
                    }
                    if(recipeAnchors.isEmpty()) {
                        recipeAnchors=doc
                                .select("[id~=.*print.*]");
                    }
                    if(!recipeAnchors.isEmpty()) {
                        Element bestAnchor=null;
                        for(Element anchor : recipeAnchors) {
                            String t=anchor.attr("id");
                            if(!t.contains("button") && !t.contains("btn") && !t.contains("link") && !t.contains("logo") && !t.contains("icon")) {
                                bestAnchor=anchor;
                                break;
                            }
                        }
                        if(bestAnchor!=null) {
                            String tag=bestAnchor
                                    .attr("id");
                            System.out.println("Tag found: " + tag);
                            pdfLink=recipeLink + "#" + tag;
                        }
                    }
                }
                System.out.println("PDFLink: " + pdfLink);
                pdfLinks.add(pdfLink);
            }
            return pdfLinks;
        }

        protected void onPostExecute(List<String> result) {
            Log.d(TAG, "ScrapeRecipePDFLinks done");
        }
    }
}
