package com.philkes.pin2pdf.api

import android.content.Context
import android.os.Build
import android.os.FileUtils
import android.print.PdfConverter
import android.util.Log
import androidx.core.util.Consumer
import com.philkes.pin2pdf.Util.convertToCompatibleFileName
import com.philkes.pin2pdf.Util.getUrlDomainName
import com.philkes.pin2pdf.fragment.boards.PinModel
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import org.apache.commons.validator.routines.UrlValidator
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.util.concurrent.Callable
import java.util.function.Predicate
import kotlin.io.path.Path


/**
 * Task to scrape a recipe's PDF/Print Link from a HTML page
 */
class ExtractRecipeToPDFTask(
    val context: Context,
    private val pins: List<PinModel>,
    val onProgress: Predicate<PinModel>
) :
    Callable<List<PinModel>> {

    val HTTP = "http"

    override fun call(): List<PinModel> {
        val futures: MutableList<PinModel> = ArrayList()
        // Try to scrape PDF/Print Link from all Recipe Webpages
        for (pin in pins) {
            var pdfLink: String? = null
            var doc: Document? = null
            val recipeLink: String
            doc = try {
                if (!urlValidator.isValid(pin.link)) {
                    throw Exception("Invalid URL found: ${pin.link}")
                }
                recipeLink = pin.link!!
                if (recipeLink.contains("youtube.com")) {
                    futures.add(pin);
                    continue;
                }
                Jsoup.connect(recipeLink)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36")
                    .get()
            } catch (e: Exception) {
                e.printStackTrace()
                futures.add(pin);
                continue;
            }
            // Get all Links (<a>)
            val pinsHrefs = doc!!
                .select("a[href]")
            // Find Link with Text containing "Print" or "Drucken"
            val pinHref = pinsHrefs.select(":contains(Print)").first()
                ?: pinsHrefs.select(":contains(Drucken)").first()
            if (pinHref != null) {
                findPrintUrlInElementAttribute(
                    pinHref,
                    "a",
                    "href",
                    listOf("drucken", "print"),
                    recipeLink,
                    false
                )
            }
            // List of elementType+attributeName and List of strings that can be contained in the attribute value
            val elementTypAttributePairs: List<Pair<Pair<String, String>, List<String>>> =
                listOf(
                    ("button" to "on") to listOf("drucken", "print"),
                    ("button" to "href") to listOf("drucken", "print"),
                    ("button" to "data-mv-print") to listOf("drucken", "print"),
                    ("a" to "href") to listOf("drucken", "print"),

                    // e.g https://www.springlane.de/magazin/rezeptideen/bananen-himbeer-smoothie/?display=pdf
                    ("a" to "href") to listOf("display=pdf"),
                )
            for (elementTypeAttributePair in elementTypAttributePairs) {
                pdfLink = findPrintUrlInElementAttribute(
                    doc,
                    elementTypeAttributePair.first.first,
                    elementTypeAttributePair.first.second,
                    elementTypeAttributePair.second,
                    recipeLink,
                    false
                )
                if (pdfLink !== null && (pdfLink.contains("()") || pdfLink.contains("javascript"))) {
                    pdfLink = null
                }
                if (pdfLink != null) {
                    break
                }
            }
            if (!urlValidator.isValid(pdfLink)) {
                pdfLink = null
            }
            val pdfFileName = convertToCompatibleFileName(pin.title!!)
            pdfLink = if (pdfLink != null) {
                if (checkIfWebpageIsPdf(pdfLink)) {
                    Log.d(TAG, "PdfLink is direct link to a .PDF $pdfLink")
                    dowloadFile(pdfFileName, pdfLink)
                } else {
                    Log.d(TAG, "PdfLink found for recipe: ${pin.link}\npdf: $pdfLink")
                    webPageToPdf(pdfFileName, pdfLink) ?: webPageToPdf(
                        pdfFileName,
                        pin.link!!
                    )
                }

            } else {
                Log.d(TAG, "No pdfLink found for recipe: ${pin.link}")
                webPageToPdf(pdfFileName, pin.link!!)
            }
            Log.d(TAG, "PDFLink: $pdfLink")
            pin.pdfLink = pdfLink
            // Show Progress and check if Scraping should be interrupted
            if (pdfLink != null && !onProgress.test(pin)) {
                break
            }
            futures.add(pin);
        }
        return futures
    }

    /**
     * Checks if Content-Type of URL is pdf
     */
    private fun checkIfWebpageIsPdf(url: String): Boolean {
        with(URL(url).openConnection() as HttpURLConnection) {
            requestMethod = "HEAD"  // optional default is GET
            Log.d(TAG, "HEAD $url Content-Type:${contentType}")
            return contentType !== null && contentType.contains("pdf")
        }
    }

    /**
     * Downloads file from url to local folder via HTTP
     */
    private fun dowloadFile(fileName: String, url: String): String {
        val filePath = "${context.filesDir}/${fileName}.pdf"
        with(URL(url).openConnection() as HttpURLConnection) {
            requestMethod = "GET"  // optional default is GET

            Log.d(TAG, "GET $url Content-Type:${contentType}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FileUtils.copy(inputStream, FileOutputStream(File(filePath)))
            } else {
                TODO("VERSION.SDK_INT < Q")
            }
            return filePath
        }
    }

    private fun getAbsoluteUrl(recipeLink: String, url: String): String {
        with(url) {
            if (!startsWith(HTTP)) {
                if (this[0] == '/') {
                    return "$HTTP://${getUrlDomainName(recipeLink)}/${this.substring(1)}"
                }
                return "$HTTP://${getUrlDomainName(recipeLink)}/$this"
            } else {
                return this
            }
        }
    }

    /**
     * Try to find link to recipe print in given elementType attribute value
     *
     * @param requireHttp whether or not the attribute value has to contain "http"
     */
    private fun findPrintUrlInElementAttribute(
        doc: Element,
        elementType: String,
        attributeName: String,
        containsValues: List<String>,
        recipeLink: String,
        requireHttp: Boolean
    ): String? {
        var printAnchor =
            doc.select(containsValues.map { "$elementType[$attributeName*=$it]" }
                .joinToString(", "))
        if (printAnchor.isNotEmpty()) {
            if (requireHttp) {
                printAnchor = printAnchor.first().select("$elementType[$attributeName*=http]");
                if (printAnchor.isNotEmpty()) {
                    return getAbsoluteUrl(
                        recipeLink,
                        extractUrl(printAnchor.first().attr(attributeName))
                    )
                }
            } else {
                val url = with(printAnchor.first().attr(attributeName)) {
                    if (this == "#" || contains("javascript") || contains("()")) {
                        null
                    }
                    extractUrl(this)
                }
                return if (url != null) getAbsoluteUrl(recipeLink, url) else null
            }
        }
        return null;
    }


    private fun extractUrl(str: String): String {
        with(str) {
            if (!contains(HTTP)) {
                return this
            }
            val endIdx = lastIndexOf("'");
            return substring(indexOf(HTTP), if (endIdx != -1) endIdx else lastIndex + 1)
        };
    }

    private fun convertToPdf(fileName: String, data: String): String? {
        return try {
            val filePath = "${context.filesDir}/${fileName}.pdf"
            Log.d(TAG, "Trying to print from WebPage $data to $filePath")
            val converter = PdfConverter(context, data, File(filePath))
            converter.convert(true)
            var waitCycles = 0;
            while (PdfConverter.isPrinting) {
                Thread.sleep(500);
                /*if (++waitCycles >= 20) {
                    Log.d(TAG,"Skipped printing because it took to long for $data to $filePath")
                    PdfConverter.isPrinting = false;
                    break;
                }*/
            }
            if (PdfConverter.printFailed) {
                Log.e(TAG, "Print failed for $data to $filePath")
                return null
            }

            val file = File(filePath)
            if (file.exists()) {
                val fileSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Files.size(Path(filePath))
                } else {
                    100
                }
                if (fileSize < 1) {
                    Log.e(TAG, "Print resulted in invalid PDF file for $data to $filePath")
                    file.delete();
                    return null
                }
            } else {
                Log.e(TAG, "Print failed for $data to $filePath")
                return null
            }
            return filePath
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            return null
        }

    }

    private fun webPageToPdf(pinFileName: String, url: String): String? {
        return synchronized(PdfConverter) { convertToPdf(pinFileName, url) }
    }

    companion object {
        private val urlValidator = UrlValidator()
        private const val TAG = "GetPinsTask"
    }

    init {
        PDFBoxResourceLoader.init(context);
    }

}