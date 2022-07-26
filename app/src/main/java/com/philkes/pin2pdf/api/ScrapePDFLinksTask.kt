package com.philkes.pin2pdf.api

import android.util.Log
import com.philkes.pin2pdf.Util.getUrlDomainName
import org.apache.commons.validator.routines.UrlValidator
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.concurrent.Callable

/**
 * Task to scrape a recipe's PDF/Print Link from a HTML page
 */
class ScrapePDFLinksTask(private val urls: List<String?>) : Callable<List<String?>> {

    override fun call(): List<String?> {
        val pdfLinks: MutableList<String?> = ArrayList()

        // Try to scrape PDF/Print Link from all Recipe Webpages
        for (i in urls.indices) {
            var pdfLink: String? = null
            var doc: Document? = null
            val recipeLink: String
            doc = try {
                if (!urlValidator.isValid(urls[i])) {
                    throw Exception("Invalid URL found: $urls[i]")
                }
                recipeLink = urls[i]!!
                if (recipeLink.contains("youtube.com")) {
                    pdfLinks.add(null)
                    continue
                }
                Jsoup.connect(recipeLink)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36")
                    .get()
            } catch (e: Exception) {
                e.printStackTrace()
                pdfLinks.add(null)
                continue
            }
            // Get all Links (<a>)
            val pinsHrefs = doc!!
                .select("a[href]")
            // Find Link with Text containing "Print" or "Drucken"
            val pinHref = pinsHrefs.select(":contains(Print)").first()
                ?: pinsHrefs.select(":contains(Drucken)").first()
            if (pinHref != null) {
                pdfLink = pinHref.attr("href")
                // Check if found Link is actual link (not e.g. javascript code)
                pdfLink =
                    if (pdfLink == "#" || pdfLink.contains("javascript") || pdfLink.contains("()")) null else pdfLink
                if (pdfLink != null && pdfLink.startsWith("/")) {
                    pdfLink = "http://" + getUrlDomainName(recipeLink) + pdfLink
                }
            }
            if (pdfLink == null) {
                // Try to find recipe anchor tag
                var recipeAnchors = doc
                    .select("[id~=.*recipe.*]")

                if (recipeAnchors.isEmpty()) {
                    recipeAnchors = doc
                        .select("[id~=.*rezept.*]")
                }
                if (recipeAnchors.isEmpty()) {
                    recipeAnchors = doc
                        .select("[id~=.*print.*]")
                }
                if (!recipeAnchors.isEmpty()) {
                    var bestAnchor: Element? = null
                    for (anchor in recipeAnchors) {
                        val t = anchor.attr("id")
                        if (with(t) {
                                !contains("button") && !contains("btn") &&
                                        !contains("link") && !contains("logo") &&
                                        !contains("icon")
                            }
                        ) {
                            bestAnchor = anchor
                            break
                        }
                    }
                    if (bestAnchor != null) {
                        val tag = bestAnchor
                            .attr("id")
                        Log.d(TAG, String.format("Tag found: %s", tag))
                        pdfLink = "$recipeLink#$tag"
                    }
                }
            }
            if (!urlValidator.isValid(pdfLink)) {
                pdfLink = null
            }
            Log.d(TAG, String.format("PDFLink: %s", pdfLink))
            pdfLinks.add(pdfLink)
        }
        return pdfLinks
    }


    companion object {
        private val urlValidator = UrlValidator()
        private const val TAG = "GetPinsTask"
    }

}