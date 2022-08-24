package com.philkes.pin2pdf.fragment.boards

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.philkes.pin2pdf.Pin2PDFModule
import com.philkes.pin2pdf.R
import com.philkes.pin2pdf.api.pinterest.PinterestAPI
import com.philkes.pin2pdf.storage.database.DBService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * Fragments inside of BoardFragment
 * A BoardObjectFragment represents 1 Pinterest Board with its Pins (1 Tab)
 */
@AndroidEntryPoint
class BoardObjectFragment : Fragment() {
    @Inject
    lateinit var dbService: DBService

    @Inject
    lateinit var settings: Pin2PDFModule.Settings

    @Inject
    lateinit var pinterestAPI: PinterestAPI

    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var amountPinsTextView: TextView
    private lateinit var pinListView: RecyclerViewEmptySupport
    private lateinit var pinListViewAdapter: RecyclerView.Adapter<*>
    private var currentPins: MutableList<PinModel> = ArrayList()
    private var allPins: List<PinModel> = ArrayList()
    private var boardId: String? = null
    var boardName: String? = null

    val isFavoritesBoard: Boolean
        get() = boardId == BoardFragment.PIN2PDF_FAVORITES_BOARD_ID

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.board_fragment, container, false)
    }

    var viewSetup = false
    var pinsLoaded = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(arguments!!) {
            boardName = getString(ARG_BOARD)
            boardId = getString(ARG_BOARD_ID)
        }
        setupUI(view)
        viewSetup = true;
        if (userVisibleHint) {
            loadPins();
        }
//        loadPins()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && ((viewSetup && !pinsLoaded) || isFavoritesBoard)) {
            loadPins()
        }
    }

    private fun setupUI(view: View) {
        currentPins = ArrayList()
        amountPinsTextView = view.findViewById(R.id.amount_pins_text)
        pinListViewAdapter = PinAdapter(currentPins) {
            lifecycleScope.launch {
                dbService.updatePin(it) {
                    if (isFavoritesBoard && !it.isFavorite) {
                        val indexOf = currentPins.indexOf(it)
                        currentPins.remove(it)
                        pinListViewAdapter.notifyItemRemoved(indexOf)
                    }
                }
            }
        }
        pinListView = view.findViewById<RecyclerViewEmptySupport>(R.id.pins_list).apply {
            adapter = pinListViewAdapter
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(view.context)
            emptyView = view.findViewById(R.id.pins_list_empty)
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
/*           TODO setCancelable(true) // disable dismiss by tapping outside of the dialog
            setButton(
                DialogInterface.BUTTON_NEGATIVE,
                "Cancel"
            ) { dialogInterface, i ->
                lifecycleScope.launch {
                    settings.resetUser(ownerActivity)
                }
            };*/
        }

        if (isFavoritesBoard) {
            lifecycleScope.launch(Dispatchers.IO) {
                dbService.loadFavoritePins { pins ->
                    updatePinsList(pins)
                    allPins = pins
                }
            }
        } else {
            progress.show()
            pinterestAPI.requestPinsOfBoard(boardId) { pins: List<PinModel> ->
                Log.d(TAG, "All Pins: ${pins.size}")
                // Try to load all Pins from local DB
                // important: use Dispatchers.IO to not block the UI Thread
                lifecycleScope.launch(Dispatchers.IO) {
                    dbService.loadPins(pins.map { obj: PinModel -> obj.pinId!! }) { loadedPins: List<PinModel> ->
                        Log.d(TAG, "Loaded Pins: ${loadedPins.size}")
                        // Check if any Pins weren't loaded from local DB
                        val missingPins: List<PinModel> =
                            ArrayList(pins).filter { pinModel: PinModel -> !loadedPins.any { pinModel.pinId == it.pinId } }
                        Log.d(TAG, "Missing Pins: ${missingPins.size}")
                        // Fetch missing Pins from Pinterest API/Scraper
                        if (missingPins.isNotEmpty()) {
                            activity!!.runOnUiThread {
                                progress.setMessage("${getString(R.string.progress_pins_scraping)}\n${missingPins.size} Pins")
                            }
                            val updatedPins = pinterestAPI.scrapePDFLinks(missingPins)
                            lifecycleScope.launch(Dispatchers.IO) {
                                dbService.insertPins(updatedPins) {
                                    lifecycleScope.launch {
                                        // Reload all Pins from Local DB
                                        dbService.loadPins(
                                            pins.map { obj: PinModel -> obj.pinId!! }) { allPins: List<PinModel> ->
                                            updatePinsList(allPins)
                                            // Store all available Pins separately for filtering
                                            this@BoardObjectFragment.allPins = allPins
                                            activity!!.runOnUiThread {
                                                progress.dismiss()
                                            }
                                        }
                                    }
                                }
                            }

                        } else {
                            updatePinsList(loadedPins)
                            allPins = loadedPins
                            activity!!.runOnUiThread {
                                progress.dismiss()
                            }
                        }
                    }
                }
            }

        }
        pinsLoaded = true;
    }

    private fun updatePinsList(pins: List<PinModel>) {
        with(currentPins) {
            clear()
            addAll(pins)
        }
        activity!!.runOnUiThread {
            if (!boardId.equals(BoardFragment.PIN2PDF_FAVORITES_BOARD_ID)) {
                amountPinsTextView.text =
                    String.format(getString(R.string.amount_pins_text_template), currentPins.size)
            } else {
                amountPinsTextView.text = ""
            }

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