package io.github.philkes.pin2pdf.fragment.boards

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
import io.github.philkes.pin2pdf.R
import io.github.philkes.pin2pdf.Settings
import io.github.philkes.pin2pdf.api.pinterest.PinterestAPI
import io.github.philkes.pin2pdf.storage.database.DBService
import io.github.philkes.pin2pdf.storage.database.Pin
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
class SearchObjectFragment : Fragment() {
    @Inject
    lateinit var dbService: DBService

    @Inject
    lateinit var settings: Settings

    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var amountPinsTextView: TextView
    private lateinit var pinListView: RecyclerViewEmptySupport
    private lateinit var pinListViewAdapter: RecyclerView.Adapter<*>
    private var currentPins: MutableList<PinModel> = ArrayList()
    private var allPins: List<PinModel> = ArrayList()
    var query: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.board_fragment, container, false)
    }

    var viewSetup = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupUI(view)
        viewSetup = true
//        dbService.loadAllPins()
//            .observe(
//                viewLifecycleOwner
//            ) { changedPins -> setChangedPins(changedPins) }
        fetchAllPins()
        (pinListView.layoutManager as LinearLayoutManager).stackFromEnd = true
    }

    private fun setupUI(view: View) {
        currentPins = ArrayList()
        amountPinsTextView = view.findViewById(R.id.amount_pins_text)
        pinListViewAdapter = PinAdapter(currentPins) {
            lifecycleScope.launch {}
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

    private val notReachablePins = mutableListOf<PinModel>()

    fun isCancelScraping(): Boolean {
        return cancelScraping;
    }

    fun fetchAllPins() {
        notReachablePins.clear()
        Log.d(TAG, "Fetch pins of search")
        val progress = ProgressDialog(context).apply {
            setTitle(getString(R.string.progress_pins_title))
            setMessage(getString(R.string.progress_pins_wait))
            setCancelable(false)
            setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", { i, a -> })
        }
        lateinit var button: Button
        progress.setOnShowListener {
            button = (progress as ProgressDialog).getButton(DialogInterface.BUTTON_NEGATIVE)
            button.setOnClickListener {
                cancelScraping = true
                progress.setMessage("Cancelling Searching...")
            }
        }
        progress.show()
        lifecycleScope.launch(Dispatchers.IO) {
            dbService.loadAllPins { loadedPins: List<PinModel> ->
                Log.d(TAG, "Loaded Pins: ${loadedPins.size}")
                updatePinsList(loadedPins, false)
                allPins = currentPins.toMutableList()
                requireActivity().runOnUiThread {
                    progress.dismiss()
                }
            }
        }
    }

    fun updateQuery(query: String) {
        this.query = query
        updatePinsList(allPins)
    }

    private fun updatePinsList(pins: List<PinModel>, filter: Boolean = true) {
        val pinsWithPdfLink = pins.filter { it.pdfLink != null }
        with(currentPins) {
            clear()
            addAll(if (filter) pinsWithPdfLink.filter {
                it.title!!.lowercase().contains(query.lowercase())
            }.toMutableList() else pinsWithPdfLink.toMutableList())
        }
        requireActivity().runOnUiThread {
            amountPinsTextView.text = "Results: " + currentPins.size
            pinListViewAdapter.notifyDataSetChanged()
            pinListView.scrollTo(0, 0)
            (pinListView.layoutManager!! as LinearLayoutManager).scrollToPositionWithOffset(0, 0)
        }
    }

    companion object {
        private const val TAG = "SearchObjectFragment"
        const val ARG_QUERY = "query"
    }
}