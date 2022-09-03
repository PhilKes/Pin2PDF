package com.philkes.pin2pdf.fragment.boards

import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.philkes.pin2pdf.R
import com.philkes.pin2pdf.Settings
import com.philkes.pin2pdf.api.pinterest.PinterestAPI
import com.philkes.pin2pdf.storage.database.DBService
import com.philkes.pin2pdf.storage.database.Pin
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    lateinit var settings: Settings

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

    var destroyed: Boolean = false

    private val isFavoritesBoard: Boolean
        get() = boardId == BoardFragment.PIN2PDF_FAVORITES_BOARD_ID

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.board_fragment, container, false)
    }

    var viewSetup = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(requireArguments()) {
            boardName = getString(ARG_BOARD)
            boardId = getString(ARG_BOARD_ID)
        }
        setupUI(view)
        viewSetup = true;
        /*if (userVisibleHint) {
            fetchAllPins();
        }*/
        if (isFavoritesBoard) {
            dbService.loadFavoritePins().observe(
                viewLifecycleOwner
            ) { changedPins -> setChangedPins(changedPins) }
        } else {
            dbService.loadPinsOfBoard(boardName!!)
                .observe(
                    viewLifecycleOwner
                ) { changedPins -> setChangedPins(changedPins) }
        }

    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Log.d(TAG, "Board $boardName visible $isVisibleToUser allpins size ${allPins.size}")
        if (!destroyed && isVisibleToUser && ((viewSetup && allPins.isEmpty()) || isFavoritesBoard)) {
            fetchAllPins()
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
                fetchAllPins()
                isRefreshing = false
            }
        }
    }

    private var cancelScraping = false

    fun isCancelScraping(): Boolean {
        return cancelScraping;
    }

    fun fetchAllPins() {
        if (isFavoritesBoard) {
            return
        }
        Log.d(TAG, "Fetch pins of $boardName")
        val progress = ProgressDialog(context).apply {
            setTitle(String.format(getString(R.string.progress_pins_title), boardName))
            setMessage(getString(R.string.progress_pins_wait))
            setCancelable(false)
            setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", { i, a -> })
        }
        progress.setOnShowListener {
            val button: Button =
                (progress as ProgressDialog).getButton(DialogInterface.BUTTON_NEGATIVE)
            button.setOnClickListener {
                cancelScraping = true
                progress.setMessage("Cancelling PDF Scraping...")
            }
        }
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
                        val insertJobs = mutableListOf<Job>()
                        var pinCounter = 0;
                        cancelScraping = false
                        pinterestAPI.scrapePDFLinks(missingPins) { updatedPin ->
                            insertJobs.add(lifecycleScope.launch(Dispatchers.IO) {
                                dbService.insertPins(listOf(updatedPin)) {
                                }
                            })
                            activity!!.runOnUiThread {
                                progress.setMessage("${getString(R.string.progress_pins_scraping)}\n${++pinCounter} of ${missingPins.size} Pins")
                            }
                            return@scrapePDFLinks !isCancelScraping()
                        }
                        cancelScraping = false
                        lifecycleScope.launch {
                            insertJobs.forEach { it.join() }
                            activity!!.runOnUiThread {
                                progress.dismiss()
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

    private fun setChangedPins(changedPins: List<Pin>?) {
        var changedPinModels = listOf<PinModel>()
        if (changedPins != null) {
            changedPinModels = changedPins.map { it.toModel() }
        }
        updatePinsList(changedPinModels)
        allPins = changedPinModels
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

    fun reset() {
        val emptyList = listOf<PinModel>()
        updatePinsList(emptyList)
        this.allPins = emptyList
        destroyed= true
//        dbService.loadPinsOfBoard(boardName!!).removeObservers(viewLifecycleOwner)
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