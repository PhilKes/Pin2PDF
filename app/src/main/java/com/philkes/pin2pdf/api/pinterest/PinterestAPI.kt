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
import com.philkes.pin2pdf.api.ScrapePDFLinksTask
import com.philkes.pin2pdf.api.TaskRunner
import com.philkes.pin2pdf.api.pinterest.model.BoardResponse
import com.philkes.pin2pdf.api.pinterest.model.PinResponse
import com.philkes.pin2pdf.api.pinterest.model.UserBoardsResponse
import com.philkes.pin2pdf.api.pinterest.model.UserPinsResponse
import com.philkes.pin2pdf.fragment.boards.PinModel
import java.util.concurrent.ExecutionException

/**
 * Methods to call the Pinterest JSON API for access to user's Boards + Pins
 */
class PinterestAPI constructor(context: Context) {
    private val queue: RequestQueue

    private val taskRunner: TaskRunner = TaskRunner()

    /**
     * Fetch Boards of given User from Pinterest JSON API
     */
    fun requestBoardsOfUser(
        user: String?,
        onSuccess: Consumer<List<BoardResponse?>>?,
        onError: Consumer<VolleyError?>
    ) {
        val url = String.format(PIN_GET_BOARDS_URL, user)
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response: String? ->
                val responseObj = gson.fromJson(response, UserBoardsResponse::class.java)
                val boardResponses: List<BoardResponse> =
                    responseObj.resource_response!!.data!!.sortedBy { it!!.name }.map { it!! }
                onSuccess?.accept(boardResponses)
            }) { t: VolleyError? -> onError.accept(t) }
        queue.add(stringRequest)
    }

    /**
     * Get Pins of the User's given Board with Pinterest API + Scrape PDF Links
     */
    fun requestPinsOfBoard(
        boardId: String?,
        scrapePDFlinks: Boolean,
        consumer: Consumer<List<PinModel>>?
    ) {
        val url = PIN_PINS_OF_BOARD_URL.format(boardId)

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response: String? ->
                val responseObj = gson.fromJson(response, UserPinsResponse::class.java)
                val boardPins = responseObj.resourceResponse!!.data!!
                    .filter { pin: PinResponse? -> pin!!.board != null }
                    .map { obj: PinResponse? -> obj!!.toPin() }
                // Scrape PDF/Print Links with JSoup
                if (scrapePDFlinks) {
                    scrapePDFLinks(boardPins, consumer)
                } else {
                    consumer?.accept(boardPins)
                }
            }) { error: VolleyError? ->
            Log.e(TAG, String.format("nErrorResponse: Failed: %s", error))
        }
        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    /**
     * Try to scrape PDF/Print Links
     */
    fun scrapePDFLinks(boardPins: List<PinModel>, consumer: Consumer<List<PinModel>>?) {
        try {
            taskRunner.executeAsync(
                ScrapePDFLinksTask(boardPins.map { obj: PinModel? -> obj?.link })
            ) { pdfLinks ->
                for (i in boardPins.indices) {
                    val res = pdfLinks[i] ?: continue
                    val pin = boardPins[i]
                    pin.pdfLink = res
                }
                consumer?.accept(boardPins)
            }
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }


    companion object {
        private const val PIN_GET_BOARDS_URL =
            "https://www.pinterest.com/_ngjs/resource/BoardsResource/get/?data={\"options\":{\"username\":\"%s\",\"page_size\":250,\"prepend\":false}}"
        private const val PIN_PINS_OF_BOARD_URL =
            "https://www.pinterest.de/resource/BoardFeedResource/get/?data={\"options\":{\"board_id\":\"%s\",\"field_set_key\":\"react_grid_pin\",\"page_size\":250}}"
        private const val TAG = "PinterestAPI"
        private val gson = Gson()
    }

    init {
        queue = Volley.newRequestQueue(context)
    }
}