package com.philkes.pin2pdf.api

import android.os.AsyncTask
import android.util.Log
import com.philkes.pin2pdf.Util.getUrlDomainName
import org.apache.commons.validator.routines.UrlValidator
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.*

class Tasks {
    class ScrapePDFLinksTask : AsyncTask<List<String>, Void?, List<String?>>() {
        protected override fun doInBackground(vararg urls: List<String>): List<String?> {
            val pdfLinks: MutableList<String?> = ArrayList()
            for (i in urls[0].indices) {
                val recipeLink = urls[0][i]
                var pdfLink: String? = null
                var doc: Document? = null
                try {
                    val urlValidator = UrlValidator()
                    if (!urlValidator.isValid(recipeLink)) {
                        throw Exception("Invalid URL found: $recipeLink")
                    }
                    doc = Jsoup.connect(recipeLink)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36")
                        .get()
                } catch (e: Exception) {
                    e.printStackTrace()
                    pdfLinks.add(null)
                    continue
                }
                // Get all Links (<a>)
                val pinsHrefs = doc.select("a[href]")
                // Find Link with Text containing "Print" or "Drucken"
                var pinHref = pinsHrefs
                    .select(":contains(Print)").first()
                if (pinHref == null) {
                    pinHref = pinsHrefs
                        .select(":contains(Drucken)").first()
                }
                if (pinHref != null) {
                    pdfLink = pinHref.attr("href")
                    // Check if found Link is actual link (not e.g. javascript code)
                    pdfLink =
                        if (pdfLink == "#" || pdfLink.contains("javascript") || pdfLink.contains("()")) null else pdfLink
                    if (pdfLink != null && pdfLink.startsWith("/")) {
                        pdfLink = "http://" + getUrlDomainName(recipeLink) + pdfLink
                        /*  System.out.println("ORIGINAL LINK: " + pin.getLink());
                            System.out.println("LINK: " + pdfLink);
                            System.out.println("Fused Link :" + (pdfLink));*/
                    }
                }
                println("PDFLink: $pdfLink")
                pdfLinks.add(pdfLink)
            }
            return pdfLinks
        }

        override fun onPostExecute(result: List<String?>) {
            Log.d(TAG, "ScrapeRecipePDFLinks done")
        }

        companion object {
            private const val TAG = "GetPinsTask"
        }


    }
}