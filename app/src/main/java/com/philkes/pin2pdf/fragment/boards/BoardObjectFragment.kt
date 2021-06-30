package com.philkes.pin2pdf.fragment.boards

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.philkes.pin2pdf.R
import com.philkes.pin2pdf.adapter.PinAdapter
import com.philkes.pin2pdf.api.pinterest.PinterestAPI
import com.philkes.pin2pdf.model.PinModel
import com.philkes.pin2pdf.storage.local.service.DBService
import java.util.*
import java.util.stream.Collectors

/**
 * Fragments inside of BoardFragment
 * A BoardObjectFragment represents 1 Pinterest Board with its Pins (1 Tab)
 */
class BoardObjectFragment : Fragment() {
    private lateinit var pinListView: RecyclerView
    private lateinit var pinListViewAdapter: RecyclerView.Adapter<*>
    private lateinit var pinsList: MutableList<PinModel>
    private lateinit var boardName: String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.board_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args = arguments
        boardName = args!!.getString(ARG_BOARD)!!
        setupUI(view)
        loadPins()
    }

    private fun setupUI(view: View) {
        pinsList = ArrayList()
        pinListView = view.findViewById(R.id.pins_list)
        pinListViewAdapter = PinAdapter(pinsList)
        pinListView.setAdapter(pinListViewAdapter)
        pinListView.setItemAnimator(DefaultItemAnimator())
        val listViewManager = LinearLayoutManager(view.context)
        pinListView.setLayoutManager(listViewManager)
        val dividerItemDecoration = DividerItemDecoration(
            pinListView.getContext(),
            listViewManager.orientation
        )
        pinListView.addItemDecoration(dividerItemDecoration)
    }

    private fun loadPins() {
        val progress = ProgressDialog(context)
        progress.setTitle("Loading Pins of '$boardName'")
        progress.setMessage("Please wait...")
        progress.setCancelable(false) // disable dismiss by tapping outside of the dialog
        progress.show()
        val api = PinterestAPI.getInstance(context!!)!!
        api.requestPinsOfBoard(
            BoardFragment.USER,
            boardName,
            false,
            false,
        { pins: List<PinModel> ->
            println("All Pins: " + pins.size)
            val dbService = DBService.getInstance(context)
            // Try to load all Pins from local DB
            dbService.loadPins(
                pins.map { it.id!! }
            ) { loadedPins: List<PinModel> ->
                println("Loaded Pins: " + loadedPins.size)
                // Check if any Pins weren't loaded from local DB
                val missingPins: MutableList<PinModel> = ArrayList(pins)
                missingPins.removeIf { pinModel: PinModel ->
                    loadedPins.stream().anyMatch { pin: PinModel -> pin.id == pinModel.id }
                }
                println("Missing Pins: " + missingPins.size)
                // Fetch missing Pins from Pinterest API/Scraper
                if (!missingPins.isEmpty()) {
                    api.scrapePDFLinks(missingPins)
                    api.requestPinsInfos(missingPins, { fetchedPins: List<PinModel> ->
                        dbService.insertPins(fetchedPins, Runnable {
                            // Reload all Pins from Local DB
                            dbService.loadPins(
                                pins.map { it.id!! }
                            ) { allPins: List<PinModel> ->
                                updatePinsList(allPins)
                                progress.dismiss()
                            }
                        })
                    }, null)
                } else {
                    updatePinsList(loadedPins)
                    progress.dismiss()
                }
            }
        })
    }

    private fun updatePinsList(pins: List<PinModel>) {
        pinsList.clear()
        pinsList.addAll(pins)
        activity!!.runOnUiThread { pinListViewAdapter.notifyDataSetChanged() }
    }

    companion object {
        const val ARG_BOARD = "board"
    }
}