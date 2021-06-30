package com.philkes.pin2pdf.api.pinterest

import android.content.Context
import android.util.Log
import androidx.core.util.Consumer
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.philkes.pin2pdf.api.Tasks.ScrapePDFLinksTask
import com.philkes.pin2pdf.api.pinterest.model.PinsInfosResponse
import com.philkes.pin2pdf.api.pinterest.model.UserPinsResponse
import com.philkes.pin2pdf.model.PinModel
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.stream.Collectors

class PinterestAPI private constructor(context: Context) {
    private val queue: RequestQueue
    fun requestBoardsOfUser(user: String, onSuccess: Consumer<List<String>>?) {
        val url = PIN_BASE_URL + "users/" + user + "/pins"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response: String? ->
                val responseObj = gson.fromJson(response, UserPinsResponse::class.java)
                val boardNames = responseObj.boardPins.keys
                    .stream()
                    .sorted()
                    .collect(Collectors.toList())
                onSuccess?.accept(boardNames)
            }) { error: VolleyError? -> Log.e(TAG, "nErrorResponse: Failed") }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    /**
     * Get Pins of the User's given Board with Pinterest API + Scrape PDF Links
     */
    fun requestPinsOfBoard(
        user: String, boardName: String, getPinInfos: Boolean,
        scrapePDFlinks: Boolean, consumer: Consumer<List<PinModel>>?
    ) {
        val url = PIN_BASE_URL + "boards/" + user + "/" + boardName + "/pins"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response: String? ->
                val responseObj = gson.fromJson(response, UserPinsResponse::class.java)
                val boardPins = responseObj.getBoardPins(boardName)
                // Scrape PDF/Print Links with JSoup
                if (scrapePDFlinks) {
                    scrapePDFLinks(boardPins)
                }
                if (getPinInfos) {
                    // Counter to wait until requestPinsInfos is done
                    val requestCount = 1
                    val requestCountDown = CountDownLatch(requestCount)
                    requestPinsInfos(boardPins, null, requestCountDown)
                    Thread {
                        try {
                            requestCountDown.await()
                            // Execute consumer if all Pins were loaded
                            consumer?.accept(boardPins)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    }.start()
                } else {
                    consumer?.accept(boardPins)
                }
            }) { error: VolleyError? -> Log.e(TAG, "nErrorResponse: Failed") }
        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    /**
     * Try to scrape PDF/Print Links from original Recipe Link
     */
    fun scrapePDFLinks(boardPins: List<PinModel>) {
        try {
            val pdfLinks = ScrapePDFLinksTask()
                .execute(boardPins.map { it.link!! })
                .get()
            for (i in boardPins.indices) {
                boardPins[i].pdfLink = pdfLinks[i]
            }
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /**
     * Get detailed Infos of Pins (fill Title if present)
     */
    fun requestPinsInfos(
        pins: List<PinModel>,
        onSuccess: Consumer<List<PinModel>>?,
        requestCountDown: CountDownLatch?
    ) {
        val idsStr = pins.stream().map(PinModel::id).collect(Collectors.joining(","))
        val url = PIN_BASE_URL + "pins/info/?pin_ids=" + idsStr
        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response: String? ->
                val responseObj = gson.fromJson(response, PinsInfosResponse::class.java)
                for (i in pins.indices) {
                    val metaData = responseObj.data?.get(i)?.richMetaData
                    var title = pins[i].title
                    if (metaData != null && metaData.article != null) {
                        title = metaData.article!!.name
                    }
                    pins[i].title = title
                }
                requestCountDown?.countDown()
                onSuccess?.accept(pins)
            }) { error: VolleyError? -> Log.e(TAG, "nErrorResponse: Failed") }
        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    companion object {
        private const val PIN_BASE_URL = "https://widgets.pinterest.com/v3/pidgets/"
        private const val TAG = "PinterestAPI"
        private val gson = Gson()
        private var instance: PinterestAPI? = null
        fun getInstance(context: Context): PinterestAPI? {
            if (instance == null) {
                instance = PinterestAPI(context)
            }
            return instance
        }
    }

    init {
        queue = Volley.newRequestQueue(context)
    }
}