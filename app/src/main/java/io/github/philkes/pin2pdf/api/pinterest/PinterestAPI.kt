package io.github.philkes.pin2pdf.api.pinterest

import android.content.Context
import android.util.Log
import androidx.core.util.Consumer
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import io.github.philkes.pin2pdf.api.ExtractRecipeToPDFTask
import io.github.philkes.pin2pdf.api.pinterest.model.BoardResponse
import io.github.philkes.pin2pdf.api.pinterest.model.PinResponse
import io.github.philkes.pin2pdf.api.pinterest.model.UserBoardsResponse
import io.github.philkes.pin2pdf.api.pinterest.model.UserPinsResponse
import io.github.philkes.pin2pdf.fragment.boards.PinModel
import java.util.function.Predicate

/**
 * Methods to call the Pinterest JSON API for access to user's Boards + Pins
 */
class PinterestAPI constructor(val context: Context) {
    private val queue: RequestQueue

    /**
     * Fetch Boards of given User from Pinterest JSON API
     */
    fun requestBoardsOfUser(
        user: String?,
        onSuccess: Consumer<List<BoardResponse>>?,
        onError: Consumer<VolleyError>?
    ) {
        val url = String.format(PIN_GET_BOARDS_URL, user)
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response: String? ->
                val responseObj = gson.fromJson(response, UserBoardsResponse::class.java)
                val boardResponses: List<BoardResponse> =
                    responseObj.resource_response!!.data!!.sortedBy { it!!.name }.map { it!! }
                onSuccess?.accept(boardResponses)
            }) { t: VolleyError? -> onError?.accept(t) }
        queue.add(stringRequest)
    }

    /**
     * Get Pins of the User's given Board with Pinterest API + Scrape PDF Links
     */
    fun requestPinsOfBoard(boardId: String?, consumer: Consumer<List<PinModel>>?) {
        val url = PIN_PINS_OF_BOARD_URL.format(boardId)

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response: String? ->
                val responseObj = gson.fromJson(response, UserPinsResponse::class.java)
                val boardPins = responseObj.resourceResponse!!.data!!
                    .filter { pin: PinResponse? -> pin!!.board != null && pin.link != null }
                    .map { obj: PinResponse? -> obj!!.toPin() }
                consumer?.accept(boardPins)
            }) { error: VolleyError? ->
            Log.e(TAG, "nErrorResponse: Failed: $error")
        }
        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    /**
     * Try to scrape PDF/Print Links
     * @param onProgress is called when a PDF has been scraped for a pin, returns whether or not scraping has been cancelled
     */
    fun scrapePDFLinks(boardPins: List<PinModel>, onProgress: Predicate<PinModel>): List<PinModel> {
        return ExtractRecipeToPDFTask(context, boardPins, onProgress).call();
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