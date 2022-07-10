package com.philkes.pin2pdf.fragment.boards

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.philkes.pin2pdf.R
import com.philkes.pin2pdf.adapter.PinAdapter
import com.philkes.pin2pdf.api.pinterest.PinterestAPI
import com.philkes.pin2pdf.model.PinModel
import com.philkes.pin2pdf.storage.local.service.DBService
import java.util.*

/**
 * Fragments inside of BoardFragment
 * A BoardObjectFragment represents 1 Pinterest Board with its Pins (1 Tab)
 */
class BoardObjectFragment : Fragment() {
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var amountPinsTextView: TextView
    private lateinit var pinListView: RecyclerView
    private lateinit var pinListViewAdapter: RecyclerView.Adapter<*>
    private var currentPins: MutableList<PinModel> = ArrayList()
    private var allPins: List<PinModel> = ArrayList()
    private var boardName: String? = null
    private var boardId: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.board_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(arguments!!) {
            boardName = getString(ARG_BOARD)
            boardId = getString(ARG_BOARD_ID)
        }
        setupUI(view)
        loadPins()
    }

    private fun setupUI(view: View) {
        currentPins = ArrayList()
        amountPinsTextView = view.findViewById(R.id.amount_pins_text)
        pinListViewAdapter = PinAdapter(currentPins)
        pinListView = view.findViewById<RecyclerView?>(R.id.pins_list).apply {
            adapter = pinListViewAdapter
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(view.context)
        }
        refreshLayout = view.findViewById<SwipeRefreshLayout?>(R.id.refresh_layout).apply {
            setOnRefreshListener {
                loadPins()
                isRefreshing = false
            }
        }
    }

    private fun loadPins() {
        val progress = ProgressDialog(context).apply {
            setTitle(String.format(getString(R.string.progress_pins_title), boardName))
            setMessage(getString(R.string.progress_pins_wait))
            setCancelable(false) // disable dismiss by tapping outside of the dialog
            show()
        }

        val api: PinterestAPI = PinterestAPI.getInstance(context!!)
        api.requestPinsOfBoard(boardId, false) { pins: List<PinModel> ->
            Log.d(TAG, String.format("All Pins: %d", pins.size))
            val dbService = DBService.getInstance(context!!)
            // Try to load all Pins from local DB
            dbService.loadPins(pins.map { obj: PinModel -> obj.pinId!! }) { loadedPins: List<PinModel> ->
                Log.d(TAG, String.format("Loaded Pins: %d", loadedPins.size))
                // Check if any Pins weren't loaded from local DB
                val missingPins: List<PinModel> =
                    ArrayList(pins).filter { pinModel: PinModel -> !loadedPins.any { pinModel.pinId == it.pinId } }
                Log.d(TAG, String.format("Missing Pins: %d", missingPins.size))
                // Fetch missing Pins from Pinterest API/Scraper
                if (missingPins.isNotEmpty()) {
                    activity!!.runOnUiThread {
                        progress.setMessage(getString(R.string.progress_pins_scraping))
                    }
                    api.scrapePDFLinks(missingPins) { updatedPins ->
                        dbService.insertPins(updatedPins) {
                            // Reload all Pins from Local DB
                            dbService.loadPins(
                                pins.map { obj: PinModel -> obj.pinId!! }) { allPins: List<PinModel> ->
                                updatePinsList(allPins)
                                // Store all available Pins separately for filtering
                                this.allPins = allPins
                                activity!!.runOnUiThread {
                                    progress.dismiss()
                                }
                            }
                        }
                    }

                } else {
                    updatePinsList(loadedPins)
                    allPins = loadedPins
                    progress.dismiss()
                }
            }
        }
    }

    private fun updatePinsList(pins: List<PinModel>) {
        with(currentPins) {
            clear()
            addAll(pins)
        }
        activity!!.runOnUiThread {
            amountPinsTextView.text =
                String.format(getString(R.string.amount_pins_text_template), allPins.size)
            pinListViewAdapter.notifyDataSetChanged()
        }
    }

    fun setFilter(filter: String) {
        val filteredPins: MutableList<PinModel> = ArrayList()
        // Find all Pins that match Filter from allPins
        for (pin in allPins) {
            if (pin.title!!.lowercase(Locale.getDefault())
                    .contains(filter.lowercase(Locale.getDefault()))
            ) {
                filteredPins.add(pin)
            }
        }
        updatePinsList(filteredPins)
    }

    companion object {
        private const val TAG = "BoardObjectFragment"
        const val ARG_BOARD = "board"
        const val ARG_BOARD_ID = "boardId"
    }
}