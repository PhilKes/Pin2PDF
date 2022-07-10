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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.philkes.pin2pdf.R
import com.philkes.pin2pdf.adapter.PinAdapter
import com.philkes.pin2pdf.api.pinterest.PinterestAPI
import com.philkes.pin2pdf.model.PinModel
import com.philkes.pin2pdf.storage.local.service.DBService.Companion.getInstance
import java.util.*
import java.util.stream.Collectors

/**
 * Fragments inside of BoardFragment
 * A BoardObjectFragment represents 1 Pinterest Board with its Pins (1 Tab)
 */
class BoardObjectFragment : Fragment() {
    private var refreshLayout: SwipeRefreshLayout? = null
    private var amountPinsTextView: TextView? = null
    private var pinListView: RecyclerView? = null
    private var pinListViewAdapter: RecyclerView.Adapter<*>? = null
    private var currentPins: MutableList<PinModel?> = ArrayList()
    private var allPins: List<PinModel?>? = ArrayList()
    private var boardName: String? = null
    private var boardId: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.board_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args = arguments
        boardName = args!!.getString(ARG_BOARD)
        boardId = args.getString(ARG_BOARD_ID)
        setupUI(view)
        loadPins()
    }

    private fun setupUI(view: View) {
        currentPins = ArrayList()
        amountPinsTextView = view.findViewById(R.id.amount_pins_text)
        pinListView = view.findViewById(R.id.pins_list)
        pinListViewAdapter = PinAdapter(currentPins)
        pinListView!!.adapter = pinListViewAdapter
        pinListView!!.itemAnimator = DefaultItemAnimator()
        val listViewManager = LinearLayoutManager(view.context)
        pinListView!!.layoutManager = listViewManager
        refreshLayout = view.findViewById(R.id.refresh_layout)
        refreshLayout!!.setOnRefreshListener({
            loadPins()
            refreshLayout!!.setRefreshing(false)
        })
    }

    private fun loadPins() {
        val progress = ProgressDialog(context)
        progress.setTitle(String.format(getString(R.string.progress_pins_title), boardName))
        progress.setMessage(getString(R.string.progress_pins_wait))
        progress.setCancelable(false) // disable dismiss by tapping outside of the dialog
        progress.show()
        val api: PinterestAPI = PinterestAPI.Companion.getInstance(context)
        api.requestPinsOfBoard(boardId, false) { pins: List<PinModel?> ->
            Log.d(TAG, String.format("All Pins: %d", pins.size))
            val dbService = getInstance(context!!)
            // Try to load all Pins from local DB
            dbService!!.loadPins(
                pins.map { obj: PinModel? -> obj!!.pinId }
            ) { loadedPins: List<PinModel?>? ->
                Log.d(TAG, String.format("Loaded Pins: %d", loadedPins!!.size))
                // Check if any Pins weren't loaded from local DB
                val missingPins: MutableList<PinModel> = ArrayList(pins)
                missingPins.removeIf { pinModel: PinModel? ->
                    loadedPins.stream()
                        .anyMatch { pin: PinModel? -> pin!!.pinId == pinModel!!.pinId }
                }
                Log.d(TAG, String.format("Missing Pins: %d", missingPins.size))
                // Fetch missing Pins from Pinterest API/Scraper
                if (!missingPins.isEmpty()) {
                    api.scrapePDFLinks(missingPins)
                    dbService.insertPins(missingPins, Runnable {
                        // Reload all Pins from Local DB
                        dbService.loadPins(
                            pins.map { obj: PinModel? -> obj!!.pinId }) { allPins: List<PinModel?>? ->
                            updatePinsList(allPins)
                            // Store all available Pins separately for filtering
                            this.allPins = allPins
                            progress.dismiss()
                        }
                    })
                } else {
                    updatePinsList(loadedPins)
                    allPins = loadedPins
                    progress.dismiss()
                }
            }
        }
    }

    private fun updatePinsList(pins: List<PinModel?>?) {
        currentPins.clear()
        currentPins.addAll(pins!!)
        activity!!.runOnUiThread {
            amountPinsTextView!!.text =
                String.format(getString(R.string.amount_pins_text_template), allPins!!.size)
            pinListViewAdapter!!.notifyDataSetChanged()
        }
    }

    fun setFilter(filter: String) {
        val filteredPins: MutableList<PinModel?> = ArrayList()
        // Find all Pins that match Filter from allPins
        for (pin in allPins!!) {
            if (pin!!.title!!.lowercase(Locale.getDefault())
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